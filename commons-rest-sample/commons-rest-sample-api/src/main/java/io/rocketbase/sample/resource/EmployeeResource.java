package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.resource.AbstractCrudChildRestResource;
import io.rocketbase.sample.dto.employee.EmployeeRead;
import io.rocketbase.sample.dto.employee.EmployeeWrite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class EmployeeResource extends AbstractCrudChildRestResource<EmployeeRead, EmployeeWrite, String> {

    @Value("${resource.base.api.url}")
    private String baseApiUrl;

    @Override
    protected String getBaseParentApiUrl() {
        return baseApiUrl + "/api/company";
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
