/**
 * Copyright (c) 2016, Sixturtle, LLC.
 * All rights reserved
 */
package com.sixturtle.configuration;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author Anurag Sharma
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "com.sixturtle" })
@EnableJpaRepositories(basePackages = { "com.sixturtle.service" })
@PropertySource(value = {
        "classpath:application.properties",
        "file:${config.dir}/application.properties"
}, ignoreResourceNotFound = true)
public class ApplicationConfiguration {
    private Logger log = LoggerFactory.getLogger(ApplicationConfiguration.class);

    @Autowired
    Environment env;

    /**
     * @return {@link LocalContainerEntityManagerFactoryBean}
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        log.debug("Catalina Home: {}", env.getProperty("catalina.home"));
        log.debug("all env content: {}", env);

       LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
       em.setDataSource(dataSource());
       em.setPackagesToScan(new String[] { "com.sixturtle.model" });

       JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
       em.setJpaVendorAdapter(vendorAdapter);
       em.setJpaProperties(additionalProperties());

       return em;
    }

    /**
     * @return {@link DataSource}
     */
    @Bean
    public DataSource dataSource() {
        System.out.println(env);

       DriverManagerDataSource dataSource = new DriverManagerDataSource();
       dataSource.setDriverClassName(env.getRequiredProperty("db.driver"));
       dataSource.setUrl(env.getRequiredProperty("db.url"));
       dataSource.setUsername(env.getRequiredProperty("db.username"));
       dataSource.setPassword(env.getRequiredProperty("db.password"));
       return dataSource;
    }

    /**
     * @param emf {@link EntityManagerFactory}
     * @return {@link PlatformTransactionManager}
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
       JpaTransactionManager transactionManager = new JpaTransactionManager();
       transactionManager.setEntityManagerFactory(emf);

       return transactionManager;
    }

    /**
     * @return {@link PersistenceExceptionTranslationPostProcessor}
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
       return new PersistenceExceptionTranslationPostProcessor();
    }

    /**
     * @return {@link Properties}
     */
    Properties additionalProperties() {
       Properties properties = new Properties();
       properties.setProperty("hibernate.hbm2ddl.auto", env.getRequiredProperty("hibernate.hbm2ddl.auto"));
       properties.setProperty("hibernate.show_sql", env.getRequiredProperty("hibernate.show_sql"));
       properties.setProperty("hibernate.format_sql", env.getRequiredProperty("hibernate.format_sql"));
       return properties;
    }
}
