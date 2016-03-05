package com.sixturtle.web;

import java.io.EOFException;
import java.util.List;

import javax.ejb.EJBAccessException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import com.sixturtle.exception.InvalidEntityException;
import com.sixturtle.exception.UnknownEntityException;

/**
 * Maps most commonly known HTTP errors into a JSON response. This should hide
 * the regular error pages shown by server for these common HTTP errors.
 *
 * @author Anurag Sharma
 */
@Provider
public class BasicExceptionMapper implements ExceptionMapper<Throwable> {
    private static Logger logger = LoggerFactory.getLogger(BasicExceptionMapper.class);

    @Context
    private HttpHeaders headers;

    /**
     * @param headers
     *            {@link HttpHeaders}
     */
    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
     */
    @Override
    public Response toResponse(final Throwable ex) {
        // default is server error
        ErrorInfo error = convertToError(ex);
        if (error == null) {
            /*
             * If the current exception class could not be mapped then try
             * mapping the root cause exception. But this should be the last try
             * to map exception to HTTP error.
             */
            Throwable t = ex.getCause();
            error = convertToError(t);
            if (error == null) {
                logger.error("Unable to find a matching code so mapping to the default HTTP 500", ex);
                error = new ErrorInfo(Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Unexpected server error occurred");
            }
        }

        /*
         * Convert the errorInfo to match with "Accept" header
         */
        MediaType media = MediaType.APPLICATION_JSON_TYPE;
        List<MediaType> accepts = headers.getAcceptableMediaTypes();
        if (accepts != null && !accepts.isEmpty()) {
            media = accepts.get(0); // pick the first one
        }
        if (media != MediaType.APPLICATION_JSON_TYPE && media != MediaType.APPLICATION_XML_TYPE) {
            media = MediaType.APPLICATION_JSON_TYPE; // default to JSON
        }
        return Response.status(error.getCode()).type(media).entity(error).build();
    }

    /**
     * Converts an exception to ErrorInfo.
     *
     * @param ex
     *            the exception to be converted to {@link ErrorInfo}
     * @return the error info
     */
    private ErrorInfo convertToError(final Throwable ex) {
        // default is server error
        ErrorInfo error = new ErrorInfo(Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "Unexpected server error occurred");
        if (ex instanceof EJBAccessException) {
            error.setCode(Status.FORBIDDEN.getStatusCode());
            error.setMessage("Access restricted");
        } else if ((ex instanceof BadRequestException)
                || (ex instanceof EOFException)) {
            error.setCode(Status.BAD_REQUEST.getStatusCode());
            error.setMessage(ex.getLocalizedMessage());
            if (ex.getCause() instanceof InvalidEntityException) {
                InvalidEntityException e = (InvalidEntityException) ex.getCause();
                error.setMessage(e.getMessage());
                error.setAdditionalInfo(e.getViolations());
            }

        } else if (ex instanceof NotFoundException) {
            error.setCode(Status.NOT_FOUND.getStatusCode());
            error.setMessage(ex.getLocalizedMessage());
            if (ex.getCause() instanceof UnknownEntityException) {
                UnknownEntityException e = (UnknownEntityException) ex.getCause();
                error.setMessage(e.getMessage());
                error.setAdditionalInfo(e.getViolations());
            }

        } else if (ex instanceof UnrecognizedPropertyException) {
            /*
             * WARNING: UnrecognizedPropertyException is coming from Jackson
             * library which we did not intend to depend on at compile time.
             * However, catching this specific exception allows us to return
             * appropriate HTTP code rather than throwing HTTP 500.
             *
             * Further usage of classes coming from Jackson library are not
             * recommended.
             */
            error.setCode(Status.BAD_REQUEST.getStatusCode());
            error.setMessage("Unrecognized field: "
                + ((UnrecognizedPropertyException) ex).getPropertyName());
            logger.error("Unrecognized fields", ex);

        } else if (ex instanceof JsonParseException) {
            /*
             * WARNING: JsonParseException is coming from Jackson library which
             * we did not intend to depend on at compile time. However, catching
             * this specific exception allows us to return appropriate HTTP code
             * rather than throwing HTTP 500.
             *
             * Further usage of classes coming from Jackson library are not
             * recommended.
             */
            error.setCode(Status.BAD_REQUEST.getStatusCode());
            error.setMessage("Invalid input format");
            logger.error("Invalid input format", ex);

        } else if (ex instanceof JsonMappingException) {
            /*
             * WARNING: JsonMappingException is coming from Jackson library
             * which we did not intend to depend on at compile time. However,
             * catching this specific exception allows us to return appropriate
             * HTTP code rather than throwing HTTP 500.
             *
             * Further usage of classes coming from Jackson library are not
             * recommended.
             */
            error.setCode(Status.BAD_REQUEST.getStatusCode());
            error.setMessage("Invalid input format");
            logger.error("Invalid input format", ex);

        } else if (ex instanceof NotAllowedException) {
            error.setCode(Status.METHOD_NOT_ALLOWED.getStatusCode());
            error.setMessage(ex.getLocalizedMessage());

        } else if (ex instanceof NotAcceptableException) {
            error.setCode(Status.NOT_ACCEPTABLE.getStatusCode());
            error.setMessage(ex.getLocalizedMessage());

        } else if (ex instanceof NotSupportedException) {
            error.setCode(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
            error.setMessage(ex.getLocalizedMessage());

        } else if (ex instanceof NotAuthorizedException) {
            error.setCode(Status.UNAUTHORIZED.getStatusCode());
            error.setMessage(ex.getLocalizedMessage());

        } else if (ex instanceof WebApplicationException) {
            WebApplicationException e = (WebApplicationException) ex;
            error.setCode(e.getResponse().getStatus());
            error.setMessage(ex.getLocalizedMessage());
            logger.error("Internal error occured", ex);

        } else {
            error = null;
        }
        return error;
    }
}
