package io.rocketbase.sample.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;

@Document(collection = "employee")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeEntity implements Serializable {

    @Id
    private String id;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private boolean female;

    private String email;

    @DBRef(lazy = true)
    private CompanyEntity company;
}
