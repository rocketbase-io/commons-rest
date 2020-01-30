package io.rocketbase.commons.dto.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto implements Serializable {

    @Size(max = 40)
    private String addressLineOne;

    @Size(max = 40)
    private String addressLineTwo;

    @Size(max = 50)
    private String city;

    @Size(max = 50)
    private String state;

    @Size(max = 10)
    private String postalCode;

    @Size(max = 2)
    private String countryCode;

}
