package io.rocketbase.sample.converter;

import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.commons.obfuscated.ObfuscatedId;
import io.rocketbase.sample.dto.customer.CustomerRead;
import io.rocketbase.sample.dto.customer.CustomerWrite;
import io.rocketbase.sample.model.CustomerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerConverter implements EntityReadWriteConverter<CustomerEntity, CustomerRead, CustomerWrite> {

    private final ObfuscatedIdMapper obfuscatedIdMapper;

    @Override
    public CustomerRead fromEntity(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }
        return CustomerRead.builder()
                .id(obfuscatedIdMapper.asObfuscatedId(entity.getId()))
                .name(entity.getName())
                .build();
    }

    @Override
    public CustomerEntity newEntity(CustomerWrite write) {
        if (write == null) {
            return null;
        }
        return CustomerEntity.builder()
                .name(write.getName())
                .build();
    }

    @Override
    public CustomerEntity updateEntityFromEdit(CustomerWrite write, CustomerEntity entity) {
        if (write == null || entity == null) {
            return entity;
        }
        entity.setName(write.getName());
        return entity;
    }
}
