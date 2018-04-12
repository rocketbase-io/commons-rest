package io.rocketbase.sample.converter;

import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.sample.dto.data.EmployeeData;
import io.rocketbase.sample.dto.edit.EmployeeEdit;
import io.rocketbase.sample.model.Employee;
import org.mapstruct.*;

import java.util.List;

@Mapper(config = CentralConfig.class, uses = {CompanyConverter.class})
public interface EmployeeConverter extends EntityReadWriteConverter<Employee, EmployeeData, EmployeeEdit> {
    @Mappings({
            @Mapping(target = "company", ignore = true)
    })
    Employee toEntity(EmployeeData data);

    @InheritInverseConfiguration
    EmployeeData fromEntity(Employee entity);

    List<EmployeeData> fromEntities(List<Employee> entities);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "company", ignore = true),
    })
    Employee newEntity(EmployeeEdit edit);

    @InheritConfiguration()
    Employee updateEntityFromEdit(EmployeeEdit edit, @MappingTarget Employee entity);
}