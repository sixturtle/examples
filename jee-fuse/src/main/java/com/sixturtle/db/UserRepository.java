package com.sixturtle.db;

import javax.inject.Named;

import com.sixturtle.model.UserEntity;

/**
 * Represents {@link UserEntity} repository.
 *
 * @author Anurag Sharma
 */
@Named
public class UserRepository extends JPARepositoryImpl<UserEntity, Long> {

}
