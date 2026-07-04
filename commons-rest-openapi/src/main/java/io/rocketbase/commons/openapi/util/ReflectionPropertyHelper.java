package io.rocketbase.commons.openapi.util;

import org.springframework.boot.data.autoconfigure.web.DataWebProperties;

import java.lang.reflect.Field;

/**
 * Utility class for setting properties via reflection.
 * Used by {@link io.rocketbase.commons.openapi.StandaloneClientGenerator}
 * and {@link io.rocketbase.commons.openapi.GenerateTypescriptClientMojo}
 * to configure properties without Spring context.
 */
public class ReflectionPropertyHelper {

    /**
     * Sets a field value using reflection.
     *
     * @param target    The target object
     * @param fieldName The field name
     * @param value     The value to set
     * @throws RuntimeException if field cannot be set
     */
    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field '" + fieldName + "' on " + target.getClass().getSimpleName(), e);
        }
    }

    /**
     * Creates and configures DataWebProperties with default pagination settings.
     *
     * @return Configured DataWebProperties
     */
    public static DataWebProperties createDefaultSpringDataProperties() {
        try {
            DataWebProperties props = DataWebProperties.class.getDeclaredConstructor().newInstance();
            DataWebProperties.Pageable pageable = DataWebProperties.Pageable.class.getDeclaredConstructor().newInstance();
            DataWebProperties.Sort sort = DataWebProperties.Sort.class.getDeclaredConstructor().newInstance();

            setField(pageable, "pageParameter", "page");
            setField(pageable, "sizeParameter", "size");
            setField(sort, "sortParameter", "sort");
            setField(props, "pageable", pageable);
            setField(props, "sort", sort);

            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create DataWebProperties", e);
        }
    }
}
