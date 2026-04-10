package io.rocketbase.commons.openapi;

import io.rocketbase.commons.config.OpenApiGeneratorProperties;
import io.rocketbase.commons.openapi.model.ReactQueryVersion;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Maven Mojo to generate TypeScript client from OpenAPI specification.
 * This replaces the need for bash scripts and manual ZIP extraction.
 *
 * Usage in pom.xml:
 * <pre>
 * {@code
 * <build>
 *   <plugins>
 *     <plugin>
 *       <groupId>io.rocketbase.commons</groupId>
 *       <artifactId>commons-rest-openapi</artifactId>
 *       <executions>
 *         <execution>
 *           <goals>
 *             <goal>generate</goal>
 *           </goals>
 *         </execution>
 *       </executions>
 *     </plugin>
 *   </plugins>
 * </build>
 * }
 * </pre>
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class GenerateTypescriptClientMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Path to OpenAPI JSON file.
     * Default: ${project.build.directory}/openapi.json
     */
    @Parameter(property = "openapi.file", defaultValue = "${project.build.directory}/openapi.json")
    private File openapiFile;

    /**
     * Output directory for generated TypeScript client.
     * Default: ${project.build.directory}/typescript-client
     */
    @Parameter(property = "typescript.outputDirectory", defaultValue = "${project.build.directory}/typescript-client")
    private File outputDirectory;

    /**
     * React Query version to generate for.
     * Options: V3, V4, V5
     * Default: V5
     */
    @Parameter(property = "reactQuery.version", defaultValue = "V5")
    private String reactQueryVersion;

    /**
     * Base URL for API calls.
     * Default: /api
     */
    @Parameter(property = "api.baseUrl", defaultValue = "/api")
    private String baseUrl;

    /**
     * Group name for generated client.
     * Default: ModuleApi
     */
    @Parameter(property = "api.groupName", defaultValue = "ModuleApi")
    private String groupName;

    /**
     * Package name for generated npm package.
     * Default: openapi-client
     */
    @Parameter(property = "npm.packageName", defaultValue = "openapi-client")
    private String packageName;

    /**
     * Skip generation if true.
     */
    @Parameter(property = "typescript.skip", defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("TypeScript client generation skipped");
            return;
        }

        if (!openapiFile.exists()) {
            getLog().warn("OpenAPI file not found: " + openapiFile.getAbsolutePath());
            getLog().warn("Skipping TypeScript client generation");
            return;
        }

        try {
            getLog().info("Generating TypeScript client from: " + openapiFile.getAbsolutePath());
            getLog().info("Output directory: " + outputDirectory.getAbsolutePath());

            // Read OpenAPI specification
            OpenAPI openAPI = Json.mapper().readValue(openapiFile, OpenAPI.class);

            // Create configuration
            OpenApiGeneratorProperties generatorProperties = createGeneratorProperties();
            SpringDataWebProperties springDataProperties = createSpringDataProperties();

            // Create services
            TypeScriptTypeConverter typeConverter = new OpenApiTypeMapper(new TypeScriptGenerationResult("", new HashMap<>()));
            DefaultInfiniteOptionsTemplateBuilder templateBuilder = new DefaultInfiniteOptionsTemplateBuilder();

            // Note: OpenApiWebMvcResource is not available in Maven context
            // We work directly with the OpenAPI object
            OpenApiClientCreatorService creatorService = new OpenApiClientCreatorServiceStandalone(
                springDataProperties,
                generatorProperties,
                typeConverter,
                templateBuilder
            );

            // Generate client
            ReactQueryVersion version = ReactQueryVersion.valueOf(reactQueryVersion.toUpperCase());
            Path outputPath = outputDirectory.toPath();

            creatorService.generateClientToFileSystem(version, outputPath, openAPI, baseUrl, groupName);

            getLog().info("Successfully generated TypeScript client to: " + outputPath);

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate TypeScript client", e);
        }
    }

    private OpenApiGeneratorProperties createGeneratorProperties() {
        // Create properties using reflection to avoid setter issues during Maven build
        try {
            OpenApiGeneratorProperties props = OpenApiGeneratorProperties.class.getDeclaredConstructor().newInstance();

            // Use reflection to set fields if setters are not available
            java.lang.reflect.Field baseUrlField = OpenApiGeneratorProperties.class.getDeclaredField("baseUrl");
            baseUrlField.setAccessible(true);
            baseUrlField.set(props, baseUrl);

            java.lang.reflect.Field packageNameField = OpenApiGeneratorProperties.class.getDeclaredField("packageName");
            packageNameField.setAccessible(true);
            packageNameField.set(props, packageName);

            java.lang.reflect.Field groupNameField = OpenApiGeneratorProperties.class.getDeclaredField("groupName");
            groupNameField.setAccessible(true);
            groupNameField.set(props, groupName);

            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create OpenApiGeneratorProperties", e);
        }
    }

    private SpringDataWebProperties createSpringDataProperties() {
        // Create a minimal configuration for pagination parameters
        try {
            SpringDataWebProperties props = SpringDataWebProperties.class.getDeclaredConstructor().newInstance();
            SpringDataWebProperties.Pageable pageable = SpringDataWebProperties.Pageable.class.getDeclaredConstructor().newInstance();
            SpringDataWebProperties.Sort sort = SpringDataWebProperties.Sort.class.getDeclaredConstructor().newInstance();

            // Use reflection to set fields
            java.lang.reflect.Field pageParamField = SpringDataWebProperties.Pageable.class.getDeclaredField("pageParameter");
            pageParamField.setAccessible(true);
            pageParamField.set(pageable, "page");

            java.lang.reflect.Field sizeParamField = SpringDataWebProperties.Pageable.class.getDeclaredField("sizeParameter");
            sizeParamField.setAccessible(true);
            sizeParamField.set(pageable, "size");

            java.lang.reflect.Field sortParamField = SpringDataWebProperties.Sort.class.getDeclaredField("sortParameter");
            sortParamField.setAccessible(true);
            sortParamField.set(sort, "sort");

            java.lang.reflect.Field pageableField = SpringDataWebProperties.class.getDeclaredField("pageable");
            pageableField.setAccessible(true);
            pageableField.set(props, pageable);

            java.lang.reflect.Field sortField = SpringDataWebProperties.class.getDeclaredField("sort");
            sortField.setAccessible(true);
            sortField.set(props, sort);

            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SpringDataWebProperties", e);
        }
    }

    /**
     * Standalone version of OpenApiClientCreatorService that doesn't require Spring context.
     * Used by Maven plugin.
     */
    private static class OpenApiClientCreatorServiceStandalone extends OpenApiClientCreatorService {

        public OpenApiClientCreatorServiceStandalone(
            SpringDataWebProperties springDataWebProperties,
            OpenApiGeneratorProperties openApiGeneratorProperties,
            TypeScriptTypeConverter typeConverter,
            InfiniteOptionsTemplateBuilder templateBuilder
        ) {
            super(springDataWebProperties, openApiGeneratorProperties, null, typeConverter, templateBuilder);
        }
    }
}
