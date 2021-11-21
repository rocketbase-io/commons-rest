package io.rocketbase.commons.openapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypescriptApiField {

    private String name;
    private boolean required;
    private String type;
    private boolean inPath;
    private String description;
}
