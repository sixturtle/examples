package com.sixturtle.jwt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;

/**
 * A name bound annotation to be used with {@link JWTRequestFilter}.
 *
 * Name-Binding: Filters and interceptors can be name-bound. Name binding is a
 * concept that allows to say to a JAX-RS runtime that a specific filter or
 * interceptor will be executed only for a specific resource method. When a
 * filter or an interceptor is limited only to a specific resource method we say
 * that it is name-bound. Filters and interceptors that do not have such a
 * limitation are called global.
 */
@NameBinding
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JWTSecured {

}
