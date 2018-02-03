package io.rocketbase.sample.converter;

import io.rocketbase.commons.converter.EntityDataEditConverter;
import io.rocketbase.sample.dto.data.CompanyData;
import io.rocketbase.sample.dto.edit.CompanyEdit;
import io.rocketbase.sample.model.Company;
import org.mapstruct.*;

import java.util.List;

@Mapper(config = CentralConfig.class)
public interface CompanyConverter extends EntityDataEditConverter<Company, CompanyData, CompanyEdit> {

    Company toEntity(CompanyData data);

    @InheritInverseConfiguration
    CompanyData fromEntity(Company entity);

    List<CompanyData> fromEntities(List<Company> entities);

    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    Company newEntity(CompanyEdit workspace);

    @InheritConfiguration()
    void updateEntityFromEdit(CompanyEdit edit, @MappingTarget Company entity);
}
