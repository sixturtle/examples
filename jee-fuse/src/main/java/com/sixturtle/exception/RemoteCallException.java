package com.sixturtle.exception;

import java.io.IOException;

/**
 * Represents an error that occurred while making a remote call to a 3rd party API.
 *
 * @author  Anurag Sharma
 */
public class RemoteCallException extends IOException {
    private static final long serialVersionUID = 5007279622942466295L;

    private int     statusCode;
    private String  responseMessage;

    /**
     * Constructor to accept error code and error message.
     *
     * @param message           The error message to include summary
     * @param statusCode        The error code received from the remote call
     * @param responseMessage   The native message received from the remote call
     */
    public RemoteCallException(final String message, final int statusCode, final String responseMessage) {
        super(message);
        this.statusCode     = statusCode;
        this.responseMessage = responseMessage;
    }

    /**
     * Constructor to accept error code, error message and the cause
     *
     * @param message
     *            exception message
     * @param cause
     *            exception cause
     * @param statusCode
     *            status code for the error exception
     * @param responseMessage
     *            response message for the exception
     */
    public RemoteCallException(final String message, final Throwable cause, final int statusCode, final String responseMessage) {
        super(message, cause);
        this.statusCode     = statusCode;
        this.responseMessage = responseMessage;
    }

    /**
     * @return the errorCode
     */
    public final int getStatusCode() {
        return statusCode;
    }

    /**
     * @return the responseMessage
     */
    public final String getResponseMessage() {
        return responseMessage;
    }

    /**
     * @return  combined error message
     */
    public final String getUnifiedRemoteError() {
        return statusCode + ": " + responseMessage;
    }
}
