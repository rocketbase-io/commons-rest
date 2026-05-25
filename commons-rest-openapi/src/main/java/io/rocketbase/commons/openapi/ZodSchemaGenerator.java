package io.rocketbase.commons.openapi;

import com.fasterxml.jackson.annotation.JsonValue;
import io.rocketbase.commons.generator.ZodSchema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;

/**
 * Generates Zod validation schemas for mutation DTOs (Commands).
 * Maps Java validation annotations (@NotNull, @Size, @Min, @Max, etc.) to Zod validators.
 * Uses the same requiredAnnotations configuration as TypeScriptModelGenerator for consistency.
 *
 * <p>Pass the {@link TypeScriptGenerationResult} from {@link TypeScriptModelGenerator}
 * to get correct schema output for fields whose Java type maps to a TS primitive (e.g.
 * {@code Tsid → string}) or whose enum type lives in {@code ./types}: the generator then
 * emits {@code z.string()} / {@code z.nativeEnum(Types.X)} instead of the fallback
 * {@code z.custom<Types.X>()} that references a non-existent or unimported name.
 */
@Slf4j
public class ZodSchemaGenerator {

    /** Set of TypeScript primitives the generator special-cases when emitting field schemas. */
    private static final Set<String> TS_PRIMITIVES = Set.of("string", "number", "boolean", "any", "unknown");

    protected final OpenAPI openAPI;
    protected final TypeScriptModelGenerator.TypeScriptGeneratorConfig config;
    /**
     * Java FQN → TypeScript type name, as produced by {@link TypeScriptModelGenerator}.
     * Empty when the no-arg constructor variant is used (back-compat).
     */
    protected final Map<String, String> typeMapping;

    /**
     * Per-run resolution context, populated at the start of {@link #generateZodSchemas(Path)}.
     * {@code schemaClasses} are the complex object types that get their own {@code …Schema}
     * const (so a field referencing them emits {@code XSchema} instead of an unvalidated
     * {@code z.custom<Types.X>()}); {@code emissionOrder} records the final output position
     * of each so the field mapper can decide when a forward reference needs {@code z.lazy(...)}.
     * Both are instance state because the generator is used single-threaded per run.
     */
    protected Map<String, Class<?>> schemaClasses = Collections.emptyMap();
    protected Map<String, Integer> emissionOrder = Collections.emptyMap();
    /** Emission index of the schema currently being rendered (for forward-reference detection). */
    protected int currentEmissionIndex = -1;

    public ZodSchemaGenerator(OpenAPI openAPI, TypeScriptModelGenerator.TypeScriptGeneratorConfig config) {
        this(openAPI, config, Collections.emptyMap());
    }

    public ZodSchemaGenerator(
            OpenAPI openAPI,
            TypeScriptModelGenerator.TypeScriptGeneratorConfig config,
            TypeScriptGenerationResult tsGenerationResult) {
        this(openAPI, config, tsGenerationResult != null ? tsGenerationResult.getTypeMapping() : Collections.emptyMap());
    }

    private ZodSchemaGenerator(
            OpenAPI openAPI,
            TypeScriptModelGenerator.TypeScriptGeneratorConfig config,
            Map<String, String> typeMapping) {
        this.openAPI = openAPI;
        this.config = config;
        this.typeMapping = typeMapping != null ? typeMapping : Collections.emptyMap();
    }

