package io.rocketbase.commons.converter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * interface that handles converting between Entity, Read and Write
 *
 * @param <Entity> database entity
 * @param <Read>   response data object
 * @param <Write>  object with all properties that are changeable
 */
public interface EntityReadWriteConverter<Entity, Read, Write> {

    /**
     * convert an entity to ReadDto
     *
     * @param entity database entity
     * @return response data object
     */
    Read fromEntity(Entity entity);

    /**
     * convert list of entities to list of ReadDtos
     *
     * @param entities list of entities
     * @return converted data list
     */
    default List<Read> fromEntities(List<Entity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(e -> fromEntity(e))
                .collect(Collectors.toList());
    }

    /**
     * create new entity by given write
     *
     * @param write values to map to entity
     * @return new entity with edit values
     */
    Entity newEntity(Write write);

    /**
     * update existing entity by given WriteDto
     *
     * @param write  values to map to entity
     * @param entity existing entity
     * @return updated entity
     */
    Entity updateEntityFromEdit(Write write, Entity entity);
}
