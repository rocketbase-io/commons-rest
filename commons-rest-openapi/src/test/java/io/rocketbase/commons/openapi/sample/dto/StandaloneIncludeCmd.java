package io.rocketbase.commons.openapi.sample.dto;

import io.rocketbase.commons.generator.ZodSchema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Test fixture: a command marked {@code @ZodSchema(INCLUDE)} that no mutation endpoint
 * references. It must still get a generated schema because it is reachable via the
 * TypeScript generator's type-mapping (i.e. it appears somewhere in the API surface).
 */
@ZodSchema
public class StandaloneIncludeCmd {

    @NotBlank
    private String sku;

    @Positive
    private int quantity;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
