package io.rocketbase.commons.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.constraints.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates Zod validation schemas for mutation DTOs (Commands).
 * Maps Java validation annotations (@NotNull, @Size, @Min, @Max, etc.) to Zod validators.
 * Uses the same requiredAnnotations configuration as TypeScriptModelGenerator for consistency.
 */
@Slf4j
@RequiredArgsConstructor
public class ZodSchemaGenerator {

    private final OpenAPI openAPI;
    private final TypeScriptModelGenerator.TypeScriptGeneratorConfig config;

    /**
     * Generates Zod schemas for all mutation commands (POST, PUT, PATCH, DELETE request bodies).
     *
     * @param outputFile Path to the output .ts file (e.g., "model/zod-schemas.ts")
     * @return Generated TypeScript code with Zod schemas
     */
    public String generateZodSchemas(Path outputFile) {
        log.info("Generating Zod schemas for mutation commands");

        // 1. Extract all mutation request body classes
        Set<String> mutationClasses = extractMutationClasses();
        log.info("Found {} mutation classes", mutationClasses.size());

        // 2. Load classes
        Set<Class<?>> classes = loadClasses(mutationClasses);

        // 3. Generate Zod schemas
        StringBuilder output = new StringBuilder();
        output.append("// Auto-generated Zod schemas for mutation commands\n");
        output.append("// DO NOT EDIT MANUALLY\n\n");
        output.append("import { z } from \"zod\";\n");
        output.append("import type * as Types from \"./types\";\n\n");

        for (Class<?> clazz : classes) {
            String zodSchema = generateZodSchemaForClass(clazz);
            output.append(zodSchema).append("\n\n");
        }

        // Write to file
        try {
            java.nio.file.Files.createDirectories(outputFile.getParent());
            java.nio.file.Files.writeString(outputFile, output.toString());
            log.info("Successfully generated Zod schemas: {}", outputFile);
        } catch (Exception e) {
            log.error("Failed to write Zod schemas to file: {}", outputFile, e);
            throw new RuntimeException("Failed to generate Zod schemas", e);
        }

        return output.toString();
    }

    /**
     * Extracts all classes used in mutation request bodies (POST, PUT, PATCH, DELETE).
     */
    private Set<String> extractMutationClasses() {
        Set<String> classes = new HashSet<>();

        if (openAPI.getPaths() == null) {
            return classes;
        }

        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            PathItem pathItem = pathEntry.getValue();

            // Check mutation operations (filter out nulls using Stream)
            List<Operation> operations = java.util.stream.Stream.of(
                pathItem.getPost(),
                pathItem.getPut(),
                pathItem.getPatch(),
                pathItem.getDelete()
            ).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toList());

