package io.rocketbase.commons.converter;

import java.util.List;

/**
 * interface that handels converting between Entity, Data and Edit
 *
 * @param <Entity> database entity
 * @param <Data>   response data object
 * @param <Edit>   object with all properties that are changeable
 */
public interface EntityDataEditConverter<Entity, Data, Edit> {

    Entity toEntity(Data data);

    Data fromEntity(Entity entity);

    List<Data> fromEntities(List<Entity> entities);

    Entity newEntity(Edit edit);

    void updateEntityFromEdit(Edit edit, Entity entity);
}
