package com.sixturtle.remote.service;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents response returned from {@link EmailValidator#isValidEmail(String)}
 *
 * @author Anurag Sharma
 */
public class EmailStatus implements Serializable {
    private static final long serialVersionUID = 3431386157628983516L;

    @JsonProperty("isValid")
    private boolean valid;

    /**
     * @return the isValid
     */
    public boolean isValid() {
        return valid;
    }
    /**
     * @param isValid the isValid to set
     */
    public void setValid(boolean isValid) {
        this.valid = isValid;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EmailStatus {")
               .append("isValid:").append(valid)
               .append("}");
        return builder.toString();
    }
}
