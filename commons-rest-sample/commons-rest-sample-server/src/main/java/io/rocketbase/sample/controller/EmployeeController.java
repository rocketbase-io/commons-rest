package io.rocketbase.sample.controller;

import io.rocketbase.commons.controller.BaseController;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.NotFoundException;
import io.rocketbase.sample.converter.EmployeeConverter;
import io.rocketbase.sample.dto.employee.EmployeeRead;
import io.rocketbase.sample.dto.employee.EmployeeWrite;
import io.rocketbase.sample.model.CompanyEntity;
import io.rocketbase.sample.model.EmployeeEntity;
import io.rocketbase.sample.repository.mongo.CompanyRepository;
import io.rocketbase.sample.repository.mongo.EmployeeRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/company/{parentId}/employee")
public class EmployeeController implements BaseController {


    private final EmployeeRepository repository;
    private final CompanyRepository companyRepository;
    private final EmployeeConverter converter;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public PageableResult<EmployeeRead> find(@PathVariable("parentId") String parentId, @RequestParam(required = false) MultiValueMap<String, String> params) {
        Page<EmployeeEntity> entities = findAllByParentId(parentId, parsePageRequest(params, getDefaultSort()));
        return PageableResult.contentPage(converter.fromEntities(entities.getContent()), entities);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    @ResponseBody
    public EmployeeRead getById(@PathVariable("parentId") String parentId, @PathVariable("id") String id) {
        EmployeeEntity entity = getEntity(parentId, id);
        return converter.fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    public EmployeeRead create(@PathVariable("parentId") String parentId, @RequestBody @NotNull @Validated EmployeeWrite write) {
        EmployeeEntity entity = repository.save(newEntity(parentId, write));
        return converter.fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmployeeRead update(@PathVariable("parentId") String parentId, @PathVariable String id, @RequestBody @NotNull @Validated EmployeeWrite write) {
        EmployeeEntity entity = getEntity(parentId, id);
        converter.updateEntityFromEdit(write, entity);
        repository.save(entity);
        return converter.fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public void delete(@PathVariable("parentId") String parentId, @PathVariable("id") String id) {
        EmployeeEntity entity = getEntity(parentId, id);
        repository.delete(entity);
    }

    protected EmployeeEntity getEntity(String parentId, String id) {
        return repository.findOneByCompanyIdAndId(parentId, id)
                .orElseThrow(() -> new NotFoundException());
    }

    protected Page<EmployeeEntity> findAllByParentId(String parentId, Pageable pageable) {
        return repository.findAllByCompanyId(parentId, pageable);
    }

    protected EmployeeEntity newEntity(String parentId, EmployeeWrite employeeWrite) {
        CompanyEntity companyEntity = companyRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException());

        EmployeeEntity employeeEntity = converter.newEntity(employeeWrite);
        employeeEntity.setCompany(companyEntity);
        return employeeEntity;
    }

    protected Sort getDefaultSort() {
        return Sort.by("id");
    }
}
