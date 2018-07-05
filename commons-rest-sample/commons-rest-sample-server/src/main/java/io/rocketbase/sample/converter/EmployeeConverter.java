package io.rocketbase.sample.converter;

import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.sample.dto.employee.EmployeeRead;
import io.rocketbase.sample.dto.employee.EmployeeWrite;
import io.rocketbase.sample.model.Employee;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class, uses = {CompanyConverter.class})
public interface EmployeeConverter extends EntityReadWriteConverter<Employee, EmployeeRead, EmployeeWrite> {

    EmployeeRead fromEntity(Employee entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "company", ignore = true),
    })
    Employee newEntity(EmployeeWrite edit);

    @InheritConfiguration()
    Employee updateEntityFromEdit(EmployeeWrite edit, @MappingTarget Employee entity);
}
