package io.rocketbase.commons.openapi.model;

import io.rocketbase.commons.openapi.OpenApiControllerMethodExtraction;
import io.rocketbase.commons.util.Nulls;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class OpenApiController implements Serializable {

    private String controllerBean;
    private List<OpenApiControllerMethodExtraction> methods;

    public String getShortName() {
        return controllerBean.substring(controllerBean.lastIndexOf(".") + 1).replace("Controller", "");
    }

    public OpenApiController(String controllerBean, List<OpenApiControllerMethodExtraction> methods) {
        this.controllerBean = controllerBean;
        this.methods = methods;
        // link this controller to methods
        for (OpenApiControllerMethodExtraction m : methods) {
            m.setController(this);
        }
    }

    public Set<String> getImportTypes() {
        Set<String> result = new HashSet<>();
        for (OpenApiControllerMethodExtraction m : Nulls.notNull(methods)) {
            if (m != null && m.getImportTypes() != null) {
                result.addAll(m.getImportTypes());
            }
        }
        return result;
    }

    protected String toKebabCase(String input) {
        return input
                .replaceAll("([a-z0-9])([A-Z])", "$1-$2")
                .replaceAll("([A-Z])([A-Z])(?=[a-z])", "$1-$2")
                .toLowerCase();
    }

    public Set<String> getFieldImports() {
        return methods.stream().filter(m -> m.hasOptionalFields() || m.hasRequiredFields()).map(OpenApiControllerMethodExtraction::getShortInputType).collect(Collectors.toSet());
    }

    public String getFilename() {
        return toKebabCase(getShortName());
    }

    public String getFieldName() {
        return StringUtils.uncapitalize(getShortName());
    }
}
