package io.rocketbase.commons.openapi.sample.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {

    @NotBlank
    private String street;

    @NotBlank
    @Size(max = 10)
    private String zipCode;

    @NotBlank
    private String city;

    private String country;
}
