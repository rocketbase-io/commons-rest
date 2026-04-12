package io.rocketbase.commons.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * Spring converter factory that converts strings to enum constants.
 * <p>
 * This converter supports three types of enums in priority order:
 * <ol>
 *   <li><strong>EnumValue</strong> - Enums implementing {@link EnumValue} with custom values</li>
 *   <li><strong>jOOQ EnumType</strong> - jOOQ generated enums (optional, only if jOOQ is in classpath)</li>
 *   <li><strong>Standard Enum</strong> - Regular Java enums via {@link Enum#valueOf(Class, String)}</li>
 * </ol>
 * </p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Case-insensitive matching for all enum types</li>
 *   <li>Automatic registration via {@link io.rocketbase.commons.config.CommonsRestAutoConfiguration}</li>
 *   <li>Works with @RequestParam, @PathVariable, and form data binding</li>
 *   <li>Tolerant null handling with SLF4J warnings for debugging</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <p>
 * No manual configuration needed! This converter is automatically registered via Spring Boot auto-configuration.
 * </p>
 *
 * <pre>{@code
 * // In your controller:
 * @GetMapping("/users")
 * public List<User> getUsers(@RequestParam Status status) {
 *     // status is automatically converted from string to enum
 *     return userService.findByStatus(status);
 * }
 *
 * // Example URLs that work:
 * // /users?status=active
 * // /users?status=ACTIVE
 * // /users?status=Active
 * }</pre>
 *
 * @see EnumValue
 * @see io.rocketbase.commons.config.CommonsRestAutoConfiguration
 */
@Slf4j
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

    // Cache for jOOQ EnumType class (loaded once, may be null if jOOQ not in classpath)
    private static final Class<?> JOOQ_ENUM_TYPE_CLASS;

    static {
        Class<?> jooqEnumType = null;
        try {
            jooqEnumType = Class.forName("org.jooq.EnumType");
            log.debug("jOOQ EnumType found in classpath - jOOQ enum support enabled");
        } catch (ClassNotFoundException e) {
            log.debug("jOOQ EnumType not found in classpath - jOOQ enum support disabled");
        }
        JOOQ_ENUM_TYPE_CLASS = jooqEnumType;
    }

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    /**
     * Converter that handles the actual string to enum conversion.
     *
     * @param <T> the target enum type
     */
    protected static class StringToEnumConverter<T extends Enum> implements Converter<String, T> {

        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }

            String trimmed = source.trim();

            // Priority 1: Try EnumValue interface
            if (EnumValue.class.isAssignableFrom(enumType)) {
                T result = convertEnumValue(trimmed);
                if (result != null) {
                    return result;
                }
            }

            // Priority 2: Try jOOQ EnumType (if available)
            if (JOOQ_ENUM_TYPE_CLASS != null && JOOQ_ENUM_TYPE_CLASS.isAssignableFrom(enumType)) {
                T result = convertJooqEnumType(trimmed);
                if (result != null) {
                    return result;
                }
            }

            // Priority 3: Fallback to standard enum valueOf (case-insensitive)
            try {
                return (T) Enum.valueOf(enumType, trimmed.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Could not convert '{}' to enum type: {}", source, enumType.getSimpleName());
                return null;
            }
        }

        /**
         * Converts a string to an EnumValue enum.
         * Uses the EnumValue.fromValue() helper method with proper type handling.
         *
         * @param value the string value to convert
         * @return the matching enum constant, or null if not found
         */
        @SuppressWarnings("unchecked")
        private T convertEnumValue(String value) {
            T[] constants = enumType.getEnumConstants();
            if (constants == null || constants.length == 0) {
                return null;
            }

            // Iterate through all enum constants and match by value
            for (T constant : constants) {
                if (constant instanceof EnumValue) {
                    EnumValue enumValue = (EnumValue) constant;
                    if (enumValue.getValue().equalsIgnoreCase(value)) {
                        return constant;
                    }
                }
            }

            // Fallback: Try standard enum valueOf with uppercase
            try {
                return (T) Enum.valueOf(enumType, value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        /**
         * Converts a string to a jOOQ EnumType enum.
         * Uses reflection to call getLiteral() on each enum constant.
         *
         * @param value the string value to convert
         * @return the matching enum constant, or null if not found
         */
        @SuppressWarnings("unchecked")
        private T convertJooqEnumType(String value) {
            T[] constants = enumType.getEnumConstants();
            if (constants == null || constants.length == 0) {
                return null;
            }

            try {
                // jOOQ EnumType has getLiteral() method
                java.lang.reflect.Method getLiteral = enumType.getMethod("getLiteral");

                for (T constant : constants) {
                    Object literal = getLiteral.invoke(constant);
                    if (literal != null && literal.toString().equalsIgnoreCase(value)) {
                        return constant;
                    }
                }
            } catch (Exception e) {
                log.debug("Error accessing jOOQ EnumType.getLiteral() for {}: {}",
                    enumType.getSimpleName(), e.getMessage());
            }

            return null;
        }
    }
}
