package com.sixturtle.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents various options for Roles of a user.
 *
 * @author Anurag Sharma
 */
public enum RoleType {
    ADMIN ("Administrator"),
    USER ("User");

    private String description;

    /**
     * Private constructor to allow description.
     *
     * @param description   The string value of the enum constant.
     */
    private RoleType(String description) {
        this.description = description;
    }

    /**
     * @return  The string value of the enum constant
     */
    @JsonValue
    public String getDescription() {
        return description;
    }
}