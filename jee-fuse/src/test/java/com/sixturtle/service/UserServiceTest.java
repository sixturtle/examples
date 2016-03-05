package com.sixturtle.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sixturtle.common.RestApiTest;
import com.sixturtle.db.RoleRepository;
import com.sixturtle.db.UserRepository;
import com.sixturtle.model.UserEntity;

/**
 * JUnit Test for UserController.
 *
 * @author Anurag Sharma
 */
public class UserServiceTest extends RestApiTest {
    private static final Logger log = LoggerFactory.getLogger(UserServiceTest.class);

    /*
     * (non-Javadoc)
     * @see com.sixturtle.common.RestApiTest#buildJaxRsApiInstance()
     */
    @Override
    protected Object buildJaxRsApiInstance() {
        UserRepository userRepository = new UserRepository();
        userRepository.setEntityManager(em);
        userRepository.setValidator(validator);

        RoleRepository roleRepository = new RoleRepository();
        roleRepository.setEntityManager(em);
        roleRepository.setValidator(validator);

        UserService service = new UserService();
        service.setUserRepository(userRepository);
        service.setRoleRepository(roleRepository);
        return service;
    }

    /*
     * (non-Javadoc)
     * @see com.sixturtle.common.BasicJPATest#getDbUnitDataUrl()
     */
    @Override
    protected URL getDbUnitDataUrl() {
        return this.getClass().getResource("/dbunit/user-test.xml");
    }

    @Test
    public void testListUsers() {
        Client client = createClient();

        try {
            int count = getDbUnitTable("User").getRowCount();
            Response r = client.target(getBaseUrl() + "/users")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();
            UserEntity[] users = r.readEntity(UserEntity[].class);
            log.debug("\n headers: {}, \n users: {}", r.getHeaders(), users);
            assertTrue("Users count does not match", users.length == count);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        } finally {
            client.close();
        }
    }

    @Test
    public void testCreateUser() {
        Client client = createClient();

        try {
            int count = getDbUnitTable("User").getRowCount();
            Response r = client.target(getBaseUrl() + "/users")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(loadFile("/json/user-create.json")));
            int newCount = getDbUnitTable("User").getRowCount();

            assertEquals("Invalid response code", Response.Status.CREATED.getStatusCode(), r.getStatus());
            assertTrue("Created user not found in the table", count + 1 == newCount);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        } finally {
            client.close();
        }
    }
}
