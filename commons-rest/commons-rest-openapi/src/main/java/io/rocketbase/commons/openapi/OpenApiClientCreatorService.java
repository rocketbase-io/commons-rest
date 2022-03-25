package io.rocketbase.commons.openapi;

import com.google.common.collect.Sets;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
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

    @SneakyThrows
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

            Writer writer;

            zippedOut.putNextEntry(new ZipEntry("rest/"));

            for (OpenApiController c : controllers) {
                zippedOut.putNextEntry(new ZipEntry("rest/" + c.getFilename() + ".ts"));
                writer = new StringWriter();

                context.put("controller", c);
                getCompiledTemplate("typescript-client-controller-template").evaluate(writer, context);
                zippedOut.write(writer.toString().getBytes("UTF-8"));
                zippedOut.closeEntry();
            }
            zippedOut.putNextEntry(new ZipEntry("rest/index.ts"));
            writer = new StringWriter();
            getCompiledTemplate("typescript-client-index-template").evaluate(writer, context);
            zippedOut.write(writer.toString().getBytes("UTF-8"));
            zippedOut.closeEntry();


            zippedOut.putNextEntry(new ZipEntry("hook/"));
            for (OpenApiController c : controllers) {
                zippedOut.putNextEntry(new ZipEntry("hook/" + c.getFilename() + ".ts"));

                writer = new StringWriter();
                context.put("controller", c);
                getCompiledTemplate("typescript-react-query-hook-template").evaluate(writer, context);
                zippedOut.write(writer.toString().getBytes("UTF-8"));
                zippedOut.closeEntry();
            }

            zippedOut.putNextEntry(new ZipEntry("hook/index.ts"));
            writer = new StringWriter();
            getCompiledTemplate("typescript-hook-index-template").evaluate(writer, context);
            zippedOut.write(writer.toString().getBytes("UTF-8"));
            zippedOut.closeEntry();


            zippedOut.finish();
        } catch (Exception e) {
            // Exception handling goes here
            log.error("write zip: {}", e.getMessage(), e);
        }
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
                        public Map<String, Filter> getFilters() {
                            return Map.of("infiniteOptions", new InfiniteOptions());
                        }
                    })
                    .build();
            compiledTemplateMap.put(keyName, engine.getTemplate(keyName));
        }
        return compiledTemplateMap.get(keyName);
    }

    public class InfiniteOptions implements Filter {

        @Override
        public Object apply(Object o, Map<String, Object> map, PebbleTemplate pebbleTemplate, EvaluationContext evaluationContext, int i) throws PebbleException {
            return templateBuilder.buildQueryOptions(String.valueOf(o));
        }

        @Override
        public List<String> getArgumentNames() {
            return null;
        }
    }

    protected Set<String> pageParams() {
        return Sets.newHashSet(springDataWebProperties.getPageable().getPageParameter(), springDataWebProperties.getPageable().getSizeParameter(), springDataWebProperties.getSort().getSortParameter());
    }

    protected void addOperation(Map<String, List<OpenApiControllerMethodExtraction>> map, PathItem.HttpMethod httpMethod, String path, Operation operation) {
        if (operation != null && operation.getExtensions() != null) {
            String controllerBean = (String) operation.getExtensions().get(OpenApiCustomExtractor.CONTROLLER_BEAN);
            map.putIfAbsent(controllerBean, new ArrayList<>());
            map.get(controllerBean).add(new OpenApiControllerMethodExtraction(new OpenApiControllerMethodExtraction.ExtractorConfig(pageParams(), typescriptConverter, httpMethod, path, operation)));
        }
    }
}