package io.rocketbase.commons.controller;

import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.NotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 * simple CRUD controller that serves entities
 *
 * @param <Entity>    database entity
 * @param <Read>      response data object
 * @param <Write>     object with all properties that are changeable
 * @param <ID>        class of the identifier
 * @param <Converter> interface that allows converting between Entity, Read and Write
 */
@RequiredArgsConstructor
public abstract class AbstractCrudController<Entity, Read, Write, ID extends Serializable, Converter extends EntityReadWriteConverter<Entity, Read, Write>> implements BaseController {

    @Getter(AccessLevel.PROTECTED)
    private final PagingAndSortingRepository<Entity, ID> repository;

    @Getter(AccessLevel.PROTECTED)
    private final EntityReadWriteConverter<Entity, Read, Write> converter;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public PageableResult<Read> find(@RequestParam(required = false) MultiValueMap<String, String> params) {
        Page<Entity> entities = repository.findAll(parsePageRequest(params));
        return PageableResult.contentPage(converter.fromEntities(entities.getContent()), entities);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    @ResponseBody
    public Read getById(@PathVariable("id") ID id) {
        Entity entity = getEntity(id);
        return converter.fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Read create(@RequestBody @NotNull @Validated Write write) {
        Entity entity = repository.save(converter.newEntity(write));
        return converter.fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Read update(@PathVariable ID id, @RequestBody @NotNull @Validated Write write) {
        Entity entity = getEntity(id);
        converter.updateEntityFromEdit(write, entity);
        repository.save(entity);
        return converter.fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public void delete(@PathVariable("id") ID id) {
        Entity entity = getEntity(id);
        repository.delete(entity);
    }

    protected Entity getEntity(ID id) {
        Entity entity = repository.findOne(id);
        if (entity == null) {
            throw new NotFoundException();
        }
        return entity;
    }


}
