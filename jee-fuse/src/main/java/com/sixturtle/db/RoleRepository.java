package com.sixturtle.db;

import javax.inject.Named;

import com.sixturtle.model.RoleEntity;

/**
 * Represents {@link RoleEntity} repository.
 *
 * @author Anurag Sharma
 */
@Named
public class RoleRepository extends JPARepositoryImpl<RoleEntity, Long> {

}
