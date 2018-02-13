package io.rocketbase.sample.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.resource.AbstractCrudChildRestResource;
import io.rocketbase.sample.dto.data.EmployeeData;
import io.rocketbase.sample.dto.edit.EmployeeEdit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmployeeResource extends AbstractCrudChildRestResource<EmployeeData, EmployeeEdit, String> {

    @Value("${resource.base.api.url}")
    private String baseApiUrl;

    @Autowired
    public EmployeeResource(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected String getBaseParentApiUrl() {
        return baseApiUrl + "/api/company";
    }

    @Override
    protected String getChildPath() {
        return "employee";
    }

    @Override
    protected TypeReference<PageableResult<EmployeeData>> createPagedTypeReference() {
        return new TypeReference<PageableResult<EmployeeData>>() {
        };
    }
}
