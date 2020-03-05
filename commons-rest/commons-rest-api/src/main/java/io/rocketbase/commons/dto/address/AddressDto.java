package io.rocketbase.commons.dto.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto implements Serializable {

    @Size(max = 40)
    @Nullable
    private String addressLineOne;

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
