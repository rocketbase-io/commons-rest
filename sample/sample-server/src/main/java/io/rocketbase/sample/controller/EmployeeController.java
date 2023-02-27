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
import io.rocketbase.sample.resource.EmployeeApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api")
public class EmployeeController implements BaseController, EmployeeApi {


    private final EmployeeRepository repository;
    private final CompanyRepository companyRepository;
    private final EmployeeConverter converter;

    public PageableResult<EmployeeRead> find(String parentId, Pageable pageable) {
        Page<EmployeeEntity> entities = findAllByParentId(parentId, pageable);
        return PageableResult.contentPage(converter.fromEntities(entities.getContent()), entities);
    }

    public EmployeeRead getById(String parentId, String id) {
        EmployeeEntity entity = getEntity(parentId, id);
        return converter.fromEntity(entity);
    }

    public EmployeeRead create(String parentId, EmployeeWrite write) {
        EmployeeEntity entity = repository.save(newEntity(parentId, write));
        return converter.fromEntity(entity);
    }

    public EmployeeRead update(String parentId, String id, EmployeeWrite write) {
        EmployeeEntity entity = getEntity(parentId, id);
        converter.updateEntityFromEdit(write, entity);
        repository.save(entity);
        return converter.fromEntity(entity);
    }

    public void delete(String parentId, String id) {
        EmployeeEntity entity = getEntity(parentId, id);
        repository.delete(entity);
    }

    protected EmployeeEntity getEntity(String parentId, String id) {
        return repository.findFirstByCompanyIdAndId(parentId, id)
                .orElseThrow(NotFoundException::new);
    }

    protected Page<EmployeeEntity> findAllByParentId(String parentId, Pageable pageable) {
        return repository.findAllByCompanyId(parentId, pageable);
    }

    protected EmployeeEntity newEntity(String parentId, EmployeeWrite employeeWrite) {
        CompanyEntity companyEntity = companyRepository.findById(parentId)
                .orElseThrow(NotFoundException::new);

        EmployeeEntity employeeEntity = converter.newEntity(employeeWrite);
        employeeEntity.setCompanyId(companyEntity.getId());
        return employeeEntity;
    }
}
