package io.rocketbase.commons.openapi;

import io.rocketbase.commons.util.Nulls;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DefaultOpenApiConverter implements OpenApiConverter {

    @Override
    public String getReturnType(String genericReturnType) {
        if (genericReturnType == null) {
            return "any";
        }
        String name = genericReturnType.replace("io.rocketbase.commons.dto.PageableResult", "PageableResult");
        // array check
        Optional<String> arrayType = getListTypes().stream().filter(v -> genericReturnType.startsWith(v)).findFirst();
        if (arrayType.isPresent()) {
            for (String l : getListTypes()) {
                name = name.replace(l, "");
            }
        }
        if (name.contains("<")) {
            String genericCenter = name.substring(name.lastIndexOf("<") + 1).replace(">", "");
            if (name.startsWith("PageableResult")) {
                name = "PageableResult<" + removePackage(genericCenter) + ">";
            } else {
                name = removePackage(genericCenter);
            }
        }
        if (name.equalsIgnoreCase("java.lang.Void")) {
            return "void";
        }
        return removePackage(name) + (arrayType.isPresent() ? "[]" : "");
    }

    @Override
    public String convertType(Schema schema) {
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
        if ("Integer".equalsIgnoreCase(type) || "Long".equalsIgnoreCase(type)) {
            type = "number";
        }
        if ("InputStreamResource".equalsIgnoreCase(type)) {
            type = "any";
        }
        return type;
    }

    @Override
    public Set<String> getImportTypes(Set<String> allTypes) {
        Set<String> result = new HashSet<>();

        for(String t : Nulls.notNull(allTypes)) {
            String type =  t.replace("[]", "");
            if (t.startsWith("PageableResult")) {
                type = t.replace("PageableResult<", "").replace(">", "");
            }
            if (!getNativeTypes().contains(type.toLowerCase())) {
                result.add(type);
            }
        }
        return result;
    }

}
