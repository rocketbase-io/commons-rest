package io.rocketbase.commons.openapi;

import cz.habarta.typescript.generator.Settings;

/**
 * Interface for customizing TypeScript generator settings.
 * <p>
 * Implement this interface and register it as a Spring Bean to customize the TypeScript generation.
 * Multiple customizers can be registered and will be applied in order of their Spring @Order annotation.
 * </p>
 *
 * @see TypeScriptModelGenerator
 * @see org.springframework.core.annotation.Order
 *
 * @example
 * <pre>{@code
 * @Component
 * @Order(100)
 * public class MyTypeScriptCustomizer implements TypeScriptGeneratorCustomizer {
 *
 *     @Override
 *     public void customize(Settings settings) {
 *         // Add custom type mappings
 *         if (settings.customTypeMappings == null) {
 *             settings.customTypeMappings = new HashMap<>();
 *         }
 *         settings.customTypeMappings.put("com.mycompany.CustomType", "string");
 *
 *         // Change enum mapping
 *         settings.mapEnum = EnumMapping.asEnum;
 *     }
 *
 *     @Override
 *     public boolean shouldExcludeClass(Class<?> clazz) {
 *         // Exclude internal classes
 *         return clazz.getName().startsWith("com.mycompany.internal.");
 *     }
 * }
 * }</pre>
 */
public interface TypeScriptGeneratorCustomizer {

    /**
     * Customize the TypeScript generator settings.
     * <p>
     * This method is called after default settings are applied but before generation starts.
     * You can modify any aspect of the settings here.
     * </p>
     *
     * @param settings The TypeScript generator settings to customize
     */
    void customize(Settings settings);

    /**
     * Determines if a class should be excluded from TypeScript generation.
     * <p>
     * This method is called for each Java class that would be included in the TypeScript output.
     * Return true to exclude the class from generation.
     * </p>
     * <p>
     * <strong>Note:</strong> PageableResult and related pagination types are already excluded by default,
     * as they are provided by @rocketbase/commons-rest-client. You can override this behavior if needed.
     * </p>
     * <p>
     * This method is called in two places:
     * <ul>
     *   <li>In {@link OpenApiSchemaAnalyzer} - during OpenAPI analysis to filter extracted classes</li>
     *   <li>In {@link TypeScriptModelGenerator} - before passing classes to typescript-generator</li>
     * </ul>
     * </p>
     *
     * @param clazz The Java class to check
     * @return true if the class should be excluded, false otherwise
     *
     * @example
     * <pre>{@code
     * @Override
     * public boolean shouldExcludeClass(Class<?> clazz) {
     *     // Exclude internal classes
     *     if (clazz.getName().startsWith("com.mycompany.internal.")) {
     *         return true;
     *     }
     *
     *     // Exclude classes with specific annotation
     *     if (clazz.isAnnotationPresent(ExcludeFromTypeScript.class)) {
     *         return true;
     *     }
     *
     *     return false;
     * }
     * }</pre>
     */
    default boolean shouldExcludeClass(Class<?> clazz) {
        return false;
    }

    /**
     * Determines the priority order of this customizer.
     * <p>
     * Lower values have higher priority and are applied first.
     * Default is 0.
     * </p>
     * <p>
     * <strong>Alternatively,</strong> use Spring's @Order annotation on your customizer bean.
     * </p>
     *
     * @return the priority order (lower = higher priority)
     */
    default int getOrder() {
        return 0;
    }
}
