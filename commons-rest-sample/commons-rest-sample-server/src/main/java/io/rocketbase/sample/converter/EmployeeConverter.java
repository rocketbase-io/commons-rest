package io.rocketbase.sample.converter;

import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.sample.dto.employee.EmployeeRead;
import io.rocketbase.sample.dto.employee.EmployeeWrite;
import io.rocketbase.sample.model.EmployeeEntity;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class)
public interface EmployeeConverter extends EntityReadWriteConverter<EmployeeEntity, EmployeeRead, EmployeeWrite> {

    EmployeeRead fromEntity(EmployeeEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "company", ignore = true),
    })
    EmployeeEntity newEntity(EmployeeWrite workspace);

    @InheritConfiguration()
    EmployeeEntity updateEntityFromEdit(EmployeeWrite write, @MappingTarget EmployeeEntity entity);
}
