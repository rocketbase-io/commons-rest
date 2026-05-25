package io.rocketbase.commons.openapi.sample.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Test fixture: a command referencing a type-level {@code @ZodSchema(IGNORE)} value object.
 */
public class IgnoredValueHolderCmd {

    @NotBlank
    private String title;

    private IgnoredValue payload;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public IgnoredValue getPayload() {
        return payload;
    }

    public void setPayload(IgnoredValue payload) {
        this.payload = payload;
    }
}
