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

    private final TypeScriptGeneratorConfig config;

    /**
     * Generates ALL TypeScript types from OpenAPI specification.
     * Extracts all Java classes used in the API and converts them to TypeScript.
     *
     * @param openAPI The OpenAPI specification
     * @param outputFile Path to the output .ts file (e.g., "model/types.ts")
     * @return TypeScriptGenerationResult with generated code and type mappings
     */
    public TypeScriptGenerationResult generateFromOpenAPI(OpenAPI openAPI, Path outputFile) {
        log.info("Generating TypeScript types from OpenAPI specification");

        // 1. Analyze OpenAPI to find all used classes
        OpenApiSchemaAnalyzer analyzer = new OpenApiSchemaAnalyzer();
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
     * @param classes Set of Java classes to convert to TypeScript
     * @param outputFile Path to the output .ts file
     * @return The generated TypeScript code
     */
    public String generateModels(Set<Class<?>> classes, Path outputFile) {
        log.info("Generating TypeScript models for {} classes to {}", classes.size(), outputFile);

        Settings settings = createSettings();
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
     * Loads Java classes from class names.
     */
    private Set<Class<?>> loadClasses(Set<String> classNames) {
        Set<Class<?>> classes = new HashSet<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className, false, classLoader);
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
    private Map<String, String> buildTypeMapping(Set<Class<?>> classes) {
        Map<String, String> mapping = new HashMap<>();

        for (Class<?> clazz : classes) {
            String javaFqn = clazz.getName();
            String tsTypeName = clazz.getSimpleName();
            mapping.put(javaFqn, tsTypeName);

            log.debug("Type mapping: {} -> {}", javaFqn, tsTypeName);
        }

        return mapping;
    }

    /**
     * Creates typescript-generator settings based on configuration.
     */
    private Settings createSettings() {
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

        // Optional handling - use config values
        settings.optionalProperties = OptionalProperties.useSpecifiedAnnotations;
        settings.optionalAnnotations = config.getOptionalAnnotations();

        // Class loader
        settings.classLoader = Thread.currentThread().getContextClassLoader();

        // Note: excludeFilter is not accessible, using different approach
        // Filtering will be done before passing classes to generator

        return settings;
    }

    /**
     * Custom type mappings for common types.
     * Note: For generic types, you MUST include the generic parameter!
     */
    private Map<String, String> getCustomTypeMappings() {
        return Map.ofEntries(
            Map.entry("java.net.URL", "string"),
            Map.entry("io.hypersistence.tsid.TSID", "string"),
            Map.entry("java.time.LocalDate", "string"),
            Map.entry("java.time.LocalTime", "string"),
            Map.entry("java.time.LocalDateTime", "string"),
            Map.entry("java.time.Instant", "string"),
            Map.entry("java.time.OffsetDateTime", "string"),
            Map.entry("java.time.ZonedDateTime", "string"),
            Map.entry("java.util.Map<K, V>", "Record<K, V>"),
            Map.entry("io.rocketbase.commons.obfuscated.ObfuscatedId", "string")
            // Let typescript-generator handle DTOs - they will be generated as interfaces
        );
    }

    /**
     * Determines if a class should be excluded from generation.
     */
    private boolean shouldExcludeClass(Class<?> clazz) {
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

        return false;
    }

    /**
     * Configuration for TypeScript generation.
     * Allows customization of typescript-generator settings.
     */
    public static class TypeScriptGeneratorConfig {
        private EnumMapping mapEnum = EnumMapping.asUnion;
        private DateMapping mapDate = DateMapping.asString;
        private boolean nonConstEnums = false;
        private List<Class<? extends java.lang.annotation.Annotation>> optionalAnnotations = List.of(
            jakarta.annotation.Nullable.class,
            org.springframework.lang.Nullable.class
        );
        private Map<String, String> additionalTypeMappings = Map.of();

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

        public List<Class<? extends java.lang.annotation.Annotation>> getOptionalAnnotations() {
            return optionalAnnotations;
        }

        public void setOptionalAnnotations(List<Class<? extends java.lang.annotation.Annotation>> optionalAnnotations) {
            this.optionalAnnotations = optionalAnnotations;
        }

        public Map<String, String> getAdditionalTypeMappings() {
            return additionalTypeMappings;
        }

        public void setAdditionalTypeMappings(Map<String, String> additionalTypeMappings) {
            this.additionalTypeMappings = additionalTypeMappings;
        }
    }
}
