package com.sixturtle.jwt;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sixturtle.jwt.JWTRequestFilter.JWTPrincipal;

/**
 * A sample REST endpoint to demonstrate {@link JWTRequestFilter} and {@link JWTSecured}.
 */
@Path("/echo")
@Produces({ MediaType.TEXT_PLAIN })
public class EchoAPI {
    private static final Logger log = LoggerFactory.getLogger(EchoAPI.class);

    @Context
    protected SecurityContext securityContext;

    /**
     * A sample GET request to demonstrate {@link JWTRequestFilter}
     *
     * @param message The query parameter
     * @return echos back the query parameter
     */
    @GET
    @JWTSecured
    @RolesAllowed("USER") // just for demonstration, check JWTRequestFilter to see what roles are injected to security context
    public Response echo(@QueryParam("message") String message) {
        JWTPrincipal p = (JWTPrincipal) securityContext.getUserPrincipal();
        log.info("Received message: {} from principal: {}", message, p);
        // TODO: inspect principal for fine grain security before proceeding
        return Response.ok().entity(message).build();
    }
}
