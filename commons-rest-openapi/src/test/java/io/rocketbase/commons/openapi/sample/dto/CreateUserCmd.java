package io.rocketbase.commons.openapi.sample.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Complex command with various validation annotations for testing.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserCmd {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotNull
    @Email
    private String email;

    @Size(min = 8, max = 100)
    private String password;

    @Min(18)
    @Max(120)
    private Integer age;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    private String phoneNumber;

    @NotNull
    private UserRole role;

    private LocalDate birthDate;

    @Size(min = 1, max = 10)
    private List<String> tags;

    private Map<String, String> metadata;

    @Positive
    private Long departmentId;

    private Boolean active;

    @NotEmpty
    private List<AddressDto> addresses;
}
