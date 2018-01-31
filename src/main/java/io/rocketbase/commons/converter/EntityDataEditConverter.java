package io.rocketbase.commons.converter;

import java.util.List;

public interface EntityDataEditConverter<Entity, Data, Edit> {

    Entity toEntity(Data data);

    Data fromEntity(Entity entity);

    List<Data> fromEntities(List<Entity> entities);

    Entity newEntity(Edit edit);

    void updateEntityFromEdit(Edit edit, Entity entity);
}