    /**
     * Generates Zod schemas for all mutation commands (POST, PUT, PATCH, DELETE request bodies).
     *
     * @param outputFile Path to the output .ts file (e.g., "model/zod-schemas.ts")
     * @return Generated TypeScript code with Zod schemas
     */
    public String generateZodSchemas(Path outputFile) {
        log.info("Generating Zod schemas for mutation commands");

        // 1. Extract the root mutation request-body classes (POST/PUT/PATCH/DELETE).
        Set<String> mutationClasses = extractMutationClasses();
        log.info("Found {} mutation classes", mutationClasses.size());
        Set<Class<?>> roots = new LinkedHashSet<>(loadClasses(mutationClasses));

        // 1b. Add explicit @ZodSchema(INCLUDE) roots. These are types a project wants a Zod
        //     schema for even though no mutation endpoint references them. Discovery is
        //     limited to types already known to the TypeScript generator (typeMapping) to
        //     avoid a full classpath scan — i.e. the type must appear somewhere in the API
        //     surface (a response, a query param, …). Types referenced nowhere in the API
        //     are out of scope for this opt-in.
        Set<Class<?>> includeRoots = discoverIncludeRoots();
        if (!includeRoots.isEmpty()) {
            log.info("Adding {} @ZodSchema(INCLUDE) root(s)", includeRoots.size());
            roots.addAll(includeRoots);
        }

        // 2. Transitively discover every complex object type reachable through the roots'
        //    field graph (nested DTOs + Cmd alike). Each of these gets its own …Schema so
        //    nested validation is preserved instead of degrading to an unvalidated
        //    z.custom<Types.X>(). Types annotated @ZodSchema(IGNORE) are pruned.
        Set<Class<?>> allSchemaClasses = discoverSchemaClasses(roots);
        this.schemaClasses = new LinkedHashMap<>();
        for (Class<?> c : allSchemaClasses) {
            this.schemaClasses.put(c.getName(), c);
        }

        // 3. Order the schemas so a referenced schema is declared before its referrer where
        //    possible (const has no hoisting). Real cycles are detected and broken later via
        //    z.lazy(...) at the offending field.
        List<Class<?>> ordered = topologicallyOrder(allSchemaClasses);
        this.emissionOrder = new HashMap<>();
        for (int i = 0; i < ordered.size(); i++) {
            this.emissionOrder.put(ordered.get(i).getName(), i);
        }

        // 4. Generate.
        StringBuilder output = new StringBuilder();
        output.append("// Auto-generated Zod schemas for mutation commands\n");
        output.append("// DO NOT EDIT MANUALLY\n\n");
        output.append("import { z } from \"zod\";\n");
        // Value import (no `type` modifier): enums in types.ts are emitted by
        // typescript-generator as runtime values, and z.nativeEnum(Types.X) references
        // them at runtime. A type-only import would compile-fail at every nativeEnum
        // call site with TS1361 ("cannot be used as a value because it was imported
        // using 'import type'").
        output.append("import * as Types from \"./types\";\n\n");

        for (Class<?> clazz : ordered) {
            this.currentEmissionIndex = this.emissionOrder.get(clazz.getName());
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
     * BFS over the field graph of the root request-body classes, collecting every complex
     * object type that should get its own Zod schema. A type is a "schema class" when it is
     * a plain object DTO/record/command — i.e. NOT a JDK type, NOT mapped to a TS primitive
     * (via {@link #typeMapping}), NOT an enum/array/collection/map, and NOT a Date-like type.
     * Those leaf categories are handled inline by {@link #mapTypeToZod(Class)}.
     */
    protected Set<Class<?>> discoverSchemaClasses(Set<Class<?>> roots) {
        Set<Class<?>> result = new LinkedHashSet<>();
        Deque<Class<?>> queue = new ArrayDeque<>(roots);
        while (!queue.isEmpty()) {
            Class<?> clazz = queue.poll();
            // A root annotated @ZodSchema(IGNORE) is honoured even if it slipped into the
            // root set — never generate a schema for an explicitly-ignored type.
            if (isZodIgnored(clazz) || !result.add(clazz)) {
                continue;
            }
            for (Class<?> referenced : referencedComplexTypes(clazz)) {
                if (!result.contains(referenced)) {
                    queue.add(referenced);
                }
            }
        }
        return result;
    }

    /**
     * Finds types annotated {@code @ZodSchema(INCLUDE)} among the classes the TypeScript
     * generator already knows about ({@link #typeMapping}), to add as extra generation roots.
     * Limited to API-surface types on purpose — discovering types referenced nowhere in the
     * API would require a full classpath scan, which the generator deliberately avoids.
     */
    protected Set<Class<?>> discoverIncludeRoots() {
        Set<Class<?>> roots = new LinkedHashSet<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (String fqn : typeMapping.keySet()) {
            try {
                Class<?> clazz = Class.forName(fqn, false, cl);
                ZodSchema annotation = clazz.getAnnotation(ZodSchema.class);
                if (annotation != null && annotation.value() == ZodSchema.Mode.INCLUDE) {
                    roots.add(clazz);
                }
            } catch (ClassNotFoundException | LinkageError e) {
                // Not loadable here (e.g. a TS-primitive mapping like "string") — skip.
            }
        }
        return roots;
    }

    /** True when the type carries {@code @ZodSchema(IGNORE)}. */
    protected boolean isZodIgnored(Class<?> type) {
        ZodSchema annotation = type.getAnnotation(ZodSchema.class);
        return annotation != null && annotation.value() == ZodSchema.Mode.IGNORE;
    }

    /**
     * Returns the distinct complex object types referenced by a class's instance fields,
     * unwrapping collection/array element types. Leaf categories (primitives, enums,
     * TS-primitive-mapped types, Date-likes, maps) are intentionally excluded — they never
     * need their own schema.
     */
    protected Set<Class<?>> referencedComplexTypes(Class<?> clazz) {
        Set<Class<?>> referenced = new LinkedHashSet<>();
        for (Field field : getAllFields(clazz)) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    || java.lang.reflect.Modifier.isTransient(field.getModifiers())
                    || field.isSynthetic()) {
                continue;
            }
            Class<?> elementType = complexElementType(field);
            if (elementType != null) {
                referenced.add(elementType);
            }
        }
        return referenced;
    }

    /**
     * Resolves the underlying complex object type of a field — unwrapping a single
     * collection/array element type or a map value type — or {@code null} if the field is a
     * leaf category that {@link #mapTypeToZod(Class)} renders inline (primitive, enum, Date,
     * TS-primitive-mapped type, or a type whose element couldn't be resolved).
     */
    protected Class<?> complexElementType(Field field) {
        Class<?> type = field.getType();
        if (Collection.class.isAssignableFrom(type)) {
            return complexTypeArg(field.getGenericType(), 0);
        }
        if (Map.class.isAssignableFrom(type)) {
            // Maps surface as z.record(z.string(), <value>); the value type is arg index 1.
            return complexTypeArg(field.getGenericType(), 1);
        }
        if (type.isArray()) {
            return complexOrNull(type.getComponentType());
        }
        return complexOrNull(type);
    }

    /** Extracts the i-th type argument as a complex schema class, or {@code null}. */
    protected Class<?> complexTypeArg(java.lang.reflect.Type generic, int index) {
        if (generic instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.Type[] args = ((java.lang.reflect.ParameterizedType) generic).getActualTypeArguments();
            if (args.length > index && args[index] instanceof Class<?>) {
                return complexOrNull((Class<?>) args[index]);
            }
        }
        return null;
    }

    /** Returns {@code type} if it is a complex object worth its own schema, else {@code null}. */
    protected Class<?> complexOrNull(Class<?> type) {
        if (type.isPrimitive() || type.isEnum() || type.isArray()) {
            return null;
        }
        if (type == String.class || type == CharSequence.class || Number.class.isAssignableFrom(type)
                || type == Boolean.class || type == Character.class) {
            return null;
        }
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            return null;
        }
        String pkg = type.getPackageName();
        if (pkg.startsWith("java.") || pkg.startsWith("javax.") || pkg.startsWith("jakarta.")) {
            return null; // JDK / Date-like / framework leaf types
        }
        // Mapped to a TS primitive by a customizer (e.g. Tsid → "string") — leaf.
        String tsTypeName = typeMapping.get(type.getName());
        if (tsTypeName != null && TS_PRIMITIVES.contains(tsTypeName)) {
            return null;
        }
        // Explicitly excluded — don't generate a schema; the referrer degrades to z.any().
        if (isZodIgnored(type)) {
            return null;
        }
        return type;
    }

    /**
     * Orders schema classes so that, ignoring cycles, a class appears after the schemas it
     * depends on. Uses a DFS post-order over the dependency edges; back-edges (cycles) are
     * tolerated and later broken with {@code z.lazy(...)} at the referencing field.
     */
    protected List<Class<?>> topologicallyOrder(Set<Class<?>> classes) {
        List<Class<?>> ordered = new ArrayList<>();
        Set<Class<?>> visited = new HashSet<>();
        Set<Class<?>> onStack = new HashSet<>();
        for (Class<?> clazz : classes) {
            topoVisit(clazz, classes, visited, onStack, ordered);
        }
        return ordered;
    }

    private void topoVisit(
            Class<?> clazz,
            Set<Class<?>> universe,
            Set<Class<?>> visited,
            Set<Class<?>> onStack,
            List<Class<?>> ordered) {
        if (visited.contains(clazz)) {
            return;
        }
        visited.add(clazz);
        onStack.add(clazz);
        for (Class<?> dep : referencedComplexTypes(clazz)) {
            if (universe.contains(dep) && !onStack.contains(dep)) {
                topoVisit(dep, universe, visited, onStack, ordered);
            }
        }
        onStack.remove(clazz);
        ordered.add(clazz);
    }

    /**
     * Extracts all classes used in mutation request bodies (POST, PUT, PATCH, DELETE).
     *
     * <p>Two sources are consulted, in this order:
     * <ol>
     *   <li>The operation extension {@link OpenApiCustomExtractor#REQUEST_BODY_TYPE_NAME}
     *       — emitted by {@link OpenApiCustomExtractor} for every {@code @MutationHook}
     *       endpoint and carrying the fully-qualified Java type name of the
     *       {@code @RequestBody} parameter. This is the path used when commons-rest is
     *       wired into a live Spring Boot application via its auto-configuration.</li>
     *   <li>The {@code x-java-type} extension on the request-body schema itself.
     *       Kept as a fallback for setups that populate the extension via a custom
     *       OpenAPI customizer and for the existing unit-test fixtures, which
     *       construct synthetic schemas with this extension.</li>
     * </ol>
     */
    protected Set<String> extractMutationClasses() {
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
                extractClassFromOperationExtensions(operation, classes);
                if (operation.getRequestBody() != null) {
                    extractClassFromRequestBody(operation.getRequestBody(), classes);
                }
            }
        }

        return classes;
    }

