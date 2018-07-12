package io.rocketbase.sample.controller;

import io.rocketbase.commons.controller.AbstractCrudChildController;
import io.rocketbase.sample.converter.EmployeeConverter;
import io.rocketbase.sample.dto.employee.EmployeeRead;
import io.rocketbase.sample.dto.employee.EmployeeWrite;
import io.rocketbase.sample.model.EmployeeEntity;
import io.rocketbase.sample.model.CompanyEntity;
import io.rocketbase.sample.repository.EmployeeRepository;
import io.rocketbase.sample.repository.CompanyRepository;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.rocketbase.commons.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Optional;


@Slf4j
@RestController
@RequestMapping("/api/company/{parentId}/employee")
public class EmployeeController extends AbstractCrudChildController<EmployeeEntity, EmployeeRead, EmployeeWrite, String, EmployeeConverter> {

    @Resource
    private CompanyRepository companyRepository;

    @Autowired
    public EmployeeController(EmployeeRepository repository, EmployeeConverter converter) {
        super(repository, converter);
    }

    @Override
    protected EmployeeEntity getEntity(String parentId, String id) {
        return getRepository().findOneByCompanyIdAndId(parentId, id)
                .orElseThrow(() -> new NotFoundException());
    }

    @Override
    protected Page<EmployeeEntity> findAllByParentId(String parentId, Pageable pageable) {
        return getRepository().findAllByCompanyId(parentId, pageable);
    }

    @Override
    protected EmployeeEntity newEntity(String parentId, EmployeeWrite employeeWrite) {
        CompanyEntity companyEntity = companyRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException());

        EmployeeEntity employeeEntity = getConverter().newEntity(employeeWrite);
        employeeEntity.setCompany(companyEntity);
        return employeeEntity;
    }

    @Override
    protected EmployeeRepository getRepository() {
        return (EmployeeRepository) super.getRepository();
    }

    @Override
    protected Sort getDefaultSort() {
        return Sort.by("id");
    }
}
