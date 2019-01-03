package com.alipay.sofa.ark.common.util;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class AssertUtils {

    /**
     * Validate current object must not be null
     *
     * @param instance object instance
     * @param msg error message
     * @throws IllegalArgumentException if object instance is null
     */
    public static void assertNotNull(Object instance, String msg) {
        if (instance == null) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * <p>Validate that the argument condition is {@code true}; otherwise
     * throwing an exception with the specified message. This method is useful when
     * validating according to an arbitrary boolean expression, such as validating a
     * primitive number or using your own custom validation expression.</p>
     *
     * @param expression  the boolean expression to check
     * @param message  the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(final boolean expression, final String message,
                              final Object... values) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(message, values));
        }
    }

    /**
     * <p>Validate that the argument condition is {@code false}; otherwise
     * throwing an exception with the specified message. This method is useful when
     * validating according to an arbitrary boolean expression, such as validating a
     * primitive number or using your own custom validation expression.</p>
     *
     * @param expression  the boolean expression to check
     * @param message  the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isFalse(final boolean expression, final String message,
                               final Object... values) {
        if (expression) {
            throw new IllegalArgumentException(String.format(message, values));
        }
    }
}