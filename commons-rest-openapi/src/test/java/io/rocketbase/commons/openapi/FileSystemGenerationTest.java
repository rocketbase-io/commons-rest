package io.rocketbase.commons.openapi;

import io.rocketbase.commons.config.OpenApiGeneratorProperties;
import io.rocketbase.commons.openapi.model.ReactQueryVersion;
import io.rocketbase.commons.openapi.sample.SampleApplication;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for file system based TypeScript client generation.
 * Demonstrates the new approach without ZIP files and bash scripts.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SampleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileSystemGenerationTest {

    @Value("http://localhost:${local.server.port}")
    protected String baseUrl;

    @Autowired
    protected OpenApiClientCreatorService openApiClientCreatorService;

    @Autowired
    protected OpenApiGeneratorProperties generatorProperties;

    @Autowired
    protected SpringDataWebProperties springDataProperties;

    @TempDir
    Path tempDir;

    @Test
    public void testGenerateClientToFileSystem() throws Exception {
        // Download OpenAPI spec
        File openapiFile = tempDir.resolve("openapi.json").toFile();
        download(new URL(baseUrl + "/v3/api-docs"), openapiFile);
        log.info("Downloaded OpenAPI spec to: {}", openapiFile.getAbsolutePath());

        // Parse OpenAPI
        OpenAPI openAPI = Json.mapper().readValue(openapiFile, OpenAPI.class);

        // Generate client to file system
        Path outputDir = tempDir.resolve("typescript-client");
        openApiClientCreatorService.generateClientToFileSystem(
            ReactQueryVersion.v5,
            outputDir,
            openAPI,
            "/api",
            "TestApi"
        );

        // Verify generated files exist
        assertTrue(Files.exists(outputDir.resolve("package.json")), "package.json should exist");
        assertTrue(Files.exists(outputDir.resolve("src/index.ts")), "src/index.ts should exist");
        assertTrue(Files.exists(outputDir.resolve("src/util.ts")), "src/util.ts should exist");
        assertTrue(Files.exists(outputDir.resolve("src/model/index.ts")), "src/model/index.ts should exist");
        assertTrue(Files.exists(outputDir.resolve("src/model/types.ts")), "src/model/types.ts should exist (NEW!)");
        assertTrue(Files.exists(outputDir.resolve("src/model/request.ts")), "src/model/request.ts should exist");
        assertTrue(Files.isDirectory(outputDir.resolve("src/clients")), "src/clients directory should exist");
        assertTrue(Files.isDirectory(outputDir.resolve("src/hooks")), "src/hooks directory should exist");

        log.info("Successfully generated TypeScript client to: {}", outputDir);
        log.info("Generated files:");
        Files.walk(outputDir)
            .filter(Files::isRegularFile)
            .forEach(file -> log.info("  - {}", outputDir.relativize(file)));
    }

    @Test
    public void testGenerateForDifferentReactQueryVersions() throws Exception {
        // Download OpenAPI spec
        File openapiFile = tempDir.resolve("openapi.json").toFile();
        download(new URL(baseUrl + "/v3/api-docs"), openapiFile);

        OpenAPI openAPI = Json.mapper().readValue(openapiFile, OpenAPI.class);

        // Test all React Query versions
        for (ReactQueryVersion version : ReactQueryVersion.values()) {
            Path outputDir = tempDir.resolve("client-" + version.name().toLowerCase());

            openApiClientCreatorService.generateClientToFileSystem(
                version,
                outputDir,
                openAPI,
                "/api",
                "TestApi"
            );

            assertTrue(Files.exists(outputDir.resolve("package.json")),
                "package.json should exist for " + version);

            // Read and verify package.json contains correct dependencies
            String packageJson = Files.readString(outputDir.resolve("package.json"));
            // v3 uses "react-query", v4 and v5 use "@tanstack/react-query"
            String expectedPackage = version == ReactQueryVersion.v3 ? "react-query" : "@tanstack/react-query";
            assertTrue(packageJson.contains(expectedPackage),
                "package.json should reference " + expectedPackage + " for " + version);

            log.info("Successfully generated client for React Query {}", version);
        }
    }

    @Test
    public void testFileSystemClientWriter() throws Exception {
        Path testDir = tempDir.resolve("writer-test");
        FileSystemClientWriter writer = new FileSystemClientWriter(testDir);

        // Clean and create directory
        writer.cleanOutputDirectory();
        assertTrue(Files.exists(testDir), "Directory should be created");

        // Write files
        writer.writeFile("test.ts", "export const test = 'hello';");
        writer.writeFile("nested/deep/file.ts", "export const nested = true;");

        assertTrue(writer.fileExists("test.ts"), "test.ts should exist");
        assertTrue(writer.fileExists("nested/deep/file.ts"), "nested file should exist");

        String content = Files.readString(writer.getAbsolutePath("test.ts"));
        assertTrue(content.contains("hello"), "File content should match");

        log.info("FileSystemClientWriter test successful");
    }

    private static void download(URL url, File file) throws IOException {
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }
}
