package io.rocketbase.sample.dto.edit;

import io.codearte.jfairy.producer.person.Person;
import lombok.Data;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class EmployeeEdit implements Serializable {

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    private LocalDate dateOfBirth;

    @NotNull
    private Person.Sex sex;

    @NotNull
    @Email
    private String email;
}
