package io.rocketbase.commons.openapi;

import com.google.common.collect.Sets;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import io.rocketbase.commons.config.OpenApiGeneratorProperties;
import io.rocketbase.commons.openapi.model.OpenApiController;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.Constants;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private final SpringDataWebProperties springDataWebProperties;
    private final OpenApiGeneratorProperties openApiGeneratorProperties;
    private final OpenApiWebMvcResource openApiWebMvcResource;
    private final OpenApiConverter typescriptConverter;
    private final InfiniteOptionsTemplateBuilder templateBuilder;

    private Map<String, PebbleTemplate> compiledTemplateMap = new HashMap<>();

    @SneakyThrows
    public List<OpenApiController> getControllers(HttpServletRequest request) {
        OpenAPI openAPI = Json.mapper().readValue(openApiWebMvcResource.openapiJson(request, Constants.DEFAULT_API_DOCS_URL, Locale.GERMAN), OpenAPI.class);
        Map<String, List<OpenApiControllerMethodExtraction>> remapped = new HashMap<>();
        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            addOperation(remapped, PathItem.HttpMethod.GET, entry.getKey(), entry.getValue().getGet());
            addOperation(remapped, PathItem.HttpMethod.PUT, entry.getKey(), entry.getValue().getPut());
            addOperation(remapped, PathItem.HttpMethod.POST, entry.getKey(), entry.getValue().getPost());
            addOperation(remapped, PathItem.HttpMethod.DELETE, entry.getKey(), entry.getValue().getDelete());
            addOperation(remapped, PathItem.HttpMethod.PATCH, entry.getKey(), entry.getValue().getPatch());
        }
        return remapped.entrySet().stream().map(e -> new OpenApiController(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public void getTypescriptClients(HttpServletRequest request, HttpServletResponse response, String baseUrl, String groupName, String filename) {

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

            if (openApiGeneratorProperties.isModelCreate()) {
                generateModels(zippedOut, context);
            }
            generateClients(controllers, zippedOut, context);
            generateHooks(controllers, zippedOut, context);
            generateUtil(zippedOut, context);
            generateIndex(zippedOut, context);


            zippedOut.finish();
        } catch (Exception e) {
            // Exception handling goes here
            log.error("write zip: {}", e.getMessage(), e);
        }
    }

    private void generateModels(ZipOutputStream zippedOut, Map<String, Object> context) throws IOException {
        Writer writer;
        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getModelFolder() + "/"));

        writer = new StringWriter();
        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getModelFolder() + "/commons-rest-api.ts"));
        getCompiledTemplate("model/commons-rest-api").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();

        writer = new StringWriter();
        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getModelFolder() + "/index.ts"));
        getCompiledTemplate("model/index").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();

        writer = new StringWriter();
        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getModelFolder() + "/request.ts"));
        getCompiledTemplate("model/request").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();
    }

    private void generateUtil(ZipOutputStream zippedOut, Map<String, Object> context) throws IOException {
        Writer writer;
        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getUtilFolder() + "/"));

        writer = new StringWriter();
        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getUtilFolder() + "/index.ts"));
        getCompiledTemplate("util/index").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();

        writer = new StringWriter();
        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getUtilFolder() + "/infinite-options.ts"));
        getCompiledTemplate("util/infinite-options").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();

        writer = new StringWriter();
        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getUtilFolder() + "/requestor.ts"));
        getCompiledTemplate("util/requestor").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();

        writer = new StringWriter();
        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getUtilFolder() + "/total-elements.ts"));
        getCompiledTemplate("util/total-elements").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();
    }

    private void generateIndex(ZipOutputStream zippedOut, Map<String, Object> context) throws IOException {
        Writer writer;
        zippedOut.putNextEntry(new ZipEntry("/"));

        writer = new StringWriter();
        zippedOut.putNextEntry(new ZipEntry("/index.ts"));
        getCompiledTemplate("index").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();
    }

    private void generateClients(List<OpenApiController> controllers, ZipOutputStream zippedOut, Map<String, Object> context) throws IOException {
        Writer writer;
        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getClientFolder() + "/"));
        for (OpenApiController c : controllers) {
            zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getClientFolder() + "/" + c.getFilename() + ".ts"));
            writer = new StringWriter();

            context.put("controller", c);
            getCompiledTemplate("client/controller-template").evaluate(writer, context);
            zippedOut.write(writer.toString().getBytes("UTF-8"));
            zippedOut.closeEntry();
        }

        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getClientFolder() + "/index.ts"));
        writer = new StringWriter();
        getCompiledTemplate("client/index").evaluate(writer, context);
        zippedOut.write(writer.toString().getBytes("UTF-8"));
        zippedOut.closeEntry();
    }

    private void generateHooks(List<OpenApiController> controllers, ZipOutputStream zippedOut, Map<String, Object> context) throws IOException {
        Writer writer;
        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getHookFolder() + "/"));
        for (OpenApiController c : controllers) {
            zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getHookFolder() + "/" + c.getFilename() + ".ts"));

            writer = new StringWriter();
            context.put("controller", c);
            getCompiledTemplate("hook/hook-template").evaluate(writer, context);
            zippedOut.write(writer.toString().getBytes("UTF-8"));
            zippedOut.closeEntry();
        }

        zippedOut.putNextEntry(new ZipEntry(openApiGeneratorProperties.getHookFolder() + "/index.ts"));
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
        return Sets.newHashSet(springDataWebProperties.getPageable().getPageParameter(), springDataWebProperties.getPageable().getSizeParameter(), springDataWebProperties.getSort().getSortParameter());
    }

    protected void addOperation(Map<String, List<OpenApiControllerMethodExtraction>> map, PathItem.HttpMethod httpMethod, String path, Operation operation) {
        if (operation != null && operation.getExtensions() != null) {
            String controllerBean = (String) operation.getExtensions().get(OpenApiCustomExtractor.CONTROLLER_BEAN);
            boolean disabled = (Boolean) operation.getExtensions().getOrDefault(OpenApiCustomExtractor.DISABLED, false);
            if (!disabled) {
                map.putIfAbsent(controllerBean, new ArrayList<>());
                map.get(controllerBean).add(new OpenApiControllerMethodExtraction(new OpenApiControllerMethodExtraction.ExtractorConfig(pageParams(), typescriptConverter, httpMethod, path, operation)));
            }
        }
    }
}