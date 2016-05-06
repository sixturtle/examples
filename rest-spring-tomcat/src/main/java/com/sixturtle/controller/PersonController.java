/**
 * Copyright (c) 2016, Sixturtle, LLC.
 * All rights reserved
 */
package com.sixturtle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sixturtle.model.Person;
import com.sixturtle.model.PersonList;
import com.sixturtle.service.PersonRepository;

/**
 * @author Anurag Sharma
 */
@RestController
@RequestMapping (value = "/persons")
class PersonController {

    @Autowired
    PersonRepository personRepository;

    /**
     * @param email The person email address
     * @return  {@link PersonList}
     */
    @RequestMapping (method = RequestMethod.GET)
    @ResponseBody
    public PersonList listAll(@RequestParam(value = "email", required = false) String email) {
        PersonList persons = new PersonList();

        if (!StringUtils.isEmpty(email)) {
            Person p = personRepository.findByEmail(email);
            persons.getPersons().add(p);
        } else {
            Iterable<Person> result = personRepository.findAll();
            for (Person p : result) {
                persons.getPersons().add(p);
            }
        }

        return persons;
    }

    /**
     * @param id The person ID
     * @return {@link Person}
     */
    @RequestMapping (value = "{id}", method = RequestMethod.GET)
    @ResponseBody
    public Person findById(@PathVariable("id") long id) {
        return personRepository.findOne(id);
    }

    /**
     * @param p {@link Person}
     */
    @RequestMapping (method = RequestMethod.POST)
    @ResponseStatus (code = HttpStatus.CREATED)
    public void create(@RequestBody Person p) {
        personRepository.save(p);
    }

    /**
     * @param p {@link Person}
     */
    @RequestMapping (method = RequestMethod.PUT)
    public void update(@RequestBody Person p) {
        personRepository.save(p);
    }

    /**
     * @param id {@link Person} ID
     */
    @RequestMapping (method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") long id) {
        personRepository.delete(id);
    }
}
