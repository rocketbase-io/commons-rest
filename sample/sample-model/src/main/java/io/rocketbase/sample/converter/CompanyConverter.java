package io.rocketbase.sample.converter;

import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.sample.dto.company.CompanyRead;
import io.rocketbase.sample.dto.company.CompanyWrite;
import io.rocketbase.sample.model.CompanyEntity;
import org.springframework.stereotype.Component;

@Component
public class CompanyConverter implements EntityReadWriteConverter<CompanyEntity, CompanyRead, CompanyWrite> {

    @Override
    public CompanyRead fromEntity(CompanyEntity entity) {
        if (entity == null) {
            return null;
        }
        return CompanyRead.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .url(entity.getUrl())
                .build();
    }

    @Override
    public CompanyEntity newEntity(CompanyWrite write) {
        if (write == null) {
            return null;
        }
        return CompanyEntity.builder()
                .name(write.getName())
                .email(write.getEmail())
                .url(write.getUrl())
                .build();
    }

    @Override
    public CompanyEntity updateEntityFromEdit(CompanyWrite write, CompanyEntity entity) {
        if (write == null || entity == null) {
            return entity;
        }
        entity.setName(write.getName());
        entity.setEmail(write.getEmail());
        entity.setUrl(write.getUrl());
        return entity;
    }
}
