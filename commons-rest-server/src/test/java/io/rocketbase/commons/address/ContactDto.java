package io.rocketbase.commons.address;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

@Data
@NoArgsConstructor
@SuperBuilder
public class ContactDto implements Serializable {

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

}
