package com.sixturtle.remote.service;

import java.io.Serializable;

/**
 * Security Context for using Mashup API.
 *
 * @author Anurag Sharma
 */
public class MashupContext implements Serializable {
    private static final long serialVersionUID = 6605440997492972143L;
    public static final String AUTH_HEADER = "X-Mashape-Key";

    private String baseUrl;
    private String apiPath;
    private String authcode;

    /**
     * @return the baseUrl
     */
    public String getBaseUrl() {
        return baseUrl;
    }
    /**
     * @param baseUrl the baseUrl to set
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    /**
     * @return the apiPath
     */
    public String getApiPath() {
        return apiPath;
    }
    /**
     * @param apiPath the apiPath to set
     */
    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }
    /**
     * @return the authcode
     */
    public String getAuthcode() {
        return authcode;
    }
    /**
     * @param authcode the authcode to set
     */
    public void setAuthcode(String authcode) {
        this.authcode = authcode;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MashupContext {")
               .append("baseUrl:").append(baseUrl).append(",")
               .append("apiPath:").append(apiPath).append(",")
               .append("authcode:").append(authcode)
               .append("}");
        return builder.toString();
    }
}
