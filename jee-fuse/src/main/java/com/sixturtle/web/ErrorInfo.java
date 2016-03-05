package com.sixturtle.web;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * A simple error bean used in sending JSON response body in case of error.
 *
 * @author Anurag Sharma
 */
@XmlRootElement
@JsonInclude(Include.NON_NULL)
public class ErrorInfo implements Serializable {
    private static final long serialVersionUID = 4409782805386039091L;

    /** must be a valid HTTP code. */
    private int     code;
    private String  message;

    /** (field, message) or (name, value) pair. */
    private Map<String, String> additionalInfo;

    /**
     * Default constructor.
     */
    public ErrorInfo() {
    }

    /**
     * Convenience constructor.
     *
     * @param code      The error code
     * @param message   The error that occurred
     */
    public ErrorInfo(final int code, final String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * @return the code
     */
    public final int getCode() {
        return code;
    }
    /**
     * @param code the code to set
     */
    public final void setCode(final int code) {
        this.code = code;
    }
    /**
     * @return the message
     */
    public final String getMessage() {
        return message;
    }
    /**
     * @param message the message to set
     */
    public final void setMessage(final String message) {
        this.message = message;
    }
    /**
     * @return the additionalInfo
     */
    public final Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }
    /**
     * @param additionalInfo the additionalInfo to set
     */
    public final void setAdditionalInfo(final Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ErrorInfo {")
                .append("code:").append(code).append(",")
                .append("message:").append(message).append(",")
                .append("additionalInfo:").append(additionalInfo)
                .append("}");
        return builder.toString();
    }
}
