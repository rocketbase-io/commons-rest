package io.rocketbase.sample.converter;

import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.sample.dto.company.CompanyRead;
import io.rocketbase.sample.dto.employee.EmployeeRead;
import io.rocketbase.sample.dto.employee.EmployeeWrite;
import io.rocketbase.sample.model.EmployeeEntity;
import io.rocketbase.sample.repository.mongo.CompanyRepository;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = CentralConfig.class)
public abstract class EmployeeConverter implements EntityReadWriteConverter<EmployeeEntity, EmployeeRead, EmployeeWrite> {

    @Autowired
    protected CompanyRepository companyRepository;

    @Autowired
    protected CompanyConverter companyConverter;

    @Mapping(target = "company", expression = "java( convertCompanyId( entity.getCompanyId() ) )")
    public abstract EmployeeRead fromEntity(EmployeeEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    public abstract EmployeeEntity newEntity(EmployeeWrite workspace);

    @InheritConfiguration()
    public abstract EmployeeEntity updateEntityFromEdit(EmployeeWrite write, @MappingTarget EmployeeEntity entity);

    public CompanyRead convertCompanyId(String companyId) {
        return companyConverter.fromEntity(companyRepository.findById(companyId).orElseGet(null));
    }
}
