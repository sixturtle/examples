package com.sixturtle.db;

import javax.inject.Named;

import com.sixturtle.model.PersonEntity;

/**
 * Represents {@link PersonEntity} repository.
 *
 * @author Anurag Sharma
 */
@Named
public class PersonRepository extends JPARepositoryImpl<PersonEntity, Long> {

}
