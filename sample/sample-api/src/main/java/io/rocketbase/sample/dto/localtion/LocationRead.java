package io.rocketbase.sample.dto.localtion;

import io.hypersistence.tsid.TSID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRead implements Serializable {


    private TSID id;
    private String name;
    private String city;
    private String street;
    private String country;
}
