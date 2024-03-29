package io.rocketbase.commons.openapi.model;

import io.rocketbase.commons.openapi.OpenApiControllerMethodExtraction;
import io.rocketbase.commons.openapi.OpenApiConverter;
import io.rocketbase.commons.util.Nulls;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class OpenApiController implements Serializable {

    private String controllerBean;
    private List<OpenApiControllerMethodExtraction> methods;

    private OpenApiConverter openApiConverter;

    public String getShortName() {
        return controllerBean.substring(controllerBean.lastIndexOf(".") + 1).replace("Controller", "");
    }

    public OpenApiController(String controllerBean, List<OpenApiControllerMethodExtraction> methods, OpenApiConverter openApiConverter) {
        this.controllerBean = controllerBean;
        this.methods = methods;
        // link this controller to methods
        for (OpenApiControllerMethodExtraction m : methods) {
            m.setController(this);
        }
        this.openApiConverter = openApiConverter;
    }

    public Collection<ImportGroup> getImportTypes() {
        Map<String, ImportGroup> packMap = new TreeMap<>();
        for (OpenApiControllerMethodExtraction m : Nulls.notNull(methods)) {
            Nulls.notNull(m, OpenApiControllerMethodExtraction::getImportTypes, new ArrayList<ImportGroup>())
                    .stream()
                    .forEach(i -> {
                        if (packMap.containsKey(i.getName())) {
                            packMap.get(i.getName()).getTypes().addAll(i.getTypes());
                        } else {
                            packMap.put(i.getName(), i);
                        }
                    });
        }
        return packMap.values();
    }

    protected String toKebabCase(String input) {
        return input
                .replaceAll("([a-z0-9])([A-Z])", "$1-$2")
                .replaceAll("([A-Z])([A-Z])(?=[a-z])", "$1-$2")
                .toLowerCase();
    }

    public Set<String> getFieldImports() {
        return methods.stream().filter(m -> m.hasOptionalFields() || m.hasRequiredFields())
                .map(OpenApiControllerMethodExtraction::getShortInputType)
                .filter(v -> !Nulls.notNull(openApiConverter, OpenApiConverter::getNativeTypes, Collections.emptySet())
                        .contains(v.toLowerCase()))
                .collect(Collectors.toSet());
    }

    public String getFilename() {
        return toKebabCase(getShortName());
    }

    public String getFieldName() {
        return StringUtils.uncapitalize(getShortName());
    }
}
