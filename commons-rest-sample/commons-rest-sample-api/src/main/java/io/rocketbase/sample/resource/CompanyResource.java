package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.sample.dto.company.CompanyRead;
import io.rocketbase.sample.dto.company.CompanyWrite;
import org.springframework.util.Assert;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

public class CompanyResource extends AbstractCrudRestResource<CompanyRead, CompanyWrite, String> {

    protected String baseUrl;

    public CompanyResource(String baseUrl) {
        this(baseUrl, null);
    }

    public CompanyResource(String baseUrl, RestTemplate restTemplate) {
        Assert.hasText(baseUrl, "baseUrl is required");
        this.baseUrl = baseUrl;
        setRestTemplate(restTemplate);
    }

    @Override
    protected String getBaseApiUrl() {
        return baseUrl + "/api/company";
    }

    @Override
    protected ParameterizedTypeReference<PageableResult<CompanyRead>> createPagedTypeReference() {
        return new ParameterizedTypeReference<PageableResult<CompanyRead>>() {
        };
    }
}
