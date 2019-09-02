package io.rocketbase.sample.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document(collection = "company")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyEntity implements Serializable {

    @Id
    private String id;

    private String name;

    private String email;

    private String url;
}
