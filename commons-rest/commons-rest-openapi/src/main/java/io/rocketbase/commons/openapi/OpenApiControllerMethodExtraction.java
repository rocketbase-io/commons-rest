package io.rocketbase.commons.openapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.rocketbase.commons.openapi.model.OpenApiController;
import io.rocketbase.commons.openapi.model.TypescriptApiField;
import io.rocketbase.commons.util.Nulls;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
public class OpenApiControllerMethodExtraction {

    @JsonIgnore
    protected OpenApiController controller;

    protected String hookType;
    protected String methodName;
    protected Integer staleTime;
    protected String requestBodyType;

    protected List<String> cacheKeys;
    protected List<List<String>> invalidateKeys;

    protected String genericReturnType;
    protected List<String> parameterTypes;

    protected final ExtractorConfig config;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ExtractorConfig {
        private Set<String> pageableParams;
        private OpenApiConverter typescriptConverter;
        private PathItem.HttpMethod httpMethod;
        private String path;
        private Operation operation;
    }

    public String getHookType() {
        return hookType;
    }

    public boolean isValid() {
        return hookType != null && methodName != null;
    }

    public OpenApiControllerMethodExtraction(ExtractorConfig config) {
        this.config = config;

        Operation operation = config.getOperation();
        if (operation.getExtensions() != null) {
            this.methodName = (String) operation.getExtensions().get(OpenApiCustomExtractor.METHOD_NAME);
            this.genericReturnType = (String) operation.getExtensions().get(OpenApiCustomExtractor.GENERIC_RETURN_TYPE);
            this.parameterTypes = (List) operation.getExtensions().get(OpenApiCustomExtractor.PARAMETER_TYPES);
            this.hookType = String.valueOf(operation.getExtensions().get(OpenApiCustomExtractor.HOOK_TYPE));
            if (operation.getExtensions().containsKey(OpenApiCustomExtractor.REQUEST_BODY_TYPE_NAME)) {
                this.requestBodyType = String.valueOf(operation.getExtensions().get(OpenApiCustomExtractor.REQUEST_BODY_TYPE_NAME));
            }
            Object cK = operation.getExtensions().get(OpenApiCustomExtractor.CACHE_KEYS);
            if (cK instanceof String && !((String) cK).isEmpty()) {
                cacheKeys = splitString((String) cK);
            }
            Object invK = operation.getExtensions().get(OpenApiCustomExtractor.INVALIDATE_KEYS);
            if (invK instanceof Collection && !((Collection) invK).isEmpty()) {
                List<List<String>> values = new ArrayList<>();
                for (Object v : (Collection) invK) {
                    if (v instanceof String && !((String) v).isEmpty()) {
                        values.add(splitString((String) v));
                    }
                }
                invalidateKeys = values;
            }
            Object st = operation.getExtensions().getOrDefault(OpenApiCustomExtractor.STALE_TIME, null);
            if (st instanceof Number) {
                this.staleTime = ((Number) st).intValue();
            }
        }
    }


    public List<TypescriptApiField> getFields() {
        List<TypescriptApiField> result = new ArrayList<>();
        Operation operation = config.getOperation();
        if (operation.getParameters() != null) {
            for (Parameter p : operation.getParameters()) {
                if (!(config.getTypescriptConverter().hasPageableParameter(parameterTypes)
                        && config.getPageableParams().contains(p.getName()))) {
                    result.add(convert(p));
                }
            }
        }
        Schema requestBodySchema = getRequestBodySchema(operation.getRequestBody());
        if (requestBodySchema != null) {
            String type = config.getTypescriptConverter().convertType(requestBodySchema);
            if (type == null && requestBodyType != null) {
                type = config.getTypescriptConverter().getReturnType(requestBodyType);
            }
            result.add(new TypescriptApiField("body", true, type,
                    false, operation.getRequestBody().getDescription()));
        }
        return result.isEmpty() ? null : result;
    }

    public boolean hasRequiredFields() {
        List<TypescriptApiField> fields = getFields();
        if (fields == null || fields.isEmpty()) {
            return false;
        }
        return fields.stream().filter(v -> v.isRequired()).findFirst().isPresent();
    }

