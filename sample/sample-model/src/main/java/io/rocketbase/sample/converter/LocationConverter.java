package io.rocketbase.sample.converter;

import io.rocketbase.sample.dto.localtion.LocationRead;
import io.rocketbase.sample.model.LocationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LocationConverter {

    private final TsidMapper tsidMapper;

    public LocationRead fromEntity(LocationEntity entity) {
        if (entity == null) {
            return null;
        }
        return LocationRead.builder()
                .id(tsidMapper.asTsid(entity.getId()))
                .name(entity.getName())
                .city(entity.getCity())
                .street(entity.getStreet())
                .country(entity.getCountry())
                .build();
    }

    public List<LocationRead> fromEntities(List<LocationEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::fromEntity)
                .collect(Collectors.toList());
    }
}
