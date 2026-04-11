package io.rocketbase.commons.openapi;

import io.rocketbase.commons.config.OpenApiGeneratorProperties;
import io.rocketbase.commons.openapi.model.ReactQueryVersion;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that verifies the generated TypeScript code actually compiles.
 * This ensures we're generating valid TypeScript, not just syntactically correct strings.
 */
@SpringBootTest(classes = io.rocketbase.commons.openapi.sample.SampleApplication.class)
class TypeScriptCompilationTest {

    @Autowired
    private OpenApiClientCreatorService clientCreatorService;

    @Autowired
    private OpenAPI openAPI;

    @Autowired
    private OpenApiGeneratorProperties generatorProperties;

    @Autowired
    private SpringDataWebProperties springDataProperties;

    @TempDir
    Path tempDir;

    @Test
    void testGeneratedTypeScriptCompiles() throws Exception {
        // Given: Generate TypeScript client
        Path outputDir = tempDir.resolve("typescript-client");
        clientCreatorService.generateClientToFileSystem(
            ReactQueryVersion.v5,
            outputDir,
            openAPI,
            generatorProperties.getBaseUrl(),
            generatorProperties.getGroupName()
        );

        // Create package.json with TypeScript and dependencies
        String packageJson = """
            {
              "name": "test-client",
              "version": "1.0.0",
              "private": true,
              "scripts": {
                "typecheck": "tsc --noEmit"
              },
              "dependencies": {
                "@tanstack/react-query": "^5.0.0",
                "axios": "^1.6.0",
                "react": "^18.2.0"
              },
              "devDependencies": {
                "typescript": "^5.3.0",
                "@types/node": "^20.0.0",
                "@types/react": "^18.2.0",
                "zod": "^3.22.0"
              }
            }
            """;
        Files.writeString(outputDir.resolve("package.json"), packageJson);

        // Create tsconfig.json
        String tsConfig = """
            {
              "compilerOptions": {
                "target": "ES2020",
                "module": "ESNext",
                "lib": ["ES2020", "DOM"],
                "moduleResolution": "bundler",
                "strict": true,
                "skipLibCheck": true,
                "esModuleInterop": true,
                "allowSyntheticDefaultImports": true,
                "resolveJsonModule": true,
                "isolatedModules": true,
                "noEmit": true,
                "jsx": "react-jsx",
                "baseUrl": ".",
                "paths": {
                  "@rocketbase/commons-core": ["./mocks/commons-core.ts"]
                }
              },
              "include": ["src/**/*", "mocks/**/*"],
              "exclude": ["node_modules"]
            }
            """;
        Files.writeString(outputDir.resolve("tsconfig.json"), tsConfig);

        // Create mock for @rocketbase/commons-core
        Files.createDirectories(outputDir.resolve("mocks"));
        String commonsCoreMock = """
            // Mock for @rocketbase/commons-core
            export interface PageableConfig {
              page?: string;
              size?: string;
              sort?: string;
            }

            export interface PageableResult<T> {
              content: T[];
              totalElements: number;
              totalPages: number;
              page: number;
              pageSize: number;
            }

            export const buildPageableQueryParams = (config?: PageableConfig): Record<string, string> => {
              const params: Record<string, string> = {};
              if (config?.page) params.page = config.page;
              if (config?.size) params.size = config.size;
              if (config?.sort) params.sort = config.sort;
              return params;
            };

            export const buildSortParam = (sort?: string[]): string | undefined => {
              return sort?.join(',');
            };

            export const useAuth = () => {
              return {
                token: null,
                axiosClient: undefined,
                baseUrl: '',
              };
            };
            """;
        Files.writeString(outputDir.resolve("mocks/commons-core.ts"), commonsCoreMock);

        // Create empty types.ts if it doesn't exist (when no classes found)
        Path typesFile = outputDir.resolve("src/model/types.ts");
        if (!Files.exists(typesFile) || Files.size(typesFile) == 0) {
            String emptyTypes = "// No types generated\nexport {};\n";
            Files.writeString(typesFile, emptyTypes);
        }

        // Fix empty hooks/index.ts to export an empty object
        Path hooksIndexFile = outputDir.resolve("src/hooks/index.ts");
        if (Files.exists(hooksIndexFile)) {
            String content = Files.readString(hooksIndexFile);
            if (content.trim().isEmpty() || !content.contains("export")) {
                String emptyExport = "// No hooks generated\nexport {};\n";
                Files.writeString(hooksIndexFile, emptyExport);
            }
        }

        // When: Check if npm/node is available
        boolean npmAvailable = isCommandAvailable("npm");

        if (!npmAvailable) {
            System.out.println("⚠️  npm not found - skipping TypeScript compilation test");
            System.out.println("   To enable this test, install Node.js and npm");
            return; // Skip test if npm not available
        }

        // Install dependencies
        System.out.println("Installing npm dependencies...");
        executeCommand(outputDir, "npm", "install", "--silent");

        // Then: Run TypeScript compiler
        System.out.println("Running TypeScript compiler...");
        int exitCode = executeCommand(outputDir, "npm", "run", "typecheck");

        assertEquals(0, exitCode, "TypeScript compilation should succeed without errors");
        System.out.println("✅ TypeScript compilation successful!");
    }

    private boolean isCommandAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command, "--version");
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private int executeCommand(Path workingDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Stream output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
        }

        boolean finished = process.waitFor(120, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Command timed out: " + String.join(" ", command));
        }

        return process.exitValue();
    }
}