    public boolean hasOptionalFields() {
        List<TypescriptApiField> fields = getFields();
        if (fields == null || fields.isEmpty()) {
            return false;
        }
        return getFields().stream().filter(v -> !v.isRequired()).findFirst().isPresent();
    }

    public String getShortInputType() {
        if (isVoidShortInputType()) {
            return "void";
        }
        return getController().getShortName() + StringUtils.capitalize(methodName);
    }

    public boolean isVoidShortInputType() {
        return !hasRequiredFields() && !hasOptionalFields() && !hasBody();
    }

    public String getDescription() {
        return config.getOperation().getDescription();
    }

    public String getPathJs() {
        return config.getPath().replace("{", "${");
    }

    public List<String> getPathFields() {
        List<String> result = new ArrayList<>();
        Operation operation = config.getOperation();
        if (operation.getParameters() != null) {
            for (Parameter p : operation.getParameters()) {
                if (p.getIn().equalsIgnoreCase("path")) {
                    result.add(p.getName());
                }
            }
        }
        return result.isEmpty() ? null : result;
    }

    public List<String> getQueryFields() {
        List<String> result = new ArrayList<>();
        Operation operation = config.getOperation();
        if (operation.getParameters() != null) {
            for (Parameter p : operation.getParameters()) {
                if (p.getIn().equalsIgnoreCase("query")) {
                    result.add(p.getName());
                }
            }
        }
        return result.isEmpty() ? null : result;
    }

    public boolean getFieldsExtendsPaging() {
        return parameterTypes.contains("org.springframework.data.domain.Pageable");
    }

    public boolean hasBody() {
        return config.getOperation().getRequestBody() != null;
    }

    public String getHookName() {
        if (hookType == null) {
            return null;
        }
        String result = "use" + StringUtils.capitalize(hookType);
        if (getShortInputType().equalsIgnoreCase("void")) {
            result += StringUtils.capitalize(getController().getShortName()) + StringUtils.capitalize(getMethodName());
        } else {
            result += StringUtils.capitalize(getShortInputType());
        }
        return result;
    }

    public List<String> getCacheKeysPrepared(String prefix) {
        if (cacheKeys == null) {
            return null;
        }
        return cacheKeys.stream().map(v -> v.startsWith("${") ? v.replace("${", "${" + prefix) : v).collect(Collectors.toList());
    }

    public List<List<String>> getInvalidateKeysPrepared(String inputPrefix, String responsePrefix) {
        if (invalidateKeys == null) {
            return null;
        }
        return invalidateKeys.stream()
                .filter(Objects::nonNull)
                .map(e -> e.stream()
                        .filter(Objects::nonNull)
                        .map(v ->
                                v.startsWith("${") ? v.replace("${", "${" + inputPrefix) : (
                                        v.startsWith("@{") ? v.replace("@{", "${" + responsePrefix) : v
                                ))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public Set<String> getImportTypes() {
        Set<String> types = new HashSet<>();
        if (genericReturnType != null) {
            types.add(genericReturnType);
        }
        types.addAll(Nulls.notNull(getFields()).stream()
                .filter(Objects::nonNull)
                .map(TypescriptApiField::getType)
                .collect(Collectors.toSet()));
        return config.getTypescriptConverter().getImportTypes(types);
    }

    public String getShortReturnType() {
        return config.getTypescriptConverter().getReturnType(genericReturnType);
    }

    protected Schema getRequestBodySchema(RequestBody body) {
        if (body != null) {
            Content content = body.getContent();
            if (content.keySet().contains("application/json")) {
                Schema schema = content.get("application/json").getSchema();
                return schema;
            } else if (content.keySet().contains("*/*")) {
                Schema schema = content.get("*/*").getSchema();
                return schema;
            } else {
                log.error("invalid input-type");
            }
        }
        return null;
    }

    protected TypescriptApiField convert(Parameter p) {
        return TypescriptApiField.builder()
                .name(p.getName())
                .required(p.getRequired() != null && p.getRequired())
                .type(config.getTypescriptConverter().convertType(p.getSchema()))
                .inPath(p.getIn().equalsIgnoreCase("path"))
                .description(p.getDescription())
                .build();
    }

    protected List<String> splitString(Object value) {
        if (value instanceof String) {
            return Lists.newArrayList(Splitter.on(",").trimResults().split((String) value));
        }
        return null;
    }


}