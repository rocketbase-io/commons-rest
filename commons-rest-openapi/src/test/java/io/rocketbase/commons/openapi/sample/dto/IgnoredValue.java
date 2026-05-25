package io.rocketbase.commons.openapi.sample.dto;

import io.rocketbase.commons.generator.ZodSchema;
import jakarta.validation.constraints.NotBlank;

/**
 * Test fixture: a complex value object marked {@code @ZodSchema(IGNORE)} at type level.
 * A command field of this type must degrade to {@code z.any()} rather than getting its own
 * schema or an unvalidated {@code z.custom<Types.IgnoredValue>()}.
 */
@ZodSchema(ZodSchema.Mode.IGNORE)
public class IgnoredValue {

    @NotBlank
    private String opaque;

    public String getOpaque() {
        return opaque;
    }

    public void setOpaque(String opaque) {
        this.opaque = opaque;
    }
}
