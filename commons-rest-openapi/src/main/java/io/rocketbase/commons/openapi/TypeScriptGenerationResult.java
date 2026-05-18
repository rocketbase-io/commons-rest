package io.rocketbase.commons.openapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Result of TypeScript generation containing the generated code and type mappings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeScriptGenerationResult {

    /**
     * Generated TypeScript code
     */
    protected String typeScriptCode;

    /**
     * Mapping from Java fully qualified names to TypeScript type names.
     * Example: "io.rocketbase.commons.dto.ActivityDto" -> "ActivityDto"
     */
    protected Map<String, String> typeMapping;

    /**
     * Gets the TypeScript type name for a Java class name.
     * Handles generics by extracting the simple name.
     *
     * @param javaType Java type (can be generic like "PageableResult<ActivityDto>")
     * @return TypeScript type name
     */
    public String getTypeScriptType(String javaType) {
        if (javaType == null) return "unknown";

        // Handle generics: PageableResult<ActivityDto> -> PageableResult<ActivityDto>
        // We need to replace each Java type with its TS equivalent
        String result = javaType;

        for (Map.Entry<String, String> entry : typeMapping.entrySet()) {
            String javaFqn = entry.getKey();
            String tsName = entry.getValue();

            // Replace fully qualified name with simple name
            result = result.replace(javaFqn, tsName);
        }

        return result;
    }

    /**
     * Gets the simple TypeScript type name (without generics).
     * Example: "PageableResult<ActivityDto>" -> "PageableResult"
     */
    public String getSimpleTypeName(String javaType) {
        String tsType = getTypeScriptType(javaType);

        // Remove generics
        int ltIndex = tsType.indexOf('<');
        if (ltIndex > 0) {
            return tsType.substring(0, ltIndex);
        }

        return tsType;
    }
}
