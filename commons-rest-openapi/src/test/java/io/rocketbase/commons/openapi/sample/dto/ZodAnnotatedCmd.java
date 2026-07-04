package io.rocketbase.commons.openapi.sample.dto;

import tools.jackson.databind.JsonNode;
import io.rocketbase.commons.generator.ZodSchema;
import jakarta.validation.constraints.NotBlank;

/**
 * Test fixture exercising field-level {@link ZodSchema} modes:
 * <ul>
 *   <li>{@code rawPayload} → {@code @ZodSchema(ANY)} emits {@code z.any()}</li>
 *   <li>{@code internalNote} → {@code @ZodSchema(IGNORE)} is dropped from the schema</li>
 *   <li>{@code name} → ordinary validated field, proves the rest is untouched</li>
 * </ul>
 */
public class ZodAnnotatedCmd {

    @NotBlank
    private String name;

    @ZodSchema(ZodSchema.Mode.ANY)
    private JsonNode rawPayload;

    @ZodSchema(ZodSchema.Mode.IGNORE)
    private String internalNote;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonNode getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(JsonNode rawPayload) {
        this.rawPayload = rawPayload;
    }

    public String getInternalNote() {
        return internalNote;
    }

    public void setInternalNote(String internalNote) {
        this.internalNote = internalNote;
    }
}
