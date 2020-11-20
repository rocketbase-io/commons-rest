package io.rocketbase.commons.dto.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * simple address object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto implements Serializable {

    /**
     * first address line
     */
    @Size(max = 40)
    @Nullable
    private String addressLineOne;

    /**
     * second address line (optional)
     */
    @Size(max = 40)
    @Nullable
    private String addressLineTwo;

    @Size(max = 50)
    @Nullable
    private String city;

    @Size(max = 50)
    @Nullable
    private String state;

    @Size(max = 10)
    @Nullable
    private String postalCode;

    @Size(max = 2)
    @Nullable
    private String countryCode;

}
