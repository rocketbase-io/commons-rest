package io.rocketbase.commons.dto.address;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.rocketbase.commons.model.HasFirstAndLastName;
import io.rocketbase.commons.util.Nulls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import javax.annotation.Nullable;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @JsonIgnore
    public String getDisplayName() {
        return Nulls.notEmpty(getFullName(), email);
    }

}
