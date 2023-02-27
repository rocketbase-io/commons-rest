package io.rocketbase.commons.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * simple address object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "simple address object")
public class AddressDto implements Serializable {

    /**
     * first address line
     */
    @Size(max = 40)
    @Nullable
    @Schema(description = "first address line", example = "Katharinenstraße 30a")
    private String addressLineOne;

    /**
     * second address line (optional)
     */
    @Size(max = 40)
    @Nullable
    @Schema(description = "second address line (optional)", example = "rocketbase.io software productions GmbH")
    private String addressLineTwo;

    @Size(max = 50)
    @Nullable
    @Schema(example = "Hamburg")
    private String city;

    @Size(max = 50)
    @Nullable
    @Schema(example = "Hamburg")
    private String state;

    @Size(max = 10)
    @Nullable
    @Schema(example = "20457")
    private String postalCode;

    @Size(max = 2)
    @Nullable
    @Schema(description = "iso country code 2 letters - ISO 3166-1 alpha-2", example = "de")
    private String countryCode;

}
