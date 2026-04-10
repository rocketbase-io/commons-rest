package io.rocketbase.commons.openapi;

import java.util.Set;

/**
 * Simple interface for converting Java types to TypeScript types.
 * Replaces the complex OpenApiConverter with a minimal, clean API.
 */
public interface TypeScriptTypeConverter {

    /**
     * Converts a Java type (possibly generic) to TypeScript type.
     * Example: "io.rocketbase.commons.dto.PageableResult<ActivityDto>" -> "PageableResult<ActivityDto>"
     *
     * @param javaType Java type (fully qualified or simple name)
     * @return TypeScript type
     */
    String toTypeScript(String javaType);

    /**
     * Gets the simple type name without generics.
     * Example: "PageableResult<ActivityDto>" -> "PageableResult"
     */
    String getSimpleTypeName(String javaType);

    /**
     * Extracts all types that need to be imported.
     * Filters out native TypeScript types.
     *
     * @param tsType TypeScript type (can contain generics)
     * @return Set of type names to import
     */
    Set<String> extractImportTypes(String tsType);

    /**
     * Gets native TypeScript types (for filtering).
     */
    Set<String> getNativeTypes();

    /**
     * Gets the import path for model types.
     * Default: "../../model"
     */
    default String getModelImportPath() {
        return "../../model";
    }
}
