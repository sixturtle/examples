package com.sixturtle.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

/**
 * Represents an error to indicate that the data is not found.
 *
 * @author Anurag Sharma
 */
public class UnknownEntityException extends Exception {
    private static final long serialVersionUID = 5849318601242477783L;
    private Map<String, String> violations; // (field, message) tuple

    /**
     * Default constructor.
     */
    public UnknownEntityException() {

    }

    /**
     * Constructor that takes message
     *
     * @param message
     *             The error message
     */
    public UnknownEntityException(String message) {
        super(message);
    }

    /**
     * Constructor with violation map and cause of exception.
     *
     * @param message
     *            The error message
     * @param violations
     *            A map of (field, error message)
     */
    public UnknownEntityException(final String message, final Map<String, String> violations) {
        super(message);
        this.violations = violations;
    }

    /**
     * Constructor with violation map and cause of exception.
     *
     * @param message
     *            The error message
     * @param violations
     *            A map of (field, error message)
     * @param cause
     *            The chained exception
     */
    public UnknownEntityException(final String message, final Map<String, String> violations, final Throwable cause) {
        super(message, cause);
        this.violations = violations;
    }

    /**
     * @return the violations
     */
    public final Map<String, String> getViolations() {
        return violations;
    }

    /**
     * Converts a {@link Set} of {@link ConstraintViolation} into a {@link Map}
     * of (String, String).
     *
     * @param message
     *            The error message
     * @param violations
     *            A {@link Set} of {@link ConstraintViolation}
     * @param <T>
     *            Generic type
     * @return A {@link Map} of (String, String)
     */
    public static <T> UnknownEntityException valueOf(final String message, final Set<ConstraintViolation<T>> violations) {
        Map<String, String> violationsMap = new HashMap<String, String>();
        for (ConstraintViolation<T> v : violations) {
            violationsMap.put(v.getPropertyPath().toString(), v.getMessage());
        }
        return new UnknownEntityException(message, violationsMap);
    }

}
