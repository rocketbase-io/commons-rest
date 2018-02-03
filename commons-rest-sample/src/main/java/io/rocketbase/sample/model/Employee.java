package io.rocketbase.sample.model;

import io.codearte.jfairy.producer.person.Person.Sex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Employee implements Serializable {

    @Id
    private String id;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private Sex sex;

    private String email;

    @DBRef(lazy = true)
    private Company company;

}
