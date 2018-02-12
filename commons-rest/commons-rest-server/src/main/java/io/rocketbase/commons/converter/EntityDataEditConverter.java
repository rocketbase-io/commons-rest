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

    /**
     * convert data to entity
     *
     * @param data reponse data object
     * @return database entity
     */
    Entity toEntity(Data data);

    /**
     * convert vise versa entity to data
     *
     * @param entity database entity
     * @return response data object
     */
    Data fromEntity(Entity entity);

    /**
     * convert list of entities to data
     *
     * @param entities list of entities
     * @return converted data list
     */
    List<Data> fromEntities(List<Entity> entities);

    /**
     * create new entity by given edit
     *
     * @param edit given edit
     * @return new entity with edit values
     */
    Entity newEntity(Edit edit);

    /**
     * update exisiting entity by given edit
     *
     * @param edit   values to map to entity
     * @param entity existing entity
     */
    void updateEntityFromEdit(Edit edit, Entity entity);
}
