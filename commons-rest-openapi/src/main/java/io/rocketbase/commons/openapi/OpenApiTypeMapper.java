package io.rocketbase.commons.openapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps Java types to TypeScript type names using the typescript-generator results.
 * NO string manipulation - just simple mapping!
 */
@Slf4j
@RequiredArgsConstructor
public class OpenApiTypeMapper implements TypeScriptTypeConverter {

    private final TypeScriptGenerationResult generationResult;

    /**
     * Converts a Java generic type to TypeScript type using the generated type mappings.
     * Example: "io.rocketbase.commons.dto.PageableResult<io.rocketbase.commons.dto.ActivityDto>"
     *       -> "PageableResult<ActivityDto>"
     */
    public String toTypeScript(String javaType) {
        if (javaType == null || javaType.isEmpty()) {
            return "unknown";
        }

        // Handle void
        if (javaType.equalsIgnoreCase("void") || javaType.equalsIgnoreCase("java.lang.Void")) {
            return "void";
        }

        // Use the generation result to map types
        String result = generationResult.getTypeScriptType(javaType);

        // Handle additional primitives and common types
        result = handlePrimitives(result);
        result = handleCollections(result);
        result = handleOptionals(result);

        return result;
    }

    /**
     * Gets the simple type name without generics.
     */
    public String getSimpleTypeName(String javaType) {
        String tsType = toTypeScript(javaType);
        int ltIndex = tsType.indexOf('<');
        if (ltIndex > 0) {
            return tsType.substring(0, ltIndex);
        }
        return tsType;
    }

    /**
     * Extracts all import types from a TypeScript type string.
     * Example: "PageableResult<ActivityDto>" -> ["PageableResult", "ActivityDto"]
     */
    public Set<String> extractImportTypes(String tsType) {
        Set<String> types = new HashSet<>();

        if (tsType == null || tsType.isEmpty()) {
            return types;
        }

        // Remove array brackets, Record, Promise, etc.
        String cleaned = tsType
            .replaceAll("\\[\\]", "")
            .replaceAll("Record<[^,]+,\\s*", "")
            .replaceAll("Promise<", "")
            .replaceAll("\\|\\s*null", "")
            .replaceAll("[<>,]", " ");

        String[] parts = cleaned.split("\\s+");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            if (isNativeType(part)) continue;

            // Extract simple name (e.g., "io.rocketbase.commons.dto.Activity" -> "Activity")
            // This handles Java package names that typescript-generator may generate
            String simpleName = part.contains(".") ? part.substring(part.lastIndexOf('.') + 1) : part;
            types.add(simpleName);
        }

        return types;
    }

    /**
     * Handle primitive types mapping.
     */
    private String handlePrimitives(String type) {
        // Java primitives to TypeScript
        Map<String, String> primitives = Map.ofEntries(
            Map.entry("int", "number"),
            Map.entry("Integer", "number"),
            Map.entry("long", "number"),
            Map.entry("Long", "number"),
            Map.entry("double", "number"),
            Map.entry("Double", "number"),
            Map.entry("float", "number"),
            Map.entry("Float", "number"),
            Map.entry("short", "number"),
            Map.entry("Short", "number"),
            Map.entry("byte", "number"),
            Map.entry("Byte", "number"),
            Map.entry("boolean", "boolean"),
            Map.entry("Boolean", "boolean"),
            Map.entry("String", "string"),
            Map.entry("UUID", "string"),
            Map.entry("LocalDate", "string"),
            Map.entry("LocalDateTime", "string"),
            Map.entry("LocalTime", "string"),
            Map.entry("Instant", "string"),
            Map.entry("OffsetDateTime", "string"),
            Map.entry("ZonedDateTime", "string"),
            Map.entry("Date", "string"),
            Map.entry("Object", "any")
        );

        for (Map.Entry<String, String> entry : primitives.entrySet()) {
            // Use word boundaries to avoid partial replacements
            type = type.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
        }

        return type;
    }

    /**
     * Handle Java collections to TypeScript arrays.
     */
    private String handleCollections(String type) {
        // List<T>, Set<T>, Collection<T> -> T[]
        Pattern collectionPattern = Pattern.compile("(List|Set|Collection|ArrayList|HashSet)<([^>]+)>");
        Matcher matcher = collectionPattern.matcher(type);
        if (matcher.find()) {
            String innerType = matcher.group(2);
            return innerType + "[]";
        }

        return type;
    }

    /**
     * Handle Java Optional to TypeScript union.
     */
    private String handleOptionals(String type) {
        // Optional<T> -> T | null
        Pattern optionalPattern = Pattern.compile("Optional<([^>]+)>");
        Matcher matcher = optionalPattern.matcher(type);
        if (matcher.find()) {
            String innerType = matcher.group(1);
            return innerType + " | null";
        }

        return type;
    }

    /**
     * Check if a type is a native TypeScript type.
     */
    private boolean isNativeType(String type) {
        Set<String> nativeTypes = Set.of(
            "string", "number", "boolean", "any", "unknown", "void", "null", "undefined",
            "Promise", "Record", "Map", "Set", "Array"
        );
        return nativeTypes.contains(type);
    }

    /**
     * Get native TypeScript types (for filtering imports).
     */
    public Set<String> getNativeTypes() {
        return Set.of(
            "string", "number", "boolean", "any", "unknown", "void", "null", "undefined"
        );
    }
}
