package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.resource.AbstractCrudRestResource;
import io.rocketbase.sample.dto.company.CompanyRead;
import io.rocketbase.sample.dto.company.CompanyWrite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class CompanyResource extends AbstractCrudRestResource<CompanyRead, CompanyWrite, String> {

    @Value("${resource.base.api.url}")
    private String baseApiUrl;

    @Override
    protected String getBaseApiUrl() {
        return baseApiUrl + "/api/company";
    }

    @Override
    protected ParameterizedTypeReference<PageableResult<CompanyRead>> createPagedTypeReference() {
        return new ParameterizedTypeReference<PageableResult<CompanyRead>>() {
        };
    }
}
