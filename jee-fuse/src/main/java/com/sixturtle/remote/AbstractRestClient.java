package com.sixturtle.remote;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import com.sixturtle.exception.InvalidEntityException;
import com.sixturtle.exception.RemoteCallException;


/**
 * Common ground work for making REST calls using JAX-RS client APIs.
 *
 * @author  Anurag Sharma
 */
public abstract class AbstractRestClient {
    private static final Logger log = LoggerFactory.getLogger(AbstractRestClient.class);
    private static final int MAX_AUTH_RETRY = 1;

    protected static final String AUTH_BEARER_TOKEN = "Bearer %s";
    protected static final String HDR_AUTHORIZATION = "Authorization";

    private Client client;
    private ClientResponseMapper mapper = new ClientResponseMapper();

    /**
     * Default constructor sets up all the required providers.
     */
    public AbstractRestClient() {
        this(ClientBuilder.newClient());
    }

    /**
     * Special constructor to allow passing custom Client. It can be used to
     * create a client trusting all SSL certificates. May be useful during unit
     * testing but not recommended for production.
     *
     * @param client
     *            An instance of {@link Client}
     */
    public AbstractRestClient(final Client client) {
        try {
            this.client = client.register(JacksonJsonProvider.class)
                                .register(mapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @return  an instance of {@link Client} which has
     * {@link JacksonJsonProvider} and {@link ClientResponseMapper}.
     */
    public Client getClient() {
        return client;
    }

    /**
     * @return an instance of {@link ClientResponseMapper}
     */
    public ClientResponseMapper getMapper() {
        return mapper;
    }

    /**
     * An abstract method that subclass must implement to handle HTTP 401/403
     * errors that may occur during {@link #invoke(InvokeCommand)}.
     *
     * @throws RemoteCallException
     *                  in case of runtime error from Rest API call to authenticate
     */
    protected abstract void authenticate() throws RemoteCallException;

    /**
     * A Rest API execution template with common error handling and
     * authentication. Idea is to capture 401/403 and call
     * {@link #authenticate()} first and then try again. This method translates
     * any other runtime error into a {@link RemoteCallException}.
     *
     * @param <R>
     *            The response type
     * @param command
     *            An implementation of {@link InvokeCommand}
     * @return The response returned by {@link InvokeCommand}
     * @throws InvalidEntityException
     *             in case of validation error performed by
     *             {@link InvokeCommand}
     * @throws RemoteCallException
     *             in case of runtime error from Rest API call
     */
    protected <R> R invoke(final InvokeCommand<R> command)
            throws InvalidEntityException, RemoteCallException {
        R response = null;

        for (int iIndex = 0; (iIndex <= MAX_AUTH_RETRY) && (response == null);) {
            try {
                response = command.execute();
                break;
            } catch (NotAuthorizedException | ForbiddenException e) {
                if (iIndex < MAX_AUTH_RETRY) {
                    ++iIndex;
                    log.warn("Auth token expired, refresh token retryCount: {}", iIndex);
                    authenticate();
                    continue;
                } else {
                    throw new RemoteCallException(
                            "Exceeded max retries but failed to authenticate",
                            e,
                            getMapper().getErrorCode(),
                            getMapper().getErrorMessage());
                }
            } catch (ClientErrorException e) {
                /*
                 * If ForbiddenException and NotAuthorizedException is not
                 * thrown by the framework, then inspect the error code.
                 */
                if ((getMapper().getErrorCode() == Response.Status.FORBIDDEN.getStatusCode()
                        ||  getMapper().getErrorCode() == Response.Status.UNAUTHORIZED.getStatusCode())
                        && (iIndex < MAX_AUTH_RETRY)) {
                    ++iIndex;
                    log.warn("Authentication required error, retryCount: {}", iIndex);
                    authenticate();
                    continue;
                } else {
                    throw new RemoteCallException(
                            "Client request error during remote call",
                            e,
                            getMapper().getErrorCode(),
                            getMapper().getErrorMessage());
                }
            } catch (ProcessingException e) {
                String message = "Unexpected error occurred: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                throw new RemoteCallException(
                        "Processing error during remote call",
                        e,
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                        message);
            } catch (WebApplicationException e) {
                throw new RemoteCallException(
                        "Server error during remote call",
                        e,
                        getMapper().getErrorCode(),
                        getMapper().getErrorMessage());
            }
        }
        return response;
    }

    /**
     * A generic command interface to serve as a callback. This command may
     * throw {@link RuntimeException}.
     *
     * <p/>
     *
     * The caller of {@link AbstractRestClient#invoke(InvokeCommand)}
     * can implement this interface to just focus on making Rest API
     * call and not worrying about error handling and authentication.
     *
     * @param <R>       The return type of {@link #execute()} method
     *
     * @author  Anurag Sharma
     */
    protected interface InvokeCommand<R> {
        /**
         * Executes command.
         *
         * @return The response of type R
         */
        R execute();
    }
}
