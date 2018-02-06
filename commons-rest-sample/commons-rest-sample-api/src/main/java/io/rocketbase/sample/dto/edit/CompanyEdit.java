package io.rocketbase.sample.dto.edit;

import lombok.Data;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class CompanyEdit implements Serializable {

    private String name;

    @NotNull
    @Email
    private String email;

    private String url;
}
