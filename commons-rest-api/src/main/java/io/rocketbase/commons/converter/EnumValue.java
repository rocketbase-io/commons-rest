package io.rocketbase.commons.converter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Marker interface for enums that hold a custom string value.
 * <p>
 * This interface enables:
 * <ul>
 *   <li>Custom JSON serialization via {@link JsonValue}</li>
 *   <li>Flexible deserialization via {@link JsonCreator}</li>
 *   <li>Automatic Spring MVC parameter conversion from strings</li>
 *   <li>Case-insensitive value matching</li>
 * </ul>
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * public enum Status implements EnumValue {
 *     ACTIVE("active"),
 *     INACTIVE("inactive"),
 *     PENDING("pending");
 *
 *     private final String value;
 *
 *     Status(String value) {
 *         this.value = value;
 *     }
 *
 *     @JsonValue
 *     @Override
 *     public String getValue() {
 *         return value;
 *     }
 *
 *     @JsonCreator
 *     public static Status fromValue(String value) {
 *         return EnumValue.fromValue(Status.class, value);
 *     }
 * }
 * }</pre>
 *
 * <p>
 * This enum will then:
 * <ul>
 *   <li>Serialize to JSON as: "active", "inactive", "pending"</li>
 *   <li>Deserialize from JSON case-insensitively: "ACTIVE", "Active", "active" all work</li>
 *   <li>Work in Spring @RequestParam and @PathVariable: /api/users?status=active</li>
 * </ul>
 * </p>
 *
 * @see JsonValue
 * @see JsonCreator
 */
public interface EnumValue {

    /**
     * Returns the custom string value for this enum constant.
     * <p>
     * This method should be annotated with {@link JsonValue} to control JSON serialization.
     * </p>
     *
     * @return the custom string value (never null)
     */
    @JsonValue
    String getValue();

    /**
     * Helper method for implementing {@link JsonCreator} methods in enums.
     * <p>
     * This method attempts to find the enum constant by:
     * <ol>
     *   <li>Exact match with {@link #getValue()} (case-insensitive)</li>
     *   <li>Fallback: Standard enum name match via {@link Enum#valueOf(Class, String)} (uppercase)</li>
     * </ol>
     * </p>
     * <p>
     * If no match is found, returns {@code null} and logs a warning.
     * This tolerant behavior allows Spring to handle the error gracefully
     * (e.g., return 400 Bad Request with appropriate error message).
     * </p>
     *
     * @param enumClass the enum class
     * @param value the string value to convert (may be null)
     * @param <T> the enum type implementing EnumValue
     * @return the matching enum constant, or null if not found
     *
     * @example
     * <pre>{@code
     * @JsonCreator
     * public static MyEnum fromValue(String value) {
     *     return EnumValue.fromValue(MyEnum.class, value);
     * }
     * }</pre>
     */
    static <T extends Enum<T> & EnumValue> T fromValue(Class<T> enumClass, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String trimmed = value.trim();
        T[] constants = enumClass.getEnumConstants();

        if (constants == null || constants.length == 0) {
            return null;
        }

        // Try to find by custom value (case-insensitive)
        for (T constant : constants) {
            if (constant.getValue().equalsIgnoreCase(trimmed)) {
                return constant;
            }
        }

        // Fallback: Try standard enum valueOf with uppercase
        try {
            return Enum.valueOf(enumClass, trimmed.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Return null for invalid values - Spring will handle this appropriately
            return null;
        }
    }
}
