package io.rocketbase.sample.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWrite implements Serializable {

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    private LocalDate dateOfBirth;

    @NotNull
    private boolean female;

    @NotNull
    @Email
    private String email;
}
