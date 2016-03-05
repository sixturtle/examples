package com.sixturtle.remote.service;

import com.sixturtle.exception.InvalidEntityException;
import com.sixturtle.exception.RemoteCallException;

/**
 * Interface for Email Validation API.
 *
 * "https://pozzad-email-validator.p.mashape.com/emailvalidator/validateEmail"
 *
 * @author Anurag Sharma
 */
public interface EmailValidator {
    /**
     * Checks email format and domain to see if email is valid.
     *
     * @param email
     *            The email address
     * @return true if valid, false otherwise
     * @throws InvalidEntityException
     *             when validation fails
     * @throws RemoteCallException
     *             when remote call error occurrs
     */
    boolean isValidEmail(final String email) throws RemoteCallException, InvalidEntityException;
}
