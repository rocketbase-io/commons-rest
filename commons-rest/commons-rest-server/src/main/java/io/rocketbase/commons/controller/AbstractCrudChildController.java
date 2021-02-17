package io.rocketbase.commons.controller;

import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.commons.dto.PageableResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpStatus;
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
 * @param <Read>      response data object
 * @param <Write>     object with all properties that are changeable
 * @param <ID>        class of the identifier
 * @param <Converter> interface that allows converting between Entity, Read and Write
 */
@RequiredArgsConstructor
public abstract class AbstractCrudChildController<Entity, Read, Write, ID extends Serializable, Converter extends EntityReadWriteConverter<Entity, Read, Write>> implements BaseController {

    @Getter(AccessLevel.PROTECTED)
    private final PagingAndSortingRepository<Entity, ID> repository;

    @Getter(AccessLevel.PROTECTED)
    private final Converter converter;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public PageableResult<Read> find(@PathVariable("parentId") ID parentId, @RequestParam(required = false) MultiValueMap<String, String> params) {
        Page<Entity> entities = findAllByParentId(parentId, parsePageRequest(params, getDefaultSort()));
        return PageableResult.contentPage(converter.fromEntities(entities.getContent()), entities);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    @ResponseBody
    public Read getById(@PathVariable("parentId") ID parentId, @PathVariable("id") ID id) {
        Entity entity = getEntity(parentId, id);
        return converter.fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    public Read create(@PathVariable("parentId") ID parentId, @RequestBody @NotNull @Validated Write write) {
        Entity entity = repository.save(newEntity(parentId, write));
        return converter.fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Read update(@PathVariable("parentId") ID parentId, @PathVariable ID id, @RequestBody @NotNull @Validated Write write) {
        Entity entity = getEntity(parentId, id);
        converter.updateEntityFromEdit(write, entity);
        repository.save(entity);
        return converter.fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public void delete(@PathVariable("parentId") ID parentId, @PathVariable("id") ID id) {
        Entity entity = getEntity(parentId, id);
        repository.delete(entity);
    }

    /**
     * @return default sort in case nothing is given via parameter
     */
    protected Sort getDefaultSort() {
        return Sort.unsorted();
    }

    /**
     * should find entity by given parentId and id. in case anything is not fitting: for example parentId with id it could throw for example {@link io.rocketbase.commons.exception.NotFoundException}
     */
    protected abstract Entity getEntity(ID parentId, ID id);

    /**
     * should return a page of all entities that fit to the given parentId and use of pageable
     */
    protected abstract Page<Entity> findAllByParentId(ID parentId, Pageable pageable);

    /**
     * should create a new entity and take care to store the relation with parent
     */
    protected abstract Entity newEntity(ID parentId, Write writeData);
}
