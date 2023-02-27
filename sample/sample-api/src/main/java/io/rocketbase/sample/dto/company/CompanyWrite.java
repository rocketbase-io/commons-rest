package io.rocketbase.sample.dto.company;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyWrite implements Serializable {

    private String name;

    @NotNull
    @Email
    private String email;

    private String url;
}