    /**
     * Reads the {@link OpenApiCustomExtractor#REQUEST_BODY_TYPE_NAME} operation
     * extension (set by {@link OpenApiCustomExtractor} for every {@code @MutationHook}
     * endpoint) and collects fully-qualified Java type names found there.
     */
    protected void extractClassFromOperationExtensions(Operation operation, Set<String> classes) {
        if (operation.getExtensions() == null) {
            return;
        }
        Object reqBodyType = operation.getExtensions().get(OpenApiCustomExtractor.REQUEST_BODY_TYPE_NAME);
        if (reqBodyType instanceof String) {
            extractFullyQualifiedClassNames((String) reqBodyType, classes);
        }
    }

    /**
     * Pulls fully-qualified class names out of a generic type string such as
     * {@code "io.example.MyCmd"} or {@code "java.util.List<io.example.MyCmd>"}.
     * Mirrors {@link OpenApiSchemaAnalyzer#extractClassNamesFromGenericType(String, Set)}
     * but without the customizer-based exclusion (Zod schema generation already
     * filters in {@link #loadClasses(Set)} via the {@code Cmd} suffix check).
     */
    protected void extractFullyQualifiedClassNames(String typeStr, Set<String> classNames) {
        if (typeStr == null || typeStr.isEmpty()) {
            return;
        }
        String cleaned = typeStr.replaceAll("[<>]", " ");
        for (String part : cleaned.split("[,\\s]+")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.contains(".") && !trimmed.startsWith("java.lang.") && !trimmed.startsWith("java.util.")) {
                classNames.add(trimmed);
            }
        }
    }

    protected void extractClassFromRequestBody(RequestBody requestBody, Set<String> classes) {
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

    protected Set<Class<?>> loadClasses(Set<String> classNames) {
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
    protected String generateZodSchemaForClass(Class<?> clazz) {
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

    protected List<Field> getAllFields(Class<?> clazz) {
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
    protected String generateFieldSchema(Field field) {
        // Skip static, transient and synthetic fields. Synthetic fields ($jacocoData,
        // outer-class references, Lombok internals on @SuperBuilder hierarchies) would
        // otherwise leak phantom properties into the schema.
        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                || java.lang.reflect.Modifier.isTransient(field.getModifiers())
                || field.isSynthetic()) {
            return null;
        }

        ZodSchema fieldAnnotation = field.getAnnotation(ZodSchema.class);

        // @ZodSchema(IGNORE) on a field — drop it from the parent schema entirely.
        if (fieldAnnotation != null && fieldAnnotation.value() == ZodSchema.Mode.IGNORE) {
            return null;
        }

        String fieldName = field.getName();
        boolean isRequired = config.getRequiredAnnotations().stream()
                .anyMatch(field::isAnnotationPresent);

        // @ZodSchema(ANY) on a field — keep it but emit z.any(), skipping all type mapping
        // and validation. The escape hatch for shapes too complex/opaque to model in Zod.
        if (fieldAnnotation != null && fieldAnnotation.value() == ZodSchema.Mode.ANY) {
            return "  " + fieldName + ": z.any()" + (isRequired ? "" : ".nullish()");
        }

        String zodType = mapFieldTypeToZod(field);

        // Zod v4: prefer the top-level string-format constructor z.email() over the
        // deprecated .email() string method. Only swap the base when the field maps to a
        // plain string — for non-string types @Email is meaningless and left untouched.
        boolean emailHandledInBase = false;
        if (field.isAnnotationPresent(Email.class) && "z.string()".equals(zodType)) {
            zodType = "z.email()";
            emailHandledInBase = true;
        }

        // Collect all validations
        List<String> validations = new ArrayList<>();

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

        // @DecimalMin — emit the literal value verbatim (via BigDecimal#toPlainString) rather
        // than routing through Double.parseDouble, which loses precision for money-like values
        // (e.g. "0.01" → 0.009999999...).
        if (field.isAnnotationPresent(DecimalMin.class)) {
            DecimalMin decimalMin = field.getAnnotation(DecimalMin.class);
            String minValue = decimalLiteral(decimalMin.value());
            validations.add((decimalMin.inclusive() ? ".min(" : ".gt(") + minValue + ")");
        }

        // @DecimalMax
        if (field.isAnnotationPresent(DecimalMax.class)) {
            DecimalMax decimalMax = field.getAnnotation(DecimalMax.class);
            String maxValue = decimalLiteral(decimalMax.value());
            validations.add((decimalMax.inclusive() ? ".max(" : ".lt(") + maxValue + ")");
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

        // @Email — already folded into the z.email() base above for string fields. Only
        // fall back to the chained .email() when the base wasn't swapped (defensive; should
        // not normally happen for a String field).
        if (field.isAnnotationPresent(Email.class) && !emailHandledInBase) {
            validations.add(".email()");
        }

        // @Pattern
        if (field.isAnnotationPresent(Pattern.class)) {
            Pattern pattern = field.getAnnotation(Pattern.class);
            // A bare '/' terminates the JS regex literal — escape any that aren't already
            // escaped so the generated z.string().regex(/.../) stays syntactically valid.
            String regex = escapeForJsRegexLiteral(pattern.regexp());
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
    protected String mapFieldTypeToZod(Field field) {
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

        // Map with a resolvable value type → z.record(z.string(), <value>). The value zod
        // type goes through the same mapping, so a nested DTO value becomes a schema reference
        // (e.g. Map<String, FooDto> → z.record(z.string(), FooDtoSchema)).
        if (Map.class.isAssignableFrom(type)) {
            java.lang.reflect.Type genericType = field.getGenericType();
            if (genericType instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.Type[] typeArgs = ((java.lang.reflect.ParameterizedType) genericType).getActualTypeArguments();
                if (typeArgs.length == 2 && typeArgs[1] instanceof Class<?>) {
                    String valueZodType = mapTypeToZod((Class<?>) typeArgs[1]);
                    return "z.record(z.string(), " + valueZodType + ")";
                }
            }
            return "z.record(z.string(), z.any())";
        }

        // For all other types, use the regular mapping
        return mapTypeToZod(type);
    }

    /**
     * Maps Java types to Zod types.
     */
    protected String mapTypeToZod(Class<?> type) {
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

        // Consult the TypeScript type-mapping from the model generator so we don't emit
        // dangling references to types that were stripped (TypeScriptGeneratorCustomizer)
        // or remapped to a primitive (customTypeMappings, e.g. Tsid → "string").
        String tsTypeName = typeMapping.get(type.getName());

        // Enums: emit z.enum([...string literals...]) using the Java enum constants.
        // typescript-generator's default (EnumMapping.asUnion) renders Java enums as
        // string-literal unions like `type X = "a" | "b"` — runtime-erased, so
        // z.nativeEnum(Types.X) would fail with "Property 'X' does not exist on type
        // typeof import('./types')". Using the constant names directly side-steps the
        // typescript-generator mode entirely and matches the JSON wire format that
        // Jackson produces for plain Java enums.
        if (type.isEnum()) {
            return "z.enum([" + enumConstantsAsLiterals(type) + "])";
        }

        if (tsTypeName != null && TS_PRIMITIVES.contains(tsTypeName)) {
            // Customizer-mapped to a TS primitive — emit the matching Zod primitive
            // instead of z.custom<Types.string>() which would not exist in types.ts.
            if ("number".equals(tsTypeName)) return "z.number()";
            if ("boolean".equals(tsTypeName)) return "z.boolean()";
            if ("any".equals(tsTypeName)) return "z.any()";
            if ("unknown".equals(tsTypeName)) return "z.unknown()";
            return "z.string()";
        }

        // Complex object that has its own generated schema — reference it so the nested
        // validation is actually enforced (a plain z.custom<>() validates nothing).
        if (schemaClasses.containsKey(type.getName())) {
            return schemaReference(type);
        }

        // @ZodSchema(IGNORE) on the type — caller asked to skip modelling it; emit z.any()
        // so the field is accepted without an unresolvable Types.X reference.
        if (isZodIgnored(type)) {
            return "z.any()";
        }

        // No schema for this complex type (no TypeScriptGenerationResult passed in, or the
        // type is a value object typescript-generator emits but we don't generate a schema
        // for). Fall back to a type-only custom validator through the Types namespace.
        String fallbackName = tsTypeName != null ? tsTypeName : typeName;
        return "z.custom<Types." + fallbackName + ">()";
    }

    /**
     * Emits the reference to another class's generated schema. When the referenced schema is
     * declared later than the current one (forward reference, including the back-edge of a
     * dependency cycle), it is wrapped in {@code z.lazy(() => …)} so the {@code const} is
     * resolved at call time rather than at module-evaluation time.
     */
    protected String schemaReference(Class<?> type) {
        String schemaConst = type.getSimpleName() + "Schema";
        Integer refIndex = emissionOrder.get(type.getName());
        boolean forwardReference = refIndex == null || currentEmissionIndex < 0 || refIndex >= currentEmissionIndex;
        return forwardReference ? "z.lazy(() => " + schemaConst + ")" : schemaConst;
    }

    /**
     * Normalises a {@code @DecimalMin}/{@code @DecimalMax} value string into a JS numeric
     * literal without precision loss. Bean Validation guarantees the string is a valid
     * {@link java.math.BigDecimal}; {@code toPlainString()} avoids scientific notation
     * (e.g. {@code 1E-2}) that, while valid JS, reads worse than {@code 0.01}. Falls back to
     * the raw value if it somehow doesn't parse.
     */
    protected String decimalLiteral(String value) {
        try {
            return new java.math.BigDecimal(value.trim()).toPlainString();
        } catch (NumberFormatException e) {
            log.warn("@DecimalMin/@DecimalMax value '{}' is not a valid decimal — emitting verbatim", value);
            return value.trim();
        }
    }

    /**
     * Escapes a Java regex so it can be embedded in a JavaScript regex literal
     * ({@code /.../}). Only the literal-terminating {@code '/'} needs escaping, and only when
     * it isn't already escaped — backslash sequences in {@code @Pattern} are passed through
     * unchanged (they are valid in JS regex too).
     */
    protected String escapeForJsRegexLiteral(String regex) {
        StringBuilder out = new StringBuilder(regex.length() + 4);
        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);
            if (c == '\\' && i + 1 < regex.length()) {
                // Preserve escape sequences verbatim (incl. an already-escaped slash).
                out.append(c).append(regex.charAt(i + 1));
                i++;
            } else if (c == '/') {
                out.append("\\/");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Renders the Java enum constants as comma-separated, double-quoted string literals
     * for use inside {@code z.enum([...])}. The literals must match the wire format
     * Jackson produces, so the method honours any {@code @JsonValue}-annotated accessor
     * on the enum (frequent pattern: lowercase external value, uppercase Java constant
     * name) and falls back to {@code Enum#name()} when none is declared.
     */
    protected String enumConstantsAsLiterals(Class<?> enumType) {
        java.lang.reflect.Method jsonValueAccessor = findJsonValueAccessor(enumType);
        StringBuilder out = new StringBuilder();
        Object[] constants = enumType.getEnumConstants();
        for (int i = 0; i < constants.length; i++) {
            if (i > 0) out.append(", ");
            String literal;
            if (jsonValueAccessor != null) {
                try {
                    Object value = jsonValueAccessor.invoke(constants[i]);
                    literal = value != null ? value.toString() : ((Enum<?>) constants[i]).name();
                } catch (ReflectiveOperationException e) {
                    log.warn("@JsonValue accessor on {} failed for {} — falling back to name()",
                            enumType.getName(), ((Enum<?>) constants[i]).name(), e);
                    literal = ((Enum<?>) constants[i]).name();
                }
            } else {
                literal = ((Enum<?>) constants[i]).name();
            }
            out.append('"').append(literal.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        return out.toString();
    }

    /**
     * Returns the first {@code @JsonValue}-annotated zero-arg method declared on the
     * enum type (or its superclasses), or {@code null} if none is present. Mirrors
     * Jackson's discovery of the externalised value used for serialisation.
     */
    protected java.lang.reflect.Method findJsonValueAccessor(Class<?> enumType) {
        Class<?> current = enumType;
        while (current != null && current != Object.class) {
            for (java.lang.reflect.Method m : current.getDeclaredMethods()) {
                if (m.getParameterCount() == 0
                        && m.isAnnotationPresent(JsonValue.class)) {
                    m.setAccessible(true);
                    return m;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
