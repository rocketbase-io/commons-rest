package io.rocketbase.sample.dto.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class CompanyData implements Serializable {

    private String id;

    private String name;

    private String email;

    private String url;
}
