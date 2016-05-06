/**
 * Copyright (c) 2016, Sixturtle, LLC.
 * All rights reserved
 */
package com.sixturtle.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixturtle.model.Person;

/**
 * @author Anurag Sharma
 */
@Service ("personRepository")
@Transactional
public interface PersonRepository extends JpaRepository <Person, Long> {
    /**
     * @param email The email address
     * @return {@link Person}
     */
    Person findByEmail(String email);
}
