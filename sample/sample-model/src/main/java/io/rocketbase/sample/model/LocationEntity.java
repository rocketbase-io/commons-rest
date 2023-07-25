package io.rocketbase.sample.model;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationEntity implements Serializable {

    @Id
    @Tsid
    private Long id;

    private String name;
    private String city;
    private String street;
    private String country;
}
