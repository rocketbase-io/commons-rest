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

    protected String name;

    protected boolean required;

    protected String type;

    protected boolean inPath;

    protected String description;
}
