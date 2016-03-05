package com.sixturtle.common;

import com.sixturtle.web.BasicExceptionMapper;
import io.undertow.Undertow;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;

import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * A base class for implementing integration tests for a set of REST API.
 *
 * <p>
 * It uses {@link UndertowJaxrsServer} to deploy a local JAX-RS application and
 * allows a concrete test to inject a set of singletons for the application.
 * <br/>
 * This allows running a servlet in memory and exposing REST APIs as it would
 * have been running in a JEE container like Wildfly.
 * </p>
 *
 * <p>
 * It is also a subclass of {@link BasicJPATest} to leverage the running in
 * memory database. In memory database together with in memory
 * {@link UndertowJaxrsServer} provides end-to-end integration test capabilities
 * from within jUnit.
 * </p>
 *
 * <p>
 * It also provides a JAX-RS client instance to invoke APIs available from
 * {@link UndertowJaxrsServer}. This class takes care of registering
 * {@link JacksonJsonProvider} and {@link BasicExceptionMapper} for both server
 * and client side.
 * </p>
 *
 * @author Anurag Sharma
 */
public abstract class RestApiTest extends BasicJPATest {
    private static final String HTTP_HOST = "localhost";
    private static final int    HTTP_PORT = 7788;

    protected static UndertowJaxrsServer server;
    protected static Set<Object> jaxrsSingletons = new HashSet<>();
    private Object api;
    /**
     * Configures {@link BasicJPATest} and {@link UndertowJaxrsServer} with
     * {@link JacksonJsonProvider} and {@link BasicExceptionMapper}
     *
     * @throws Exception
     *             in case of setup errors
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        BasicJPATest.setUpClass();

        jaxrsSingletons.add(new BasicExceptionMapper());
        jaxrsSingletons.add(new JacksonJsonProvider());

        server = new UndertowJaxrsServer().start(
                            Undertow.builder()
                                    .addHttpListener(HTTP_PORT, HTTP_HOST));
    }

    /**
     * Cleans {@link BasicJPATest} and stops {@link UndertowJaxrsServer}
     */
    @AfterClass
    public static void tearDownClass() {
        BasicJPATest.tearDownClass();
        server.stop();
    }

    /**
     * Configures {@link BasicJPATest} and deploys {@link #buildJaxRsApiInstance()}
     * to {@link UndertowJaxrsServer}. It also instantiates {@link Client} with
     * the registered {@link JacksonJsonProvider}.
     */
    @Before
    public void setup() {
        super.setup();

        api = buildJaxRsApiInstance();
        jaxrsSingletons.add(api);
        server.deploy(JunitRestApp.class);
    }

    /**
     * Cleans {@link BasicJPATest} and closes {@link Client}.
     */
    @After
    public void teardown() {
        jaxrsSingletons.remove(api);
        super.teardown();
    }

    /**
     * @return The base URL of {@link UndertowJaxrsServer}
     */
    protected String getBaseUrl() {
        return "http://" + HTTP_HOST + ":" + HTTP_PORT + "/api";
    }

    /**
     * The concrete test must implement this method to inject a CDI free
     * instance of JAX-RS API class.
     *
     * @return An instance of JAX-RS API class
     */
    protected abstract Object buildJaxRsApiInstance();

    /**
     * Represents a JAX-RS application available via {@link UndertowJaxrsServer}.
     *
     * @author Anurag Sharma
     */
    @ApplicationPath("/api")
    public static class JunitRestApp extends Application {
        /*
         * (non-Javadoc)
         * @see javax.ws.rs.core.Application#getSingletons()
         */
        @Override
        public Set<Object> getSingletons() {
            return jaxrsSingletons;
        }
    }

    /**
     * Loads content from a file.
     *
     * @param filepath  The filepath
     *
     * @return The content of the file
     *
     * @throws Exception in case of IO error
     */
    protected byte[] loadFile(String filepath) throws Exception {
        URL path = this.getClass().getResource(filepath);
        log.debug("Loading filepath: {}", path);
        byte[] json = Files.readAllBytes(Paths.get(path.toURI()));
        return json;
    }

    protected Client createClient() {
        return ClientBuilder.newClient().register(JacksonJsonProvider.class);
    }
}
