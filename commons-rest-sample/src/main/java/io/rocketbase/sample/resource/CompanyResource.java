package io.rocketbase.sample.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.resource.AbstractCrudRestResource;
import io.rocketbase.sample.dto.data.CompanyData;
import io.rocketbase.sample.dto.edit.CompanyEdit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CompanyResource extends AbstractCrudRestResource<CompanyData, CompanyEdit, String> {

    @Autowired
    public CompanyResource(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    @Override
    protected String getBaseApiUrl() {
        return "http://localhost:8080/api/company";
    }

    @Override
    protected TypeReference<PageableResult<CompanyData>> createPagedTypeReference() {
        return new TypeReference<PageableResult<CompanyData>>() {
        };
    }
}
