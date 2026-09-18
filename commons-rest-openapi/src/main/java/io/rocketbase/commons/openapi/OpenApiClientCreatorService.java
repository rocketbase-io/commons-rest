package io.rocketbase.commons.openapi;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.rocketbase.commons.config.OpenApiGeneratorProperties;
import io.rocketbase.commons.openapi.model.OpenApiController;
import io.rocketbase.commons.openapi.model.ReactQueryVersion;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.utils.Constants;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class OpenApiClientCreatorService {

    protected final DataWebProperties springDataWebProperties;
    protected final OpenApiGeneratorProperties openApiGeneratorProperties;
    protected final OpenApiWebMvcResource openApiWebMvcResource;
    protected final TypeScriptTypeConverter typeConverter; // NEW: Simple type converter
    protected final InfiniteOptionsTemplateBuilder templateBuilder;
    protected final List<TypeScriptGeneratorCustomizer> typeScriptCustomizers; // NEW: Customizers

    protected Map<String, PebbleTemplate> compiledTemplateMap = new HashMap<>();

    // Cache for TypeScript generation result
    protected TypeScriptGenerationResult tsGenerationResult;

    /**
     * Constructor with customizers (injected by Spring).
     */
    public OpenApiClientCreatorService(
            DataWebProperties springDataWebProperties,
            OpenApiGeneratorProperties openApiGeneratorProperties,
            OpenApiWebMvcResource openApiWebMvcResource,
            TypeScriptTypeConverter typeConverter,
            InfiniteOptionsTemplateBuilder templateBuilder,
            List<TypeScriptGeneratorCustomizer> typeScriptCustomizers) {
        this.springDataWebProperties = springDataWebProperties;
        this.openApiGeneratorProperties = openApiGeneratorProperties;
        this.openApiWebMvcResource = openApiWebMvcResource;
        this.typeConverter = typeConverter;
        this.templateBuilder = templateBuilder;
        this.typeScriptCustomizers = typeScriptCustomizers != null ? typeScriptCustomizers : Collections.emptyList();
    }

    /**
     * Constructor without customizers (for backward compatibility and standalone use).
     */
    public OpenApiClientCreatorService(
            DataWebProperties springDataWebProperties,
            OpenApiGeneratorProperties openApiGeneratorProperties,
            OpenApiWebMvcResource openApiWebMvcResource,
            TypeScriptTypeConverter typeConverter,
            InfiniteOptionsTemplateBuilder templateBuilder) {
        this(springDataWebProperties, openApiGeneratorProperties, openApiWebMvcResource,
                typeConverter, templateBuilder, Collections.emptyList());
    }

    @SneakyThrows
    public List<OpenApiController> getControllers(HttpServletRequest request) {
        return getControllersFromOpenApi(readOpenApi(request));
    }

    /**
     * Reads the OpenAPI document springdoc exposes for the current request.
     */
    @SneakyThrows
    protected OpenAPI readOpenApi(HttpServletRequest request) {
        byte[] openapiJson = openApiWebMvcResource.openapiJson(request, Constants.DEFAULT_API_DOCS_URL, Locale.getDefault());
        return Json.mapper().readValue(openapiJson, OpenAPI.class);
    }

    /**
     * Extract controllers from OpenAPI object without HTTP request.
     * Uses the default type converter.
     */
    @SneakyThrows
    public List<OpenApiController> getControllersFromOpenApi(OpenAPI openAPI) {
        return getControllersFromOpenApi(openAPI, typeConverter);
    }

    /**
     * Generates TypeScript client directly to file system.
     * This is the new approach that uses typescript-generator for ALL types!
     *
     * @param reactQueryVersion React Query version to generate for
     * @param outputDirectory   Target directory for generated files
     * @param openAPI           OpenAPI specification
     * @param baseUrl           Base URL for API calls
     * @param groupName         Group name for generated client
     */
    public void generateClientToFileSystem(ReactQueryVersion reactQueryVersion, Path outputDirectory, OpenAPI openAPI, String baseUrl, String groupName) {
        log.info("Generating TypeScript client to: {}", outputDirectory);

        FileSystemClientWriter writer = new FileSystemClientWriter(outputDirectory);
        writer.cleanOutputDirectory();

        // STEP 1: Generate ALL TypeScript types using typescript-generator
        log.info("Step 1: Generating TypeScript types from OpenAPI schema");
        TypeScriptModelGenerator.TypeScriptGeneratorConfig tsConfig = new TypeScriptModelGenerator.TypeScriptGeneratorConfig();
        if (!openApiGeneratorProperties.getRequiredAnnotations().isEmpty()) {
            tsConfig.setRequiredAnnotations(TypeScriptModelGenerator.TypeScriptGeneratorConfig
                    .resolveRequiredAnnotations(openApiGeneratorProperties.getRequiredAnnotations()));
        }
        if (!openApiGeneratorProperties.getCustomTypeMappings().isEmpty()) {
            tsConfig.setAdditionalTypeMappings(openApiGeneratorProperties.getCustomTypeMappings());
        }
        tsConfig.setZodInferTypeExport(openApiGeneratorProperties.isZodInferTypeExport());
        tsConfig.setZodInferTypeSuffix(openApiGeneratorProperties.getZodInferTypeSuffix());
        TypeScriptModelGenerator tsGenerator = new TypeScriptModelGenerator(tsConfig, typeScriptCustomizers);
        Path typesFile = outputDirectory.resolve("src/model/types.ts");
        this.tsGenerationResult = tsGenerator.generateFromOpenAPI(openAPI, typesFile);

        // STEP 2: Create type mapper with generated types
        String modelImportPath = openApiGeneratorProperties.getSrcNavigation() + openApiGeneratorProperties.getModelFolder();
        TypeScriptTypeConverter typeConverter = new OpenApiTypeMapper(tsGenerationResult, modelImportPath);

        // STEP 3: Extract controllers using the type mapper
        List<OpenApiController> controllers = getControllersFromOpenApi(openAPI, typeConverter);

        Map<String, Object> context = new HashMap<>();
        context.put("controllers", controllers);
        context.put("baseUrl", baseUrl);
        context.put("groupName", groupName);
        context.put("configuredGroupVar", Introspector.decapitalize(groupName));
        context.put("timestamp", Instant.now());
        context.put("generatorConfig", openApiGeneratorProperties);
        context.put("springDataWebConfig", springDataWebProperties);
        context.put("reactQueryVersion", reactQueryVersion);

        try {
            // STEP 4: Generate model index (re-exports generated types)
            generateModelsToFileSystem(writer, context);

            // STEP 5: Generate clients
            generateClientsToFileSystem(controllers, writer, context);

            // STEP 6: Generate hooks
            generateHooksToFileSystem(controllers, writer, context);

            // STEP 7: Generate Zod schemas for mutations
            generateZodSchemasToFileSystem(writer, openAPI, outputDirectory, tsConfig);

            // STEP 8: Generate index and package.json
            generateIndexAndPackageJsonToFileSystem(writer, context);

            log.info("Successfully generated TypeScript client to: {}", outputDirectory);
        } catch (Exception e) {
            log.error("Failed to generate TypeScript client", e);
            throw new RuntimeException("Failed to generate TypeScript client", e);
        }
    }

    /**
     * Extract controllers with custom type converter.
     */
    @SneakyThrows
    public List<OpenApiController> getControllersFromOpenApi(OpenAPI openAPI, TypeScriptTypeConverter typeConverter) {
        Map<String, List<OpenApiControllerMethodExtraction>> remapped = new HashMap<>();
        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            addOperation(remapped, PathItem.HttpMethod.GET, entry.getKey(), entry.getValue().getGet(), typeConverter);
            addOperation(remapped, PathItem.HttpMethod.PUT, entry.getKey(), entry.getValue().getPut(), typeConverter);
            addOperation(remapped, PathItem.HttpMethod.POST, entry.getKey(), entry.getValue().getPost(), typeConverter);
            addOperation(remapped, PathItem.HttpMethod.DELETE, entry.getKey(), entry.getValue().getDelete(), typeConverter);
            addOperation(remapped, PathItem.HttpMethod.PATCH, entry.getKey(), entry.getValue().getPatch(), typeConverter);
        }
        return remapped.entrySet().stream()
                .map(e -> new OpenApiController(e.getKey(), e.getValue(), typeConverter))
                .collect(Collectors.toList());
    }

    protected void generateModelsToFileSystem(FileSystemClientWriter writer, Map<String, Object> context) throws IOException {
        writer.createDirectory("src/model");

        String modelIndex = evaluateTemplate("model/index", context);
        writer.writeFile("src/model/index.ts", modelIndex);

        String modelRequest = evaluateTemplate("model/request", context);
        writer.writeFile("src/model/request.ts", modelRequest);
    }

    protected void generateClientsToFileSystem(List<OpenApiController> controllers, FileSystemClientWriter writer, Map<String, Object> context) throws IOException {
        writer.createDirectory("src/" + openApiGeneratorProperties.getClientFolder());

        for (OpenApiController c : controllers) {
            context.put("controller", c);
            String content = evaluateTemplate("client/controller-template", context);
            writer.writeFile("src/" + openApiGeneratorProperties.getClientFolder() + "/" + c.getFilename() + ".ts", content);
        }

        String clientIndex = evaluateTemplate("client/index", context);
        writer.writeFile("src/" + openApiGeneratorProperties.getClientFolder() + "/index.ts", clientIndex);
    }

    protected void generateHooksToFileSystem(List<OpenApiController> controllers, FileSystemClientWriter writer, Map<String, Object> context) throws IOException {
        writer.createDirectory("src/" + openApiGeneratorProperties.getHookFolder());

        for (OpenApiController c : controllers) {
            context.put("controller", c);
            String content = evaluateTemplate("hook/hook-template", context);
            writer.writeFile("src/" + openApiGeneratorProperties.getHookFolder() + "/" + c.getFilename() + ".ts", content);
        }

        String hookIndex = evaluateTemplate("hook/index", context);
        writer.writeFile("src/" + openApiGeneratorProperties.getHookFolder() + "/index.ts", hookIndex);
    }

    protected void generateIndexAndPackageJsonToFileSystem(FileSystemClientWriter writer, Map<String, Object> context) throws IOException {
        writer.createDirectory("src");

        String packageJson = evaluateTemplate("package", context);
        writer.writeFile("package.json", packageJson);

        String util = evaluateTemplate("util", context);
        writer.writeFile("src/util.ts", util);

        String index = evaluateTemplate("index", context);
        writer.writeFile("src/index.ts", index);
    }

    /**
     * Generates Zod schemas for mutation commands. Passes the {@link TypeScriptGenerationResult}
     * so the Zod generator knows which Java types were remapped to TS primitives (e.g. Tsid → string)
     * vs. kept as TS enums/interfaces — without it, the output references {@code Types.Tsid} or bare
     * enum names that don't exist in {@code types.ts}.
     */
    protected void generateZodSchemasToFileSystem(FileSystemClientWriter writer, OpenAPI openAPI, Path outputDirectory, TypeScriptModelGenerator.TypeScriptGeneratorConfig tsConfig) {
        try {
            log.info("Generating Zod schemas for mutations");
            ZodSchemaGenerator zodGenerator = new ZodSchemaGenerator(openAPI, tsConfig, tsGenerationResult);
            Path zodFile = outputDirectory.resolve("src/model/zod-schemas.ts");
            zodGenerator.generateZodSchemas(zodFile);
        } catch (Exception e) {
            log.warn("Failed to generate Zod schemas (non-fatal): {}", e.getMessage(), e);
        }
    }

    /**
     * Evaluates a Pebble template with the given context.
     */
    protected String evaluateTemplate(String templateName, Map<String, Object> context) throws IOException {
        Writer writer = new StringWriter();
        getCompiledTemplate(templateName).evaluate(writer, context);
        return writer.toString();
    }

    /**
     * Streams a generated client as a zip.
     * <p>
     * Delegates to {@link #generateClientToFileSystem} in a temporary directory and zips its
     * output, so the download contains exactly what the file-system generator produces —
     * including {@code src/model/types.ts} and {@code src/model/zod-schemas.ts}. The previous
     * implementation rendered the templates directly into the zip with the injected
     * {@link #typeConverter}, whose {@code TypeScriptGenerationResult} is empty: no models were
     * emitted at all and every unmapped type kept its fully qualified Java name
     * (e.g. {@code RequestorBuilder<..., io.rocketbase.commons.dto.PageableResult<...>>}),
     * which is not valid TypeScript.
     */
    public void getTypescriptClients(ReactQueryVersion reactQueryVersion, HttpServletRequest request, HttpServletResponse response, String baseUrl, String groupName, String filename) {

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);
        response.setStatus(HttpServletResponse.SC_OK);

        Path tempDirectory = null;
        try {
            OpenAPI openAPI = readOpenApi(request);

            tempDirectory = Files.createTempDirectory("commons-rest-client-");
            generateClientToFileSystem(reactQueryVersion, tempDirectory, openAPI, baseUrl, groupName);

            try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
                zipDirectory(tempDirectory, zippedOut);
                zippedOut.finish();
            }
        } catch (Exception e) {
            // Exception handling goes here
            log.error("write zip: {}", e.getMessage(), e);
        } finally {
            deleteRecursively(tempDirectory);
        }
    }

    /**
     * Adds every file below {@code root} to the zip, keeping the relative layout.
     */
    protected void zipDirectory(Path root, ZipOutputStream zippedOut) throws IOException {
        List<Path> files;
        try (Stream<Path> walk = Files.walk(root)) {
            files = walk.filter(Files::isRegularFile).sorted().collect(Collectors.toList());
        }
        for (Path file : files) {
            String entryName = root.relativize(file).toString().replace(File.separatorChar, '/');
            zippedOut.putNextEntry(new ZipEntry(entryName));
            Files.copy(file, zippedOut);
            zippedOut.closeEntry();
        }
    }

    protected void deleteRecursively(Path directory) {
        if (directory == null) {
            return;
        }
        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.debug("could not delete {}: {}", path, e.getMessage());
                }
            });
        } catch (IOException e) {
            log.debug("could not clean up {}: {}", directory, e.getMessage());
        }
    }

    protected void generateModels(ZipOutputStream zippedOut, Map<String, Object> context) throws IOException {
        Writer writer;
        zippedOut.putNextEntry(new ZipEntry("src/model/"));

        writer = new StringWriter();
        zippedOut.putNextEntry(new ZipEntry("src/model/index.ts"));
        getCompiledTemplate("model/index").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();

        writer = new StringWriter();
        zippedOut.putNextEntry(new ZipEntry("src/model/request.ts"));
        getCompiledTemplate("model/request").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();
    }

    protected void generateIndexAndPackageJson(ZipOutputStream zippedOut, Map<String, Object> context) throws IOException {
        Writer writer;
        zippedOut.putNextEntry(new ZipEntry("src/"));

        writer = new StringWriter();
        zippedOut.putNextEntry(new ZipEntry("package.json"));
        getCompiledTemplate("package").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();


        zippedOut.putNextEntry(new ZipEntry("src/util.ts"));
        writer = new StringWriter();
        getCompiledTemplate("util").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();

        writer = new StringWriter();
        zippedOut.putNextEntry(new ZipEntry("src/index.ts"));
        getCompiledTemplate("index").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();
    }

    protected void generateClients(List<OpenApiController> controllers, ZipOutputStream zippedOut, Map<String, Object> context) throws IOException {
        Writer writer;
        zippedOut.putNextEntry(new ZipEntry("src/" + openApiGeneratorProperties.getClientFolder() + "/"));
        for (OpenApiController c : controllers) {
            zippedOut.putNextEntry(new ZipEntry("src/" + openApiGeneratorProperties.getClientFolder() + "/" + c.getFilename() + ".ts"));
            writer = new StringWriter();

            context.put("controller", c);
            getCompiledTemplate("client/controller-template").evaluate(writer, context);
            zippedOut.write(writer.toString().getBytes("UTF-8"));
            zippedOut.closeEntry();
        }

        zippedOut.putNextEntry(new ZipEntry("src/" + openApiGeneratorProperties.getClientFolder() + "/index.ts"));
        writer = new StringWriter();
        getCompiledTemplate("client/index").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();
    }

    protected void generateHooks(ReactQueryVersion reactQueryVersion, List<OpenApiController> controllers, ZipOutputStream zippedOut, Map<String, Object> context) throws IOException {
        Writer writer;
        zippedOut.putNextEntry(new ZipEntry("src/" + openApiGeneratorProperties.getHookFolder() + "/"));
        for (OpenApiController c : controllers) {
            zippedOut.putNextEntry(new ZipEntry("src/" + openApiGeneratorProperties.getHookFolder() + "/" + c.getFilename() + ".ts"));

            writer = new StringWriter();
            context.put("controller", c);
            getCompiledTemplate("hook/hook-template").evaluate(writer, context);
            zippedOut.write(writer.toString().getBytes("UTF-8"));
            zippedOut.closeEntry();
        }

        zippedOut.putNextEntry(new ZipEntry("src/" + openApiGeneratorProperties.getHookFolder() + "/index.ts"));
        writer = new StringWriter();
        getCompiledTemplate("hook/index").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();
    }

    protected PebbleTemplate getCompiledTemplate(String keyName) {
        if (!compiledTemplateMap.containsKey(keyName)) {
            ClasspathLoader classpathLoader = new ClasspathLoader();
            classpathLoader.setPrefix("templates/");
            classpathLoader.setSuffix(".pebble");

            PebbleEngine engine = new PebbleEngine.Builder()
                    .loader(classpathLoader)
                    .autoEscaping(false)
                    .extension(new AbstractExtension() {
                        @Override
                        public Map<String, Function> getFunctions() {
                            return Map.of("infiniteOptions", new InfiniteOptions(),
                                    "infiniteParams", new InfiniteParams());
                        }
                    })
                    .build();
            compiledTemplateMap.put(keyName, engine.getTemplate(keyName));
        }
        return compiledTemplateMap.get(keyName);
    }

    public class InfiniteOptions implements Function {

        @Override
        public Object execute(Map<String, Object> map, PebbleTemplate pebbleTemplate, EvaluationContext evaluationContext, int i) {
            Object parameter = map.getOrDefault("method", null);
            if (parameter instanceof OpenApiControllerMethodExtraction) {
                return templateBuilder.buildQueryOptions((OpenApiControllerMethodExtraction) parameter);
            } else {
                log.error("parameter not correctly used for infinite options template");
                return "";
            }
        }

        @Override
        public List<String> getArgumentNames() {
            return List.of("method");
        }
    }

    public class InfiniteParams implements Function {

        @Override
        public Object execute(Map<String, Object> map, PebbleTemplate pebbleTemplate, EvaluationContext evaluationContext, int i) {
            Object parameter = map.getOrDefault("method", null);
            if (parameter instanceof OpenApiControllerMethodExtraction) {
                return templateBuilder.buildQueryParams((OpenApiControllerMethodExtraction) parameter);
            } else {
                log.error("parameter not correctly used for infinite params template");
                return "";
            }
        }

        @Override
        public List<String> getArgumentNames() {
            return List.of("method");
        }
    }

    protected Set<String> pageParams() {
        return Set.of(springDataWebProperties.getPageable().getPageParameter(), springDataWebProperties.getPageable().getSizeParameter(), springDataWebProperties.getSort().getSortParameter());
    }

    protected void addOperation(Map<String, List<OpenApiControllerMethodExtraction>> map, PathItem.HttpMethod httpMethod, String path, Operation operation) {
        addOperation(map, httpMethod, path, operation, typeConverter);
    }

    protected void addOperation(Map<String, List<OpenApiControllerMethodExtraction>> map, PathItem.HttpMethod httpMethod, String path, Operation operation, TypeScriptTypeConverter typeConverter) {
        if (operation != null && operation.getExtensions() != null) {
            String controllerBean = (String) operation.getExtensions().get(OpenApiCustomExtractor.CONTROLLER_BEAN);
            boolean disabled = (Boolean) operation.getExtensions().getOrDefault(OpenApiCustomExtractor.DISABLED, false);
            if (!disabled) {
                map.putIfAbsent(controllerBean, new ArrayList<>());
                map.get(controllerBean).add(new OpenApiControllerMethodExtraction(
                        new OpenApiControllerMethodExtraction.ExtractorConfig(pageParams(), typeConverter, httpMethod, path, operation, openApiGeneratorProperties.getDefaultStaleTime())
                ));
            }
        }
    }
}
