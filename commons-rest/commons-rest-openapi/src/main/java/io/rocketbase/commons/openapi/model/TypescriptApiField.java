package io.rocketbase.commons.openapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypescriptApiField implements Serializable {

    private String name;

    private boolean required;

    private String type;

    private boolean inPath;

    private String description;
}
