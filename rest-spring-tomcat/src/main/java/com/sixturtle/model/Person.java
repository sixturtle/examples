/**
 * Copyright (c) 2016, Sixturtle, LLC.
 * All rights reserved
 */
package com.sixturtle.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author Anurag Sharma
 */
@Entity
@XmlRootElement (name = "person")
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Person implements Serializable {
    private static final long serialVersionUID = 3767104162666435761L;

    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private long id;

    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phone;

    /**
     * Default constructor
     */
    public Person() {
    }

    /**
     * Field constructor
     *
     * @param firstName
     *            The first name
     * @param middleName
     *            The middle name
     * @param lastName
     *            The last name
     * @param email
     *            The email address
     * @param phone
     *            The phone number
     */
    public Person(
            String firstName,
            String middleName,
            String lastName,
            String email,
            String phone) {
        super();
        this.firstName  = firstName;
        this.middleName = middleName;
        this.lastName   = lastName;
        this.email      = email;
        this.phone      = phone;
    }


    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }
    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    /**
     * @return the middleName
     */
    public String getMiddleName() {
        return middleName;
    }
    /**
     * @param middleName the middleName to set
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }
    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }
    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }
    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Person [firstName=").append(firstName).append(", middleName=").append(middleName)
                .append(", lastName=").append(lastName).append(", email=").append(email).append(", phone=")
                .append(phone).append("]");
        return builder.toString();
    }

}
