package com.sixturtle.common;

import java.io.File;
import java.net.URL;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.internal.SessionImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines fixture for starting and stopping the transaction for jUnit. The
 * transaction is wrapped around the test method and it is rolledback at the end
 * of each test.
 *
 * <br/>
 *
 * This class has been enhanced to include DbUnit framework for providing a test
 * data set specific to a test and removes use of hibernate import.sql which was
 * providing a test data set common of all the tests.
 *
 * @author Anurag Sharma
 */
public abstract class BasicJPATest {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected static EntityManager em   = null;
    protected static Validator validator = null;
    protected static IDatabaseConnection connection;

    /**
     * Runs once at the beginning of the test suite.
     *
     * @throws Exception
     *             When there is a setup error
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        if (em == null) {
            em = Persistence.createEntityManagerFactory("jUnitPersistenceUnit").createEntityManager();
            em.setFlushMode(FlushModeType.AUTO);
        }

        final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();

        // Configure DbUnit using the same data source as JPA
        connection = new DatabaseConnection(((SessionImpl) (em.getDelegate())).connection());
    }

    /**
     * Runs at the end of the test suite.
     */
    @AfterClass
    public static void tearDownClass() {
        em = null;
    }

    /**
     * Start the transaction before the test.
     * @throws Exception
     */
    @Before
    public void setup() {
        // Load DbUnit based test data set
        try {
            URL fileUrl = getDbUnitDataUrl();
            log.debug("Loading DbUnit data file: {}", fileUrl);
            if (fileUrl != null) {
                IDataSet dataset = new FlatXmlDataSetBuilder().setColumnSensing(true)
                                                              .build(new File(fileUrl.getPath()));
                DatabaseOperation.CLEAN_INSERT.execute(connection, dataset);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.debug("Starting a transaction");
        em.getTransaction().begin();
    }

    /**
     * Rollback the transaction before the test.
     */
    @After
    public void teardown() {
        if (em.getTransaction().isActive()) {
            log.debug("Rolling back transaction");
            em.getTransaction().rollback();
        }
    }

    protected URL getDbUnitDataUrl() {
        return null;
    }

    protected ITable getDbUnitTable(String tableName) throws Exception {
        em.flush();
        return connection.createDataSet().getTable(tableName);
    }
}

