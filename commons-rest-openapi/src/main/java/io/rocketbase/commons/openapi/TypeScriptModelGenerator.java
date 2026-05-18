package io.rocketbase.commons.openapi;

import cz.habarta.typescript.generator.*;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Generates TypeScript model interfaces from OpenAPI spec using typescript-generator API.
 * This replaces ALL manual type conversion - no more string manipulation!
 */
@Slf4j
@RequiredArgsConstructor
public class TypeScriptModelGenerator {

    protected final TypeScriptGeneratorConfig config;
    protected final List<TypeScriptGeneratorCustomizer> customizers;

    /**
     * Generates ALL TypeScript types from OpenAPI specification.
     * Extracts all Java classes used in the API and converts them to TypeScript.
     *
     * @param openAPI    The OpenAPI specification
     * @param outputFile Path to the output .ts file (e.g., "model/types.ts")
     * @return TypeScriptGenerationResult with generated code and type mappings
     */
    public TypeScriptGenerationResult generateFromOpenAPI(OpenAPI openAPI, Path outputFile) {
        log.info("Generating TypeScript types from OpenAPI specification");

        // 1. Analyze OpenAPI to find all used classes
        OpenApiSchemaAnalyzer analyzer = new OpenApiSchemaAnalyzer(customizers);
        Set<String> classNamesFromExtensions = analyzer.extractFromExtensions(openAPI);

        log.info("Found {} class names from OpenAPI extensions", classNamesFromExtensions.size());
        classNamesFromExtensions.forEach(name -> log.debug("  - {}", name));

        // 2. Load classes
        Set<Class<?>> classes = loadClasses(classNamesFromExtensions);

        if (classes.isEmpty()) {
            log.warn("No classes found to generate TypeScript types!");
            return new TypeScriptGenerationResult("", new HashMap<>());
        }

        // 3. Generate TypeScript
        String typescript = generateModels(classes, outputFile);

        // 4. Build type mapping (Java FQN -> TypeScript type name)
        Map<String, String> typeMapping = buildTypeMapping(classes);

        return new TypeScriptGenerationResult(typescript, typeMapping);
    }

    /**
     * Generates TypeScript interfaces for the given Java classes.
     *
     * @param classes    Set of Java classes to convert to TypeScript
     * @param outputFile Path to the output .ts file
     * @return The generated TypeScript code
     */
    public String generateModels(Set<Class<?>> classes, Path outputFile) {
        log.info("Generating TypeScript models for {} classes to {}", classes.size(), outputFile);

        Settings settings = createSettings();
        List<String> transitivelyExcluded = collectTransitivelyExcludedFqns(classes);
        if (!transitivelyExcluded.isEmpty()) {
            log.info("Excluding {} class(es) from TypeScript via customizer: {}",
                    transitivelyExcluded.size(), transitivelyExcluded);
            settings.setExcludeFilter(transitivelyExcluded, Collections.emptyList());
        }
        TypeScriptGenerator generator = new TypeScriptGenerator(settings);

        String result = generator.generateTypeScript(Input.from(classes.toArray(new Class<?>[0])));

        // Write to file
        try {
            File file = outputFile.toFile();
            file.getParentFile().mkdirs();
            java.nio.file.Files.writeString(outputFile, result);
            log.info("Successfully generated TypeScript models: {}", outputFile);
        } catch (Exception e) {
            log.error("Failed to write TypeScript models to file: {}", outputFile, e);
            throw new RuntimeException("Failed to generate TypeScript models", e);
        }

        return result;
    }

    /**
     * Loads Java classes from class names and applies exclusion filters.
     */
    protected Set<Class<?>> loadClasses(Set<String> classNames) {
        Set<Class<?>> classes = new HashSet<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className, false, classLoader);

                // Check if class should be excluded
                if (shouldExcludeClass(clazz)) {
                    log.debug("Excluding class from TypeScript generation: {}", className);
                    continue;
                }