            for (Operation operation : operations) {
                if (operation.getRequestBody() != null) {
                    extractClassFromRequestBody(operation.getRequestBody(), classes);
                }
            }
        }

        return classes;
    }

    private void extractClassFromRequestBody(RequestBody requestBody, Set<String> classes) {
        if (requestBody.getContent() == null) {
            return;
        }

        requestBody.getContent().forEach((mediaType, content) -> {
            Schema schema = content.getSchema();
            if (schema != null) {
                // Check for x-java-type extension
                Object javaType = schema.getExtensions() != null ? schema.getExtensions().get("x-java-type") : null;
                if (javaType != null) {
                    classes.add(javaType.toString());
                }

                // Also check schema reference
                if (schema.get$ref() != null) {
                    String ref = schema.get$ref();
                    String className = ref.substring(ref.lastIndexOf("/") + 1);
                    // Try to find the full class name from components
                    if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
                        Schema componentSchema = openAPI.getComponents().getSchemas().get(className);
                        if (componentSchema != null && componentSchema.getExtensions() != null) {
                            Object componentJavaType = componentSchema.getExtensions().get("x-java-type");
                            if (componentJavaType != null) {
                                classes.add(componentJavaType.toString());
                            }
                        }
                    }
                }
            }
        });
    }

    private Set<Class<?>> loadClasses(Set<String> classNames) {
        Set<Class<?>> classes = new HashSet<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className, false, classLoader);
                // Only include "Cmd" classes (commands)
                if (clazz.getSimpleName().endsWith("Cmd")) {
                    classes.add(clazz);
                    log.debug("Loaded command class: {}", className);
                }
            } catch (ClassNotFoundException e) {
                log.warn("Could not load class: {} - skipping", className);
            }
        }

        return classes;
    }

    /**
     * Generates a Zod schema for a single class.
     */
    private String generateZodSchemaForClass(Class<?> clazz) {
        StringBuilder schema = new StringBuilder();
        String schemaName = clazz.getSimpleName() + "Schema";

        schema.append("/**\n");
        schema.append(" * Zod schema for ").append(clazz.getSimpleName()).append("\n");
        schema.append(" * Auto-generated from Java class: ").append(clazz.getName()).append("\n");
        schema.append(" */\n");
        schema.append("export const ").append(schemaName).append(" = z.object({\n");

        List<String> fieldSchemas = new ArrayList<>();

        for (Field field : getAllFields(clazz)) {
            String fieldSchema = generateFieldSchema(field);
            if (fieldSchema != null) {
                fieldSchemas.add(fieldSchema);
            }
        }

        schema.append(String.join(",\n", fieldSchemas));
        schema.append("\n");

        schema.append("});\n\n");
        schema.append("export type ").append(clazz.getSimpleName()).append(" = z.infer<typeof ").append(schemaName).append(">;");

        return schema.toString();
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields;
    }

    /**
     * Generates Zod validation schema for a single field.
     */
    private String generateFieldSchema(Field field) {
        // Skip static and transient fields
        if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
            java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
            return null;
        }

        String fieldName = field.getName();
        String zodType = mapFieldTypeToZod(field);

        // Collect all validations
        List<String> validations = new ArrayList<>();

        // Check if field is required based on configured requiredAnnotations
        boolean isRequired = config.getRequiredAnnotations().stream()
            .anyMatch(field::isAnnotationPresent);

        // @NotEmpty - for collections/strings, implies min(1)
        if (field.isAnnotationPresent(NotEmpty.class)) {
            validations.add(".min(1)");
        }

        // @NotBlank - for strings, implies non-empty after trimming
        if (field.isAnnotationPresent(NotBlank.class)) {
            validations.add(".trim().min(1)");
        }

        // @Size
        if (field.isAnnotationPresent(Size.class)) {
            Size size = field.getAnnotation(Size.class);
            if (size.min() > 0) {
                validations.add(".min(" + size.min() + ")");
            }
            if (size.max() < Integer.MAX_VALUE) {
                validations.add(".max(" + size.max() + ")");
            }
        }

        // @Min
        if (field.isAnnotationPresent(Min.class)) {
            Min min = field.getAnnotation(Min.class);
            validations.add(".min(" + min.value() + ")");
        }

        // @Max
        if (field.isAnnotationPresent(Max.class)) {
            Max max = field.getAnnotation(Max.class);
            validations.add(".max(" + max.value() + ")");
        }

        // @DecimalMin
        if (field.isAnnotationPresent(DecimalMin.class)) {
            DecimalMin decimalMin = field.getAnnotation(DecimalMin.class);
            double minValue = Double.parseDouble(decimalMin.value());
            if (decimalMin.inclusive()) {
                validations.add(".min(" + minValue + ")");
            } else {
                validations.add(".gt(" + minValue + ")");
            }
        }

        // @DecimalMax
        if (field.isAnnotationPresent(DecimalMax.class)) {
            DecimalMax decimalMax = field.getAnnotation(DecimalMax.class);
            double maxValue = Double.parseDouble(decimalMax.value());
            if (decimalMax.inclusive()) {
                validations.add(".max(" + maxValue + ")");
            } else {
                validations.add(".lt(" + maxValue + ")");
            }
        }

        // @Digits - validates number of digits
        if (field.isAnnotationPresent(Digits.class)) {
            Digits digits = field.getAnnotation(Digits.class);
            // Zod doesn't have a direct equivalent, but we can use a regex or refine
            // For simplicity, we'll add a comment or use multipleOf for integer part
            // Here we'll use a custom refinement approach with regex
            int integerDigits = digits.integer();
            int fractionDigits = digits.fraction();

            if (fractionDigits == 0) {
                // Integer only: use regex to limit digits
                String digitPattern = "^-?\\\\d{1," + integerDigits + "}$";
                validations.add(".regex(/" + digitPattern + "/)");
            } else {
                // Decimal: limit integer and fraction parts
                String digitPattern = "^-?\\\\d{0," + integerDigits + "}(\\\\.\\\\d{0," + fractionDigits + "})?$";
                validations.add(".regex(/" + digitPattern + "/)");
            }
        }

        // @Email
        if (field.isAnnotationPresent(Email.class)) {
            validations.add(".email()");
        }

        // @Pattern
        if (field.isAnnotationPresent(Pattern.class)) {
            Pattern pattern = field.getAnnotation(Pattern.class);
            // In JavaScript regex literals, backslashes are already escaped in the source
            // So we don't need to double-escape them
            String regex = pattern.regexp();
            validations.add(".regex(/" + regex + "/)");
        }

        // @Positive, @PositiveOrZero
        if (field.isAnnotationPresent(Positive.class)) {
            validations.add(".positive()");
        }
        if (field.isAnnotationPresent(PositiveOrZero.class)) {
            validations.add(".nonnegative()");
        }

        // @Negative, @NegativeOrZero
        if (field.isAnnotationPresent(Negative.class)) {
            validations.add(".negative()");
        }
        if (field.isAnnotationPresent(NegativeOrZero.class)) {
            validations.add(".nonpositive()");
        }

        StringBuilder fieldSchema = new StringBuilder();
        fieldSchema.append("  ").append(fieldName).append(": ").append(zodType);

        // Add validations
        for (String validation : validations) {
            fieldSchema.append(validation);
        }

        // Make optional/nullable if not required
        if (!isRequired) {
            fieldSchema.append(".nullish()");
        }

        return fieldSchema.toString();
    }

    /**
     * Maps a field's type to Zod, considering generic type information.
     */
    private String mapFieldTypeToZod(Field field) {
        Class<?> type = field.getType();

        // Check if it's a Collection/List/Set with generic type parameter
        if (Collection.class.isAssignableFrom(type) || List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) {
            // Try to extract generic type parameter
            java.lang.reflect.Type genericType = field.getGenericType();
            if (genericType instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType paramType = (java.lang.reflect.ParameterizedType) genericType;
                java.lang.reflect.Type[] typeArgs = paramType.getActualTypeArguments();
                if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?>) {
                    Class<?> elementType = (Class<?>) typeArgs[0];
                    String elementZodType = mapTypeToZod(elementType);
                    return "z.array(" + elementZodType + ")";
                }
            }
            // Fallback to z.any() if we can't determine the type
            return "z.array(z.any())";
        }

        // For all other types, use the regular mapping
        return mapTypeToZod(type);
    }

    /**
     * Maps Java types to Zod types.
     */
    private String mapTypeToZod(Class<?> type) {
        String typeName = type.getSimpleName();

        // Primitives and common types
        if (type == String.class || type == CharSequence.class) {
            return "z.string()";
        }
        if (type == int.class || type == Integer.class ||
            type == long.class || type == Long.class ||
            type == short.class || type == Short.class ||
            type == byte.class || type == Byte.class) {
            return "z.number().int()";
        }
        if (type == double.class || type == Double.class ||
            type == float.class || type == Float.class) {
            return "z.number()";
        }
        if (type == boolean.class || type == Boolean.class) {
            return "z.boolean()";
        }
        if (type == Date.class || typeName.contains("Date") || typeName.contains("Time") || typeName.equals("Instant")) {
            return "z.coerce.date()";
        }

        // Arrays
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            return "z.array(" + mapTypeToZod(componentType) + ")";
        }

        // Collections
        if (Collection.class.isAssignableFrom(type) || List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) {
            return "z.array(z.any())"; // Generic type info lost at runtime
        }

        // Map
        if (Map.class.isAssignableFrom(type)) {
            return "z.record(z.string(), z.any())";
        }

        // Enums
        if (type.isEnum()) {
            return "z.nativeEnum(" + typeName + ")";
        }

        // Complex objects - reference the TypeScript type
        return "z.custom<Types." + typeName + ">()";
    }
}
