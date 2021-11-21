package io.rocketbase.commons.openapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.rocketbase.commons.resource.HookType;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@NoArgsConstructor
public class OpenApiControllerMethodExtraction {
    @JsonIgnore
    private OpenApiController controller;

    private PathItem.HttpMethod httpMethod;
    private String path;
    private String methodName;

    private HookType hookType;
    private List<String> cacheKeys;
    private List<List<String>> invalidateKeys;
    private Integer staleTime;

    private String genericReturnType;
    private List<String> parameterTypes;

    private Operation operation;

    private static final Set<String> PAGEABLE_PARAMS = Sets.newHashSet("page", "pageSize", "size", "sort");
    private static final Set<String> LIST_TYPE = Sets.newHashSet("java.util.List", "java.util.Collection", "java.util.Set");
    private static final Set<String> EXCLUDED_TYPES = Sets.newHashSet("string", "boolean", "long", "void", "any");

    public OpenApiControllerMethodExtraction(PathItem.HttpMethod httpMethod, String path, Operation operation) {
        this.httpMethod = httpMethod;
        this.path = path;
        if (operation.getExtensions() != null) {
            this.methodName = (String) operation.getExtensions().get(OpenApiCustomExtractor.METHOD_NAME);
            this.genericReturnType = (String) operation.getExtensions().get(OpenApiCustomExtractor.GENERIC_RETURN_TYPE);
            this.parameterTypes = (List) operation.getExtensions().get(OpenApiCustomExtractor.PARAMETER_TYPES);

            if (operation.getExtensions().get(OpenApiCustomExtractor.HOOK_TYPE) != null) {
                this.hookType = HookType.valueOf(String.valueOf(operation.getExtensions().get(OpenApiCustomExtractor.HOOK_TYPE)));
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
            Object st = operation.getExtensions().get(OpenApiCustomExtractor.STALE_TIME);
            if (st instanceof Number) {
                this.staleTime = ((Number) st).intValue();
            }
        }
        this.operation = operation;
    }

    public String getShortReturnType() {
        if (genericReturnType == null) {
            return "any";
        }
        String name = genericReturnType.replace("io.rocketbase.commons.dto.PageableResult", "PageableResult") + "";
        // array check
        Optional<String> arrayType = LIST_TYPE.stream().filter(v -> genericReturnType.startsWith(v)).findFirst();
        if (arrayType.isPresent()) {
            for (String l : LIST_TYPE) {
                name = name.replace(l, "");
            }
        }
        if (name.contains("<")) {
            String genericCenter = name.substring(name.lastIndexOf("<") + 1).replace(">", "");
            name = name.startsWith("PageableResult") ? "PageableResult<" + removePackage(genericCenter) + ">" : removePackage(genericCenter);
        }
        if (name.equalsIgnoreCase("java.lang.Void")) {
            return "void";
        }
        return removePackage(name) + (arrayType.isPresent() ? "[]" : "");
    }

    public List<TypescriptApiField> getFields() {
        List<TypescriptApiField> result = new ArrayList<>();
        if (operation.getParameters() != null) {
            for (Parameter p : operation.getParameters()) {
                if (!(getFieldsExtendsPaging() && PAGEABLE_PARAMS.contains(p.getName()))) {
                    result.add(convert(p));
                }
            }
        }
        Schema requestBodySchema = getRequestBodySchema(operation.getRequestBody());
        if (requestBodySchema != null) {
            result.add(new TypescriptApiField("body", true, convertType(requestBodySchema), false, operation.getRequestBody().getDescription()));
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
        return getController().getShortName() + StringUtils.capitalize(methodName);
    }

    public boolean getFieldsExtendsPaging() {
        return parameterTypes.contains("org.springframework.data.domain.Pageable");
    }

    public String getDescription() {
        return operation.getDescription();
    }

    public String getPathJs() {
        return getPath().replace("{", "${");
    }

    public boolean isValidForHook() {
        if (hookType == null) {
            return false;
        }
        switch (hookType) {
            case INFINITE:
                return cacheKeys != null;
            case QUERY:
                return cacheKeys != null;
            default:
                return invalidateKeys != null;
        }
    }

    public List<String> getPathFields() {
        List<String> result = new ArrayList<>();
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
        if (operation.getParameters() != null) {
            for (Parameter p : operation.getParameters()) {
                if (p.getIn().equalsIgnoreCase("query")) {
                    result.add(p.getName());
                }
            }
        }
        return result.isEmpty() ? null : result;
    }

    public boolean hasBody() {
        return operation.getRequestBody() != null;
    }

    public String getHookName() {
        if (hookType == null) {
            return null;
        }
        return "use" + StringUtils.capitalize(hookType.name().toLowerCase()) + getShortInputType();
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
        return invalidateKeys.stream().map(e -> e.stream().map(v ->
                        v.startsWith("${") ? v.replace("${", "${" + inputPrefix) : (
                                v.startsWith("@{") ? v.replace("@{", "${" + responsePrefix) : v
                        ))
                .collect(Collectors.toList())).collect(Collectors.toList());
    }

    protected String removePackage(String value) {
        return value.substring(value.lastIndexOf(".") + 1);
    }

    protected String removeRefPath(String value) {
        return value.substring(value.lastIndexOf("/") + 1);
    }

    public Set<String> getImportTypes() {
        Set<String> result = new HashSet<>();
        String returnType = getShortReturnType();
        String type = returnType.replace("[]", "");
        if (returnType.startsWith("PageableResult")) {
            type = returnType.replace("PageableResult<", "").replace(">", "");

        }
        if (!EXCLUDED_TYPES.contains(type.toLowerCase())) {
            result.add(type);
        }
        if (operation.getParameters() != null) {
            for (Parameter p : operation.getParameters()) {
                if (!(getFieldsExtendsPaging() && PAGEABLE_PARAMS.contains(p.getName()))) {
                    if (p.getSchema().get$ref() != null) {
                        result.add(removeRefPath(p.getSchema().get$ref()));
                    }
                }
            }
            if (operation.getRequestBody() != null) {
                String requestType = convertType(getRequestBodySchema(operation.getRequestBody())).replace("[]", "");
                if (!EXCLUDED_TYPES.contains(requestType.toLowerCase())) {
                    result.add(requestType);
                }
            }
        }
        return result;
    }

    protected Schema getRequestBodySchema(RequestBody body) {
        if (body != null) {
            Content content = body.getContent();
            if (content.keySet().contains("application/json")) {
                Schema schema = content.get("application/json").getSchema();
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
                .type(convertType(p.getSchema()))
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

    private String convertType(Schema schema) {
        if (schema == null) {
            return "any";
        }
        String type = schema.get$ref() != null ? removeRefPath(schema.get$ref()) : schema.getType();
        if (schema instanceof ArraySchema) {
            Schema<?> item = ((ArraySchema) schema).getItems();
            type = item.getType() != null ? item.getType() : removeRefPath(item.get$ref()) + "[]";
        }
        if ("Void".equalsIgnoreCase(type)) {
            type = "void";
        }
        return type;
    }


}
