package io.rocketbase.commons.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Analyzes OpenAPI specification to extract all Java class names used in the API.
 * These classes will then be converted to TypeScript using typescript-generator.
 */
@Slf4j
public class OpenApiSchemaAnalyzer {

    // Cache PageableResult class reference
    protected static final Class<?> PAGEABLE_RESULT_CLASS;
    protected static final Class<?> PAGEABLE_RESULT_IMPL_CLASS;
    protected static final Class<?> PAGEABLE_RESULT_WITH_META_CLASS;

    static {
        Class<?> pageableResultClass = null;
        Class<?> pageableResultImplClass = null;
        Class<?> pageableResultWithMetaClass = null;
        try {
            pageableResultClass = Class.forName("io.rocketbase.commons.dto.PageableResult");
            pageableResultImplClass = Class.forName("io.rocketbase.commons.dto.PageableResultImpl");
            pageableResultWithMetaClass = Class.forName("io.rocketbase.commons.dto.PageableResultWithMeta");
        } catch (ClassNotFoundException e) {
            log.warn("PageableResult classes not found in classpath - filtering disabled");
        }
        PAGEABLE_RESULT_CLASS = pageableResultClass;
        PAGEABLE_RESULT_IMPL_CLASS = pageableResultImplClass;
        PAGEABLE_RESULT_WITH_META_CLASS = pageableResultWithMetaClass;
    }

    protected final List<TypeScriptGeneratorCustomizer> customizers;

    /**
     * Constructor with customizers support.
     *
     * @param customizers List of customizers to apply during class extraction
     */
    public OpenApiSchemaAnalyzer(List<TypeScriptGeneratorCustomizer> customizers) {
        this.customizers = customizers != null ? customizers : Collections.emptyList();
    }

    /**
     * Default constructor without customizers (backward compatible).
     */
    public OpenApiSchemaAnalyzer() {
        this(Collections.emptyList());
    }

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

        // Convert class names to Class objects and filter
        Set<Class<?>> classes = new HashSet<>();
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);

                // Filter out classes that shouldn't be generated
                if (shouldExcludeFromGeneration(clazz)) {
                    log.debug("Excluding class from TypeScript generation: {}", className);
                    continue;
                }

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
    protected void extractClassNamesFromGenericType(String typeStr, Set<String> classNames) {
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
                // Try to load and check if should be excluded
                try {
                    Class<?> clazz = Class.forName(part);
                    if (shouldExcludeFromGeneration(clazz)) {
                        log.debug("Excluding class from extraction: {}", part);
                        continue;
                    }
                } catch (ClassNotFoundException e) {
                    // Class not loadable here - will be filtered later in extractAllUsedClasses
                }

                classNames.add(part);
            }
        }
    }

    /**
     * Determines if a class should be excluded from TypeScript generation.
     * <p>
     * This method checks:
     * <ul>
     *   <li>If the class is exactly one of the PageableResult types (fast exact match)</li>
     *   <li>If the class implements or extends PageableResult (catches custom implementations)</li>
     *   <li>If any registered TypeScriptGeneratorCustomizer excludes the class</li>
     * </ul>
     * </p>
     * <p>
     * PageableResult types are excluded because they are provided by @rocketbase/commons-rest-client
     * and should not be generated.
     * </p>
     * <p>
     * <strong>Note:</strong> This method can be overridden or extended via TypeScriptGeneratorCustomizer
     * to add custom exclusion logic.
     * </p>
     *
     * @param clazz The class to check
     * @return true if the class should be excluded from generation
     */
    protected boolean shouldExcludeFromGeneration(Class<?> clazz) {
        if (clazz == null) return false;

        // Fast path: exact match with known PageableResult classes
        if (clazz == PAGEABLE_RESULT_CLASS ||
                clazz == PAGEABLE_RESULT_IMPL_CLASS ||
                clazz == PAGEABLE_RESULT_WITH_META_CLASS) {
            return true;
        }

        // Slow path: check if class implements/extends PageableResult
        // This catches custom implementations like PageableResultGeo, PageableResultExtended, etc.
        if (PAGEABLE_RESULT_CLASS != null && PAGEABLE_RESULT_CLASS.isAssignableFrom(clazz)) {
            return true;
        }

        // Check customizers
        if (!customizers.isEmpty()) {
            for (TypeScriptGeneratorCustomizer customizer : customizers) {
                if (customizer.shouldExcludeClass(clazz)) {
                    log.debug("Class {} excluded by customizer: {}",
                            clazz.getName(), customizer.getClass().getSimpleName());
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean isPrimitiveOrCommon(String type) {
        Set<String> skip = Set.of(
                "int", "long", "double", "float", "boolean", "char", "byte", "short",
                "Integer", "Long", "Double", "Float", "Boolean", "Character", "Byte", "Short",
                "String", "Object", "Void", "void",
                "?", "E", "T", "K", "V" // Generic type parameters
        );
        return skip.contains(type);
    }

    protected void extractFromPathItem(PathItem pathItem, Set<String> classNames) {
        if (pathItem.getGet() != null) extractFromOperation(pathItem.getGet(), classNames);
        if (pathItem.getPost() != null) extractFromOperation(pathItem.getPost(), classNames);
        if (pathItem.getPut() != null) extractFromOperation(pathItem.getPut(), classNames);
        if (pathItem.getDelete() != null) extractFromOperation(pathItem.getDelete(), classNames);
        if (pathItem.getPatch() != null) extractFromOperation(pathItem.getPatch(), classNames);
    }

    protected void extractFromOperation(Operation operation, Set<String> classNames) {
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
    protected void extractFromSchema(Schema schema, Set<String> classNames) {
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
