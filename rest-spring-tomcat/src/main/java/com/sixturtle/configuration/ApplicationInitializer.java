/**
 * Copyright (c) 2016, Sixturtle, LLC.
 * All rights reserved
 */
package com.sixturtle.configuration;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * @author Anurag Sharma
 */
public class ApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer#getRootConfigClasses()
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { ApplicationConfiguration.class };
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer#getServletConfigClasses()
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.support.AbstractDispatcherServletInitializer#getServletMappings()
     */
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

}
