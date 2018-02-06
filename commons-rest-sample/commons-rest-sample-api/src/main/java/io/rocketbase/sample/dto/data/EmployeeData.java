package io.rocketbase.sample.dto.data;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class EmployeeData implements Serializable {

    private String id;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private boolean female;

    private String email;

    private CompanyData company;

}