                classes.add(clazz);
                log.debug("Loaded class: {}", className);
            } catch (ClassNotFoundException e) {
                log.warn("Could not load class: {} - skipping", className);
            }
        }

        return classes;
    }

    /**
     * Builds a mapping from Java class FQN to TypeScript type name.
     * Example: "io.rocketbase.commons.dto.ActivityDto" -> "ActivityDto"
     */
    protected Map<String, String> buildTypeMapping(Set<Class<?>> classes) {
        Map<String, String> mapping = new HashMap<>();

        for (Class<?> clazz : classes) {
            String javaFqn = clazz.getName();
            String tsTypeName = clazz.getSimpleName();
            mapping.put(javaFqn, tsTypeName);

            log.debug("Type mapping: {} -> {}", javaFqn, tsTypeName);
        }

        for (Map.Entry<String, String> entry : getCustomTypeMappings().entrySet()) {
            // Hardcoded mappings carry generic placeholders ("<T>", "<T, M>"); strip
            // those so toTypeScript matches the bare FQN — generics are reconstructed
            // by the caller.
            String bareFqn = entry.getKey().replaceAll("<[^>]*>", "").trim();
            String bareTs = entry.getValue().replaceAll("<[^>]*>", "").trim();
            mapping.putIfAbsent(bareFqn, bareTs);
        }

        return mapping;
    }

    /**
     * Creates typescript-generator settings based on configuration.
     * Applies customizers after default settings are created.
     */
    protected Settings createSettings() {
        Settings settings = new Settings();

        // Output configuration
        settings.outputKind = TypeScriptOutputKind.module;
        settings.outputFileType = TypeScriptFileType.implementationFile;
        settings.jsonLibrary = JsonLibrary.jackson2;

        // Code style
        settings.noFileComment = true;
        settings.noTslintDisable = true;
        settings.noEslintDisable = true;
        settings.newline = "\n";
        settings.quotes = "\"";
        settings.indentString = "  ";

        // Type mappings - use config values
        Map<String, String> typeMappings = new HashMap<>(getCustomTypeMappings());
        if (config.getAdditionalTypeMappings() != null) {
            typeMappings.putAll(config.getAdditionalTypeMappings());
        }
        settings.customTypeMappings = typeMappings;
        settings.mapDate = config.getMapDate();
        settings.mapEnum = config.getMapEnum();
        settings.nonConstEnums = config.isNonConstEnums();

        // Required property handling - use config values
        // Only fields with requiredAnnotations will be required, all others optional
        settings.optionalProperties = OptionalProperties.useSpecifiedAnnotations;
        settings.requiredAnnotations = config.getRequiredAnnotations();

        // Class loader
        settings.classLoader = Thread.currentThread().getContextClassLoader();

        // Apply customizers (sorted by order)
        applyCustomizers(settings);

        return settings;
    }

    /**
     * Applies all registered customizers to the settings.
     * Customizers are sorted by their order (lower values first).
     */
    protected void applyCustomizers(Settings settings) {
        if (customizers == null || customizers.isEmpty()) {
            log.debug("No TypeScript generator customizers found");
            return;
        }

        log.info("Applying {} TypeScript generator customizer(s)", customizers.size());

        // Sort customizers by order
        customizers.stream()
                .sorted((c1, c2) -> Integer.compare(c1.getOrder(), c2.getOrder()))
                .forEach(customizer -> {
                    log.debug("Applying customizer: {}", customizer.getClass().getSimpleName());
                    try {
                        customizer.customize(settings);
                    } catch (Exception e) {
                        log.error("Error applying customizer {}: {}", customizer.getClass().getSimpleName(), e.getMessage(), e);
                    }
                });
    }

    /**
     * Custom type mappings for common types.
     * Note: For generic types, you MUST include the generic parameter!
     */
    protected Map<String, String> getCustomTypeMappings() {
        return Map.ofEntries(
                Map.entry("java.net.URL", "string"),
                Map.entry("io.hypersistence.tsid.TSID", "string"),
                Map.entry("com.github.f4b6a3.tsid.Tsid", "string"),
                Map.entry("java.time.LocalDate", "string"),
                Map.entry("java.time.LocalTime", "string"),
                Map.entry("java.time.LocalDateTime", "string"),
                Map.entry("java.time.Instant", "string"),
                Map.entry("java.time.OffsetDateTime", "string"),
                Map.entry("java.time.ZonedDateTime", "string"),
                Map.entry("java.util.Map<K, V>", "Record<K, V>"),
                Map.entry("io.rocketbase.commons.obfuscated.ObfuscatedId", "string"),
                // PageableResult types - imported from @rocketbase/commons-rest-client
                // These are not generated, but imported from the runtime library
                Map.entry("io.rocketbase.commons.dto.PageableResult<T>", "PageableResult<T>"),
                Map.entry("io.rocketbase.commons.dto.PageableResultImpl<T>", "PageableResult<T>"),
                Map.entry("io.rocketbase.commons.dto.PageableResultWithMeta<T, M>", "PageableResultWithMeta<T, M>")
                // Let typescript-generator handle DTOs - they will be generated as interfaces
        );
    }

    /**
     * Determines if a class should be excluded from generation.
     * Checks both built-in exclusion rules and registered customizers.
     */
    protected boolean shouldExcludeClass(Class<?> clazz) {
        String className = clazz.getName();

        // Exclude patterns
        String[] excludePatterns = {
                "Builder",
                "BuilderImpl",
                "Deserializer",
                "Serializer",
                "Exception",
                "Visitor"
        };

        for (String pattern : excludePatterns) {
            if (className.endsWith(pattern)) {
                return true;
            }
        }

        // Exclude specific packages
        if (className.startsWith("org.springframework.boot.autoconfigure.")) {
            return true;
        }

        // Exclude common Java classes
        if (className.equals("java.io.Serializable") ||
                className.equals("java.lang.Iterable")) {
            return true;
        }

        // Check customizers
        if (customizers != null) {
            for (TypeScriptGeneratorCustomizer customizer : customizers) {
                if (customizer.shouldExcludeClass(clazz)) {
                    log.debug("Class {} excluded by customizer: {}", className, customizer.getClass().getSimpleName());
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Walks the super-class + super-interface chain of every input class and collects
     * the FQNs of those that a registered customizer wants excluded. Pushed into
     * {@code Settings.excludeFilter} so typescript-generator drops them from the output
     * AND strips the corresponding {@code extends X} clauses from the subtypes that
     * inherit from them.
     *
     * <p>Walked transitively (super of super, etc.) because deep marker-interface
     * hierarchies (e.g. {@code Aggregate extends Identifiable extends ...}) would
     * otherwise leak the intermediate interfaces.
     */
    protected List<String> collectTransitivelyExcludedFqns(Set<Class<?>> roots) {
        if (customizers == null || customizers.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> excluded = new LinkedHashSet<>();
        Deque<Class<?>> queue = new ArrayDeque<>(roots);
        Set<Class<?>> visited = new HashSet<>();
        while (!queue.isEmpty()) {
            Class<?> clazz = queue.poll();
            if (clazz == null || !visited.add(clazz)) {
                continue;
            }
            for (TypeScriptGeneratorCustomizer customizer : customizers) {
                if (customizer.shouldExcludeClass(clazz)) {
                    excluded.add(clazz.getName());
                    break;
                }
            }
            if (clazz.getSuperclass() != null) {
                queue.add(clazz.getSuperclass());
            }
            for (Class<?> iface : clazz.getInterfaces()) {
                queue.add(iface);
            }
        }
        return new ArrayList<>(excluded);
    }

    /**
     * Configuration for TypeScript generation.
     * Allows customization of typescript-generator settings.
     */
    public static class TypeScriptGeneratorConfig {
        protected EnumMapping mapEnum = EnumMapping.asUnion;
        protected DateMapping mapDate = DateMapping.asString;
        protected boolean nonConstEnums = false;

        /**
         * Annotations that mark fields as required in TypeScript.
         * Only fields with these annotations will be required, all others will be optional.
         * Default: @NotNull and @NotBlank
         */
        protected List<Class<? extends java.lang.annotation.Annotation>> requiredAnnotations = List.of(
                jakarta.validation.constraints.NotNull.class,
                jakarta.validation.constraints.NotBlank.class
        );

        protected Map<String, String> additionalTypeMappings = Map.of();

        public EnumMapping getMapEnum() {
            return mapEnum;
        }

        public void setMapEnum(EnumMapping mapEnum) {
            this.mapEnum = mapEnum;
        }

        public DateMapping getMapDate() {
            return mapDate;
        }

        public void setMapDate(DateMapping mapDate) {
            this.mapDate = mapDate;
        }

        public boolean isNonConstEnums() {
            return nonConstEnums;
        }

        public void setNonConstEnums(boolean nonConstEnums) {
            this.nonConstEnums = nonConstEnums;
        }

        public List<Class<? extends java.lang.annotation.Annotation>> getRequiredAnnotations() {
            return requiredAnnotations;
        }

        public void setRequiredAnnotations(List<Class<? extends java.lang.annotation.Annotation>> requiredAnnotations) {
            this.requiredAnnotations = requiredAnnotations;
        }

        public Map<String, String> getAdditionalTypeMappings() {
            return additionalTypeMappings;
        }

        public void setAdditionalTypeMappings(Map<String, String> additionalTypeMappings) {
            this.additionalTypeMappings = additionalTypeMappings;
        }
    }
}
