package io.rocketbase.sample.converter;

import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.sample.dto.company.CompanyRead;
import io.rocketbase.sample.dto.company.CompanyWrite;
import io.rocketbase.sample.model.CompanyEntity;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class)
public interface CompanyConverter extends EntityReadWriteConverter<CompanyEntity, CompanyRead, CompanyWrite> {

    CompanyRead fromEntity(CompanyEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    CompanyEntity newEntity(CompanyWrite workspace);

    @InheritConfiguration()
    CompanyEntity updateEntityFromEdit(CompanyWrite write, @MappingTarget CompanyEntity entity);
}
