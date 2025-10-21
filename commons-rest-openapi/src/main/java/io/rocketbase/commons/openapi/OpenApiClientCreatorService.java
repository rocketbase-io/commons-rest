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
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.utils.Constants;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;

import java.beans.Introspector;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RequiredArgsConstructor
public class OpenApiClientCreatorService {

    protected final SpringDataWebProperties springDataWebProperties;
    protected final OpenApiGeneratorProperties openApiGeneratorProperties;
    protected final OpenApiWebMvcResource openApiWebMvcResource;
    protected final OpenApiConverter typescriptConverter;
    protected final InfiniteOptionsTemplateBuilder templateBuilder;

    protected Map<String, PebbleTemplate> compiledTemplateMap = new HashMap<>();

    @SneakyThrows
    public List<OpenApiController> getControllers(HttpServletRequest request) {
        byte[] openapiJson = openApiWebMvcResource.openapiJson(request, Constants.DEFAULT_API_DOCS_URL, Locale.getDefault());
        OpenAPI openAPI = Json.mapper().readValue(openapiJson, OpenAPI.class);
        Map<String, List<OpenApiControllerMethodExtraction>> remapped = new HashMap<>();
        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            addOperation(remapped, PathItem.HttpMethod.GET, entry.getKey(), entry.getValue().getGet());
            addOperation(remapped, PathItem.HttpMethod.PUT, entry.getKey(), entry.getValue().getPut());
            addOperation(remapped, PathItem.HttpMethod.POST, entry.getKey(), entry.getValue().getPost());
            addOperation(remapped, PathItem.HttpMethod.DELETE, entry.getKey(), entry.getValue().getDelete());
            addOperation(remapped, PathItem.HttpMethod.PATCH, entry.getKey(), entry.getValue().getPatch());
        }
        return remapped.entrySet().stream().map(e -> new OpenApiController(e.getKey(), e.getValue(), typescriptConverter)).collect(Collectors.toList());
    }

    public void getTypescriptClients(ReactQueryVersion reactQueryVersion, HttpServletRequest request, HttpServletResponse response, String baseUrl, String groupName, String filename) {

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);
        response.setStatus(HttpServletResponse.SC_OK);

        List<OpenApiController> controllers = getControllers(request);

        try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
            Map<String, Object> context = new HashMap<>();
            context.put("controllers", controllers);
            context.put("baseUrl", baseUrl);
            context.put("groupName", groupName);
            context.put("configuredGroupVar", Introspector.decapitalize(groupName));
            context.put("timestamp", Instant.now());
            context.put("generatorConfig", openApiGeneratorProperties);
            context.put("springDataWebConfig", springDataWebProperties);
            context.put("reactQueryVersion", reactQueryVersion);

            generateModels(zippedOut, context);
            generateClients(controllers, zippedOut, context);
            generateHooks(reactQueryVersion, controllers, zippedOut, context);
            generateIndexAndPackageJson(zippedOut, context);


            zippedOut.finish();
        } catch (Exception e) {
            // Exception handling goes here
            log.error("write zip: {}", e.getMessage(), e);
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
        if (operation != null && operation.getExtensions() != null) {
            String controllerBean = (String) operation.getExtensions().get(OpenApiCustomExtractor.CONTROLLER_BEAN);
            boolean disabled = (Boolean) operation.getExtensions().getOrDefault(OpenApiCustomExtractor.DISABLED, false);
            if (!disabled) {
                map.putIfAbsent(controllerBean, new ArrayList<>());
                map.get(controllerBean).add(new OpenApiControllerMethodExtraction(new OpenApiControllerMethodExtraction.ExtractorConfig(pageParams(), typescriptConverter, httpMethod, path, operation, openApiGeneratorProperties.getDefaultStaleTime())));
            }
        }
    }
}
