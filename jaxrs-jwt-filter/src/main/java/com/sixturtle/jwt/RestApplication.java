package com.sixturtle.jwt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Responsible for handling the requests to RESTful resources.
 *
 * @see         Application
 * @see         ApplicationPath
 */
@ApplicationPath("/api/")
public class RestApplication extends Application {

    private Set<Class< ? >> classes = new HashSet<Class< ? >>();

    /*
     * (non-Javadoc)
     * @see javax.ws.rs.core.Application#getClasses()
     */
    @Override
    public Set<Class< ? >> getClasses() {
        /*
         * Add all the resource service classes here
         */
        classes.addAll(
                Arrays.asList(

                        ));
        return classes;
    }
}