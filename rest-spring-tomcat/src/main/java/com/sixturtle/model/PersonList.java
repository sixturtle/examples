/**
 * Copyright (c) 2016, Sixturtle, LLC.
 * All rights reserved
 */
package com.sixturtle.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Anurag Sharma
 */
@XmlRootElement (name = "persons")
public class PersonList implements Serializable {
    private static final long serialVersionUID = -7602890372816844053L;
    List<Person> persons = new ArrayList<>();
    /**
     * @return the persons
     */
    @XmlElement (name = "person")
    public List<Person> getPersons() {
        return persons;
    }
    /**
     * @param persons the persons to set
     */
    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }
}
