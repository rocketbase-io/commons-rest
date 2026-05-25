package io.rocketbase.commons.openapi.sample.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Test fixture exercising a self-referential (recursive) nested structure: a tree node
 * whose children are again {@link TreeNodeCmd}. The Zod generator must break the cycle
 * with {@code z.lazy(() => TreeNodeCmdSchema)} on the {@code children} field, otherwise the
 * generated {@code const} would reference itself before initialisation.
 */
public class TreeNodeCmd {

    @NotBlank
    private String label;

    private List<TreeNodeCmd> children;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<TreeNodeCmd> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNodeCmd> children) {
        this.children = children;
    }
}
