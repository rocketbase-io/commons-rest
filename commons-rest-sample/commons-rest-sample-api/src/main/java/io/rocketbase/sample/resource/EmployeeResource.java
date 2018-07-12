package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.resource.AbstractCrudChildRestResource;
import io.rocketbase.sample.dto.employee.EmployeeRead;
import io.rocketbase.sample.dto.employee.EmployeeWrite;
import org.springframework.util.Assert;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;


public class EmployeeResource extends AbstractCrudChildRestResource<EmployeeRead, EmployeeWrite, String> {

    protected String baseUrl;

    public EmployeeResource(String baseUrl) {
        this(baseUrl, null);
    }

    public EmployeeResource(String baseUrl, RestTemplate restTemplate) {
        Assert.hasText(baseUrl, "baseUrl is required");
        this.baseUrl = baseUrl;
        setRestTemplate(restTemplate);
    }

    @Override
    protected String getBaseParentApiUrl() {
        return baseUrl + "/api/company";
    }

    @Override
    protected String getChildPath() {
        return "employee";
    }

    @Override
    protected ParameterizedTypeReference<PageableResult<EmployeeRead>> createPagedTypeReference() {
        return new ParameterizedTypeReference<PageableResult<EmployeeRead>>() {
        };
    }
}
