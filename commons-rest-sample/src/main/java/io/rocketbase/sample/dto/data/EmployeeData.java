package io.rocketbase.sample.dto.data;

import io.codearte.jfairy.producer.person.Person;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class EmployeeData implements Serializable {

    private String id;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private Person.Sex sex;

    private String email;

    private CompanyData company;

}
