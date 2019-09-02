package io.rocketbase.sample.converter;

import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.sample.dto.customer.CustomerRead;
import io.rocketbase.sample.dto.customer.CustomerWrite;
import io.rocketbase.sample.model.CustomerEntity;
import org.mapstruct.*;

@Mapper(config = CentralConfig.class)
public interface CustomerConverter extends EntityReadWriteConverter<CustomerEntity, CustomerRead, CustomerWrite> {

    CustomerRead fromEntity(CustomerEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    CustomerEntity newEntity(CustomerWrite workspace);

    @InheritConfiguration()
    CustomerEntity updateEntityFromEdit(CustomerWrite write, @MappingTarget CustomerEntity entity);
}
