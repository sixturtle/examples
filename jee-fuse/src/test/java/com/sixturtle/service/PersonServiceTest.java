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
import com.sixturtle.db.PersonRepository;
import com.sixturtle.model.PersonEntity;

/**
 * JUnit Test for PersonController.
 *
 * @author Anurag Sharma
 */
public class PersonServiceTest extends RestApiTest {
    private static final Logger log = LoggerFactory.getLogger(PersonServiceTest.class);

    /*
     * (non-Javadoc)
     * @see com.sixturtle.common.RestApiTest#buildJaxRsApiInstance()
     */
    @Override
    protected Object buildJaxRsApiInstance() {
        PersonRepository repository = new PersonRepository();
        repository.setEntityManager(em);
        repository.setValidator(validator);

        PersonService service = new PersonService();
        service.setRepository(repository);
        return service;
    }

    /*
     * (non-Javadoc)
     * @see com.sixturtle.common.BasicJPATest#getDbUnitDataUrl()
     */
    @Override
    protected URL getDbUnitDataUrl() {
        return this.getClass().getResource("/dbunit/person-test.xml");
    }

    @Test
    public void testListPersons() {
        Client client = createClient();

        try {
            int count = getDbUnitTable("Person").getRowCount();
            Response r = client.target(getBaseUrl() + "/persons")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();
            PersonEntity[] persons = r.readEntity(PersonEntity[].class);
            log.debug("\n headers: {}, \n persons: {}", r.getHeaders(), persons);
            assertTrue("Person count does not match", persons.length == count);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        } finally {
            client.close();
        }
    }

    @Test
    public void testCreatePerson() {
        Client client = createClient();

        try {
            int count = getDbUnitTable("Person").getRowCount();
            Response r = client.target(getBaseUrl() + "/persons")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(loadFile("/json/person-create.json")));
            int newCount = getDbUnitTable("Person").getRowCount();

            assertEquals("Invalid response code", Response.Status.CREATED.getStatusCode(), r.getStatus());
            assertTrue("Created person not found in the table", count + 1 == newCount);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        } finally {
            client.close();
        }
    }
}
