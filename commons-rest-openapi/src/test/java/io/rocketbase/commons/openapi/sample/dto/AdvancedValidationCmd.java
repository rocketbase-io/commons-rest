package io.rocketbase.commons.openapi.sample.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Test command with advanced validation annotations.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdvancedValidationCmd {

    @NotBlank
    private String name;

    @DecimalMin(value = "0.01", inclusive = true)
    @DecimalMax(value = "999.99", inclusive = true)
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false)
    @DecimalMax(value = "1.0", inclusive = false)
    private Double percentage;

    @Digits(integer = 5, fraction = 2)
    private BigDecimal amount;

    @Digits(integer = 10, fraction = 0)
    private Long accountNumber;

    @Positive
    private Integer quantity;

    @PositiveOrZero
    private Integer stockLevel;

    @Negative
    private Integer debt;

    @NegativeOrZero
    private Integer balance;
}
