package io.rocketbase.commons.openapi.sample.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Test fixture for generator edge cases:
 * <ul>
 *   <li>{@code path} — a {@code @Pattern} containing a literal {@code /} that must be escaped
 *       so the generated JS regex literal stays valid.</li>
 *   <li>{@code preciseMin} — a {@code @DecimalMin} whose value would lose precision through
 *       {@code Double.parseDouble}; must be emitted verbatim.</li>
 *   <li>{@code attributes} — a {@code Map<String, AddressDto>} whose value type must resolve
 *       to the nested {@code AddressDtoSchema} inside {@code z.record}.</li>
 * </ul>
 */
public class EdgeCaseCmd {

    @Pattern(regexp = "^/api/[a-z]+$")
    private String path;

    @DecimalMin(value = "0.10000000000000001", inclusive = true)
    private BigDecimal preciseMin;

    private Map<String, AddressDto> attributes;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public BigDecimal getPreciseMin() {
        return preciseMin;
    }

    public void setPreciseMin(BigDecimal preciseMin) {
        this.preciseMin = preciseMin;
    }

    public Map<String, AddressDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, AddressDto> attributes) {
        this.attributes = attributes;
    }
}
