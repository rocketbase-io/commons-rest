package io.rocketbase.commons.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes OpenAPI specification to extract all Java class names used in the API.
 * These classes will then be converted to TypeScript using typescript-generator.
 */
@Slf4j
public class OpenApiSchemaAnalyzer {

    /**
     * Extracts all Java class names from OpenAPI spec.
     * Includes: DTOs from request/response bodies, parameter types, return types.
     *
     * @param openAPI The OpenAPI specification
     * @return Set of fully qualified Java class names
     */
    public Set<Class<?>> extractAllUsedClasses(OpenAPI openAPI) {
        Set<String> classNames = new HashSet<>();

        // Extract from paths/operations
        if (openAPI.getPaths() != null) {
            openAPI.getPaths().forEach((path, pathItem) -> {
                extractFromPathItem(pathItem, classNames);
            });
        }

        // Extract from components/schemas (if available)
        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            openAPI.getComponents().getSchemas().forEach((name, schema) -> {
                extractFromSchema(schema, classNames);
            });
        }

        // Convert class names to Class objects
        Set<Class<?>> classes = new HashSet<>();
        for (String className : classNames) {
            try {
                // Try to load the class
                Class<?> clazz = Class.forName(className);
                classes.add(clazz);
                log.debug("Loaded class for TypeScript generation: {}", className);
            } catch (ClassNotFoundException e) {
                log.warn("Could not load class: {} - skipping", className);
            }
        }

        return classes;
    }

    /**
     * Extracts class names from OpenAPI custom extensions.
     * Our custom annotations add x-generic-return-type, x-parameter-types, etc.
     */
    public Set<String> extractFromExtensions(OpenAPI openAPI) {
        Set<String> classNames = new HashSet<>();

        if (openAPI.getPaths() != null) {
            openAPI.getPaths().forEach((path, pathItem) -> {
                List<Operation> operations = Arrays.asList(
                    pathItem.getGet(),
                    pathItem.getPost(),
                    pathItem.getPut(),
                    pathItem.getDelete(),
                    pathItem.getPatch()
                );

                for (Operation op : operations) {
                    if (op == null || op.getExtensions() == null) continue;

                    // Extract return type
                    Object returnType = op.getExtensions().get(OpenApiCustomExtractor.GENERIC_RETURN_TYPE);
                    if (returnType instanceof String) {
                        extractClassNamesFromGenericType((String) returnType, classNames);
                    }

                    // Extract parameter types
                    Object paramTypes = op.getExtensions().get(OpenApiCustomExtractor.PARAMETER_TYPES);
                    if (paramTypes instanceof List) {
                        for (Object pt : (List<?>) paramTypes) {
                            if (pt instanceof String) {
                                extractClassNamesFromGenericType((String) pt, classNames);
                            }
                        }
                    }

                    // Extract request body type
                    Object reqBodyType = op.getExtensions().get(OpenApiCustomExtractor.REQUEST_BODY_TYPE_NAME);
                    if (reqBodyType instanceof String) {
                        extractClassNamesFromGenericType((String) reqBodyType, classNames);
                    }
                }
            });
        }

        return classNames;
    }

    /**
     * Extracts class names from a generic type string like "PageableResult<ActivityDto>".
     * Handles nested generics: "Map<String, List<ActivityDto>>".
     */
    private void extractClassNamesFromGenericType(String typeStr, Set<String> classNames) {
        if (typeStr == null || typeStr.isEmpty()) return;

        // Remove generic brackets and split
        String cleaned = typeStr.replaceAll("[<>]", " ");
        String[] parts = cleaned.split("[,\\s]+");

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            // Filter out primitive types and common Java classes
            if (isPrimitiveOrCommon(part)) continue;

            // If it looks like a fully qualified class name (contains .)
            if (part.contains(".") && !part.startsWith("java.lang.") && !part.startsWith("java.util.")) {
                classNames.add(part);
            }
        }
    }

    private boolean isPrimitiveOrCommon(String type) {
        Set<String> skip = Set.of(
            "int", "long", "double", "float", "boolean", "char", "byte", "short",
            "Integer", "Long", "Double", "Float", "Boolean", "Character", "Byte", "Short",
            "String", "Object", "Void", "void",
            "?", "E", "T", "K", "V" // Generic type parameters
        );
        return skip.contains(type);
    }

    private void extractFromPathItem(PathItem pathItem, Set<String> classNames) {
        if (pathItem.getGet() != null) extractFromOperation(pathItem.getGet(), classNames);
        if (pathItem.getPost() != null) extractFromOperation(pathItem.getPost(), classNames);
        if (pathItem.getPut() != null) extractFromOperation(pathItem.getPut(), classNames);
        if (pathItem.getDelete() != null) extractFromOperation(pathItem.getDelete(), classNames);
        if (pathItem.getPatch() != null) extractFromOperation(pathItem.getPatch(), classNames);
    }

    private void extractFromOperation(Operation operation, Set<String> classNames) {
        // Request body
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            operation.getRequestBody().getContent().forEach((mediaType, content) -> {
                extractFromSchema(content.getSchema(), classNames);
            });
        }

        // Parameters
        if (operation.getParameters() != null) {
            for (Parameter param : operation.getParameters()) {
                if (param.getSchema() != null) {
                    extractFromSchema(param.getSchema(), classNames);
                }
            }
        }

        // Responses
        if (operation.getResponses() != null) {
            operation.getResponses().forEach((code, response) -> {
                if (response.getContent() != null) {
                    response.getContent().forEach((mediaType, content) -> {
                        extractFromSchema(content.getSchema(), classNames);
                    });
                }
            });
        }
    }

    @SuppressWarnings("rawtypes")
    private void extractFromSchema(Schema schema, Set<String> classNames) {
        if (schema == null) return;

        // Check for $ref
        if (schema.get$ref() != null) {
            String ref = schema.get$ref();
            // OpenAPI refs are like "#/components/schemas/ActivityDto"
            // We need to extract "ActivityDto" and try to resolve it
            String schemaName = ref.substring(ref.lastIndexOf("/") + 1);
            // This is just the schema name, not the Java class
            // We'll rely on extensions for Java class names
        }

        // Check extensions for Java type info
        if (schema.getExtensions() != null) {
            Object javaType = schema.getExtensions().get("x-java-type");
            if (javaType instanceof String) {
                extractClassNamesFromGenericType((String) javaType, classNames);
            }
        }

        // Recursive: array items
        if (schema.getItems() != null) {
            extractFromSchema(schema.getItems(), classNames);
        }

        // Recursive: properties
        if (schema.getProperties() != null) {
            schema.getProperties().forEach((name, propSchema) -> {
                extractFromSchema((Schema) propSchema, classNames);
            });
        }
    }
}
