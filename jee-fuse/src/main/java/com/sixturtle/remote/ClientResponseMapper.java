package com.sixturtle.remote;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A custom JAX-RX provider to implement {@link ClientResponseFilter} to filter
 * request and response of an API call for logging purpose.
 *
 * @author      Anurag Sharma
 */
@Provider
public class ClientResponseMapper implements ClientResponseFilter {
    private static Logger log = LoggerFactory.getLogger(ClientResponseMapper.class);

    private int     errorCode;
    private String  errorMessage = "";

    /**
     * @return the errorCode
     */
    public final int getErrorCode() {
        return errorCode;
    }
    /**
     * @return the errorMessage
     */
    public final String getErrorMessage() {
        return errorMessage;
    }

    /* (non-Javadoc)
     * @see javax.ws.rs.client.ClientResponseFilter#filter(javax.ws.rs.client.ClientRequestContext, javax.ws.rs.client.ClientResponseContext)
     */
    @Override
    public void filter(
            final ClientRequestContext requestContext,
            final ClientResponseContext responseContext) throws IOException {

        this.errorCode = responseContext.getStatus();

        // In case of HTTP error, log request and response
        if (responseContext.getStatus() >= Response.Status.BAD_REQUEST.getStatusCode()) {
            String reqMessage = "";
            if (requestContext.hasEntity()) {
                Object e = requestContext.getEntity();

                if (e instanceof Form) {
                    reqMessage = ((Form) e).asMap().toString();
                } else if (e instanceof Object[]) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[");
                    for (Object o : (Object[]) e) {
                        sb.append(o.toString()).append(",");
                    }
                    sb.append("]");
                    reqMessage = sb.toString();
                } else {
                    reqMessage = e.toString();
                }
            }
            String respMessage = "";
            if (responseContext.hasEntity()) {
                InputStream is = responseContext.getEntityStream();
                respMessage = convertStreamToString(is);
            }
            log.error("REST API error:\n{} {}\n{}\n{}\n\nHTTP {} {}\n{}\n{}",
                    requestContext.getMethod(),
                    requestContext.getUri(),
                    reqHeaders(requestContext.getHeaders()),
                    reqMessage,
                    responseContext.getStatus(),
                    responseContext.getStatusInfo().getReasonPhrase(),
                    respHeaders(responseContext.getHeaders()),
                    respMessage);

           this.errorMessage = respMessage;
        }
    }

    /**
     * Convert response headers into a {@link String} format.
     *
     * @param headers   An instance of {@link MultivaluedMap}
     * @return          The {@link String} representation of the header values
     */
    private String reqHeaders(final MultivaluedMap<String, Object> headers) {
        StringBuilder builder = new StringBuilder();

        for (String key : headers.keySet()) {
            builder.append(key).append(": ").append(headers.get(key)).append("\n");
        }
        return builder.toString();
    }

    /**
     * Convert response headers into a {@link String} format.
     * Note: This version is needed due to varying generic type.
     *
     * @param headers   An instance of {@link MultivaluedMap}
     * @return          The {@link String} representation of the header values
     */
    private String respHeaders(final MultivaluedMap<String, String> headers) {
        StringBuilder builder = new StringBuilder();

        for (String key : headers.keySet()) {
            builder.append(key).append(": ").append(headers.get(key)).append("\n");
        }
        return builder.toString();
    }

    /**
     * Reads entire input stream and converts to String.
     *
     * <strong>
     * Note: It does not close the stream. But {@link InputStream}
     * received from {@link ClientResponseContext} is expected to
     * be closed by JAX-RS runtime.
     * </strong>
     *
     * @param is    The input stream
     * @return      The content of input stream in String format
     */
    private String convertStreamToString(final InputStream is) {
        @SuppressWarnings("resource")
        Scanner s = new Scanner(is).useDelimiter("\\A"); // read all
        return s.hasNext() ? s.next() : "";
    }
}
