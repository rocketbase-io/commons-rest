package io.rocketbase.commons.openapi;

import io.rocketbase.commons.config.OpenApiGeneratorProperties;
import io.rocketbase.commons.openapi.model.ReactQueryVersion;
import io.rocketbase.commons.openapi.util.ReflectionPropertyHelper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static io.rocketbase.commons.openapi.util.ReflectionPropertyHelper.setField;

/**
 * Standalone tool to generate TypeScript client from OpenAPI spec.
 *
 * Usage:
 * mvn exec:java -Dexec.mainClass="io.rocketbase.commons.openapi.StandaloneClientGenerator" \
 *               -Dexec.args="path/to/openapi.json output-dir v5 /api MyApi"
 *
 * Or simpler, if openapi.json is in target/:
 * mvn exec:java -Dexec.mainClass="io.rocketbase.commons.openapi.StandaloneClientGenerator"
 */
public class StandaloneClientGenerator {

    public static void main(String[] args) throws Exception {
        // Parse arguments with defaults
        String openapiFilePath = args.length > 0 ? args[0] : "target/openapi.json";
        String outputDirPath = args.length > 1 ? args[1] : "target/typescript-client";
        String reactQueryVersion = args.length > 2 ? args[2] : "v5";
        String baseUrl = args.length > 3 ? args[3] : "/api";
        String groupName = args.length > 4 ? args[4] : "Api";

        System.out.println("=".repeat(80));
        System.out.println("TypeScript Client Generator (Standalone)");
        System.out.println("=".repeat(80));
        System.out.println("OpenAPI file:      " + openapiFilePath);
        System.out.println("Output directory:  " + outputDirPath);
        System.out.println("React Query:       " + reactQueryVersion);
        System.out.println("Base URL:          " + baseUrl);
        System.out.println("Group name:        " + groupName);
        System.out.println("=".repeat(80));

        // Check if OpenAPI file exists
        File openapiFile = new File(openapiFilePath);
        if (!openapiFile.exists()) {
            System.err.println("ERROR: OpenAPI file not found: " + openapiFile.getAbsolutePath());
            System.err.println();
            System.err.println("Please provide a valid OpenAPI JSON file.");
            System.err.println();
            System.err.println("You can export it from your running application:");
            System.err.println("  curl http://localhost:8080/v3/api-docs > target/openapi.json");
            System.err.println();
            System.err.println("Or generate it during build with springdoc-openapi-maven-plugin");
            System.exit(1);
        }

        // Read OpenAPI specification
        System.out.println("Reading OpenAPI specification...");
        OpenAPI openAPI = Json.mapper().readValue(openapiFile, OpenAPI.class);
        System.out.println("✓ OpenAPI spec loaded successfully");

        // Create configuration
        OpenApiGeneratorProperties generatorProperties = createGeneratorProperties(baseUrl, groupName);
        SpringDataWebProperties springDataProperties = createSpringDataProperties();

        // Create services (without Spring context)
        TypeScriptTypeConverter typeConverter = new OpenApiTypeMapper(new TypeScriptGenerationResult("", new HashMap<>()));
        DefaultInfiniteOptionsTemplateBuilder templateBuilder = new DefaultInfiniteOptionsTemplateBuilder();

        OpenApiClientCreatorService creatorService = new OpenApiClientCreatorService(
            springDataProperties,
            generatorProperties,
            null, // OpenApiWebMvcResource not available in standalone mode
            typeConverter,
            templateBuilder
        );

        // Generate client
        ReactQueryVersion version = ReactQueryVersion.parse(reactQueryVersion);
        Path outputPath = Paths.get(outputDirPath);

        System.out.println("Generating TypeScript client...");
        creatorService.generateClientToFileSystem(version, outputPath, openAPI, baseUrl, groupName);

        System.out.println("=".repeat(80));
        System.out.println("✓ SUCCESS! TypeScript client generated to:");
        System.out.println("  " + outputPath.toAbsolutePath());
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("Next steps:");
        System.out.println("  cd " + outputPath);
        System.out.println("  npm install");
        System.out.println("  npm run build");
        System.out.println();
    }

    private static OpenApiGeneratorProperties createGeneratorProperties(String baseUrl, String groupName) {
        try {
            OpenApiGeneratorProperties props = OpenApiGeneratorProperties.class.getDeclaredConstructor().newInstance();
            setField(props, "baseUrl", baseUrl);
            setField(props, "groupName", groupName);
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create OpenApiGeneratorProperties", e);
        }
    }

    private static SpringDataWebProperties createSpringDataProperties() {
        return ReflectionPropertyHelper.createDefaultSpringDataProperties();
    }
}
