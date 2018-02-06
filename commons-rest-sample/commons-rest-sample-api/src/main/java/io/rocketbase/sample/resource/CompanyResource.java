package io.rocketbase.sample.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.resource.AbstractCrudRestResource;
import io.rocketbase.sample.dto.data.CompanyData;
import io.rocketbase.sample.dto.edit.CompanyEdit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CompanyResource extends AbstractCrudRestResource<CompanyData, CompanyEdit, String> {

    @Value("${resource.base.api.url}")
    private String baseApiUrl;

    @Autowired
    public CompanyResource(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected String getBaseApiUrl() {
        return baseApiUrl + "/api/company";
    }

    @Override
    protected TypeReference<PageableResult<CompanyData>> createPagedTypeReference() {
        return new TypeReference<PageableResult<CompanyData>>() {
        };
    }
}
