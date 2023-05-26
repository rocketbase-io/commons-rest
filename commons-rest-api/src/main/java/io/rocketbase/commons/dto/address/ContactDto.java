package io.rocketbase.commons.dto.address;

import io.rocketbase.commons.model.HasFirstAndLastName;
import io.rocketbase.commons.util.Nulls;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.lang.Nullable;

import java.io.Serializable;

@Data
@NoArgsConstructor
@SuperBuilder
public class ContactDto implements Serializable, HasFirstAndLastName {

    @Nullable
    private Gender gender;

    @Size(max = 10)
    @Nullable
    private String salutation;

    @Size(max = 10)
    @Nullable
    private String title;

    @Size(max = 100)
    @Nullable
    private String firstName;

    @Size(max = 100)
    @Nullable
    private String lastName;

    @Size(max = 255)
    @Email
    @Nullable
    private String email;

    @Size(max = 50)
    @Nullable
    private String landline;

    @Size(max = 50)
    @Nullable
    private String cellphone;

    /**
     * similar to getFullName but has email as fallback
     */
    @Transient
    public String getDisplayName() {
        return Nulls.notEmpty(getFullName(), email);
    }

}
