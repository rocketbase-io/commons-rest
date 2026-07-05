package io.rocketbase.commons.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties("commons.openapi.generator")
public class OpenApiGeneratorProperties {

    private String baseUrl = "/api";
    private String packageName = "openapi-module";
    private String groupName = "ModuleApi";
    private String hookFolder = "hooks";
    private String clientFolder = "clients";
    private String modelFolder = "model";
    private String srcNavigation = "../../";
    private String createPaginationOptions = "../util";
    private int defaultStaleTime = 2;

    private List<String> modelImports = new ArrayList<>();

    /**
     * Enable file system generation (writes to target directory).
     * When true, the client will be generated directly to the file system.
     * Default: true
     */
    private boolean enableFileSystemGeneration = true;

    /**
     * Output directory for generated TypeScript client.
     * Relative to project build directory.
     * Default: "typescript-client"
     */
    private String outputDirectory = "typescript-client";

    /**
     * Class patterns to scan for TypeScript model generation.
     * Example: ["io.rocketbase.commons.**.dto.**", "com.example.**.model.**"]
     */
    private List<String> classPatterns = new ArrayList<>();

    /**
     * Additional custom type mappings for TypeScript generation.
     * Key: Java class name (e.g., "com.example.CustomType")
     * Value: TypeScript type (e.g., "string" or "CustomInterface")
     */
    private Map<String, String> customTypeMappings = new HashMap<>();

    /**
     * Fully qualified annotation names that mark a field as required in the generated
     * TypeScript types and Zod schemas — all other fields render optional.
     * Empty (default) uses the built-in list: the not-null implying jakarta.validation
     * constraints (@NotNull, @NotBlank, @NotEmpty) plus org.springframework.lang.NonNull,
     * jakarta.annotation.Nonnull and org.jspecify.annotations.NonNull when on the classpath.
     * When set, this list REPLACES the defaults (allows going stricter as well).
     */
    private List<String> requiredAnnotations = new ArrayList<>();

    /**
     * Whether the Zod generator emits an inferred TypeScript type alias next to each schema
     * (export type X = z.infer&lt;typeof XSchema&gt;). Off by default — the name would collide
     * with the same-named interface in types.ts; enable together with zodInferTypeSuffix.
     */
    private boolean zodInferTypeExport = false;

    /**
     * Suffix appended to the inferred Zod type name when zodInferTypeExport is on,
     * e.g. "Zod" yields UserCreateCmdZod. Empty by default.
     */
    private String zodInferTypeSuffix = "";

    /**
     * Gets the full output path for generated client.
     * @param buildDirectory The Maven build directory (typically "target")
     */
    public Path getOutputPath(Path buildDirectory) {
        return buildDirectory.resolve(outputDirectory);
    }

    /**
     * Gets the output path as string.
     * @param buildDirectory The Maven build directory path
     */
    public String getOutputPathString(String buildDirectory) {
        return Paths.get(buildDirectory).resolve(outputDirectory).toString();
    }
}
