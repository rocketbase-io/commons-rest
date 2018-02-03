package io.rocketbase.commons.controller;

import io.rocketbase.commons.converter.EntityDataEditConverter;
import io.rocketbase.commons.dto.PageableResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * simple CRUD controller that serves child entities of it's parent<br>
 * <b>It's important that you place the pathVariable parentId in your main RequestMapping</b>
 *
 * @param <Entity>    database entity
 * @param <Data>      response data object
 * @param <Edit>      object with all properties that are changeable
 * @param <ID>        class of the identifier
 * @param <Converter> interface that allows converting between Entity, Data and Edit
 */
@RequiredArgsConstructor
public abstract class AbstractCrudChildController<Entity, Data, Edit, ID extends Serializable, Converter extends EntityDataEditConverter<Entity, Data, Edit>> implements BaseController {

    @Getter(AccessLevel.PROTECTED)
    private final PagingAndSortingRepository<Entity, ID> repository;

    @Getter(AccessLevel.PROTECTED)
    private final EntityDataEditConverter<Entity, Data, Edit> converter;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public PageableResult<Data> find(@PathVariable("parentId") ID parentId, @RequestParam(required = false) MultiValueMap<String, String> params) {
        Page<Entity> entities = findAllByParentId(parentId, parsePageRequest(params));
        return PageableResult.contentPage(converter.fromEntities(entities.getContent()), entities);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    @ResponseBody
    public Data getById(@PathVariable("parentId") ID parentId, @PathVariable("id") ID id) {
        Entity entity = getEntity(parentId, id);
        return converter.fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Data create(@PathVariable("parentId") ID parentId, @RequestBody @NotNull @Validated Edit editData) {
        Entity entity = repository.save(newEntity(parentId, editData));
        return converter.fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Data update(@PathVariable("parentId") ID parentId, @PathVariable ID id, @RequestBody @NotNull @Validated Edit editData) {
        Entity entity = getEntity(parentId, id);
        converter.updateEntityFromEdit(editData, entity);
        repository.save(entity);
        return converter.fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public void delete(@PathVariable("parentId") ID parentId, @PathVariable("id") ID id) {
        Entity entity = getEntity(parentId, id);
        repository.delete(entity);
    }

    protected abstract Entity getEntity(ID parentId, ID id);

    protected abstract Page<Entity> findAllByParentId(ID parentId, PageRequest pageRequest);

    protected abstract Entity newEntity(ID parentId, Edit editData);
}
