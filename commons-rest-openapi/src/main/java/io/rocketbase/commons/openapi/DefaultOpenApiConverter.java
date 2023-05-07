package io.rocketbase.commons.openapi;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.rocketbase.commons.util.Nulls;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.rocketbase.commons.openapi.OpenApiControllerMethodExtraction.MULTIPART_TYPESCRIPT;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultOpenApiConverter implements OpenApiConverter {

    @Override
    public String getReturnType(String genericReturnType) {
        if (genericReturnType == null) {
            return "unknown";
        }
        if (genericReturnType.equalsIgnoreCase("java.lang.void")) {
            return "void";
        }
        if (genericReturnType.equalsIgnoreCase("java.lang.object")) {
            return "any";
        }
        String name = convertInfiniteReturnTypes(genericReturnType);
        // array check
        Optional<String> arrayType = getListTypes().stream().filter(v -> genericReturnType.startsWith(v)).findFirst();
        if (arrayType.isPresent()) {
            for (String l : getListTypes()) {
                if (name.startsWith(l)) {
                    name = name.replace(l + "<", "").replaceAll("[\\>]$", "");
                    break;
                }
            }
        }
        if (name != null && name.contains("<")) {
            String genericCenter = name.substring(name.lastIndexOf("<") + 1).replace(">", "");
            if (genericCenter.equals("?")) {
                name = name.replace(genericCenter, "any");
            } else {

                name = name.replace(genericCenter, removePackage(checkAndAddUnionType(genericCenter)));
            }
        }
        return convertType(removePackage(checkAndAddUnionType(name))) + (arrayType.isPresent() ? "[]" : "");
    }

    /**
     * checks if given className is an unionType typescript related...
     */
    protected String checkAndAddUnionType(String name) {
        if (name != null && !name.contains("<")) {
            try {
                Class clazz = Class.forName(name);
                if ((clazz.isAnnotationPresent(JsonSubTypes.class) && clazz.isInterface() && clazz.isAnnotationPresent(JsonTypeInfo.class))) {
                    return name + "Union";
                }
            } catch (ClassNotFoundException cnf) {
            }
        }
        return name;
    }

    protected String convertInfiniteReturnTypes(String genericReturnType) {
        return genericReturnType.replace("io.rocketbase.commons.dto.PageableResult", "PageableResult");
    }

    @Override
    public String convertType(Schema schema) {
        if (schema == null) {
            return "unknown";
        }
        String type = schema.get$ref() != null ? removeRefPath(schema.get$ref()) : schema.getType();
        if (schema instanceof ArraySchema) {
            Schema<?> item = ((ArraySchema) schema).getItems();
            type = (item.getType() != null ? item.getType() : removeRefPath(item.get$ref())) + "[]";
        }
        type = convertType(type);
        return type;
    }

    protected String convertType(String type) {
        if (type == null) {
            return null;
        }
        if (getNativeTypes().contains(type.toLowerCase())) {
            type = type.toLowerCase();
        }
        if ("Void".equalsIgnoreCase(type)) {
            type = "void";
        }
        if ("Integer".equalsIgnoreCase(type) || "Long".equalsIgnoreCase(type) || "Double".equalsIgnoreCase(type) || "Float".equalsIgnoreCase(type) || "BigDecimal".equalsIgnoreCase(type)) {
            type = "number";
        }
        for (String java : getJavaToUnknowns()) {
            if (java.equalsIgnoreCase(type)) {
                return "unknown";
            }
        }
        return type;
    }

    @Override
    public Set<String> getImportTypes(Set<String> allTypes) {
        Set<String> result = new HashSet<>();
        for (String t : Nulls.notNull(allTypes)) {
            if (t != null) {
                String type = convertImportWrappers(t.replace("[]", ""), result);

                result.add(removePackage(type).replaceAll("<.*>", ""));
                // add possibly union types
                result.add(removePackage(checkAndAddUnionType(type)).replaceAll("<.*>", ""));
            }
        }
        return result.stream()
                .map(v -> convertType(v))
                .filter(Objects::nonNull)
                .filter(v -> !getNativeTypes().contains(v.toLowerCase()))
                .filter(v -> !MULTIPART_TYPESCRIPT.equalsIgnoreCase(v))
                .collect(Collectors.toSet());
    }

    protected String convertImportWrappers(String type, Set<String> importTypes) {
        for (String t : getListTypes()) {
            if (type.startsWith(t + "<")) {
                return type.replace(t + "<", "").replace(">", "");
            }
        }
        if (type.startsWith("io.rocketbase.commons.dto.PageableResult<")) {
            return type.replace("io.rocketbase.commons.dto.PageableResult<", "").replace(">", "");
        }
        return type;
    }

}
