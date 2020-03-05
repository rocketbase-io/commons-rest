package io.rocketbase.commons.dto.address;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.rocketbase.commons.util.Nulls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDto implements Serializable {

    @Nullable
    private Gender gender;

    @Size(max = 10)
    @Nullable
    private String title;

    @Size(max = 100)
    @Nullable
    private String firstName;

    @Size(max = 100)
    @Nullable
    private String lastName;

    @Size(max = 100)
    @Email
    @Nullable
    private String email;

    @Size(max = 50)
    @Nullable
    private String landline;

    @Size(max = 50)
    @Nullable
    private String cellphone;

    @Transient
    @JsonIgnore
    public String getDisplayName() {
        String result = (Nulls.notNull(firstName) + " " + Nulls.notNull(lastName)).trim();
        if (StringUtils.isEmpty(result) && !StringUtils.isEmpty(email)) {
            result = email;
        }
        return result;
    }

}
