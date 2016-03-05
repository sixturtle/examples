package com.sixturtle.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

/**
 * Represents bean validation exception. This class converts a list of
 * violations into a {@link Map} of fields and error messages.
 *
 * @see <code>persistence.xml for validation-mode</code>
 *
 * @author Anurag Sharma
 */
public class InvalidEntityException extends Exception {
    private static final long serialVersionUID = 1864068115460117002L;
    private Map<String, String> violations;

    /**
     * Default constructor.
     */
    public InvalidEntityException() {
    }

    /**
     * Initialize the exception.
     *
     * @param message
     *            exception message
     * @param violations
     *            {@link Map} of violations
     */
    public InvalidEntityException(final String message, final Map<String, String> violations) {
        super(message + ": " + violations);
        this.violations = violations;
    }

    /**
     * Initialize the exception.
     *
     * @param message
     *            exception message
     * @param cause
     *            exception cause
     * @param violations
     *            {@link Map} of violations
     */
    public InvalidEntityException(final String message, final Throwable cause, final Map<String, String> violations) {
        super(message + ": " + violations, cause);
        this.violations = violations;
    }

    /**
     * Builds a message list with field and error message for each violations.
     *
     * @return A concatenated list of violations with field name and error
     *         message.
     */
    public final Map<String, String> getViolations() {
        return violations;
    }

    /**
     * Converts a {@link Set} of {@link ConstraintViolation} into a {@link Map}
     * of (String, String).
     *
     * @param message
     *            exception message
     * @param violations
     *            A {@link Set} of {@link ConstraintViolation}
     *
     * @param <T>
     *            Generic type
     *
     * @return <T> {@link Map} of (String, String)
     */
    public static <T> InvalidEntityException valueOf(final String message, final Set<ConstraintViolation<T>> violations) {
        Map<String, String> violationsMap = new HashMap<String, String>();
        for (ConstraintViolation<T> v : violations) {
            violationsMap.put(v.getPropertyPath().toString(), v.getMessage());
        }
        return new InvalidEntityException(message, violationsMap);
    }
}
