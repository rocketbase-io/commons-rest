package io.rocketbase.sample.converter;

import io.rocketbase.sample.dto.localtion.LocationRead;
import io.rocketbase.sample.model.LocationEntity;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = CentralConfig.class)
public interface LocationConverter {

    LocationRead fromEntity(LocationEntity entity);
    
    default List<LocationRead> fromEntities(List<LocationEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(e -> fromEntity(e))
                .collect(Collectors.toList());
    }
}
