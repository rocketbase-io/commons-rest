package io.rocketbase.commons.controller;

import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.commons.exception.NotFoundException;
import org.springframework.data.repository.PagingAndSortingRepository;
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
public abstract class AbstractCrudController<Entity, Read, Write, ID extends Serializable, Converter extends EntityReadWriteConverter<Entity, Read, Write>> extends AbstractBaseCrudController<Entity, Read, Write, ID, Converter> {

    public AbstractCrudController(PagingAndSortingRepository<Entity, ID> repository, Converter converter) {
        super(repository, converter);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    @ResponseBody
    public Read getById(@PathVariable("id") ID id) {
        Entity entity = getEntity(id);
        return getConverter().fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Read update(@PathVariable ID id, @RequestBody @NotNull @Validated Write write) {
        Entity entity = getEntity(id);
        getConverter().updateEntityFromEdit(write, entity);
        getRepository().save(entity);
        return getConverter().fromEntity(entity);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public void delete(@PathVariable("id") ID id) {
        Entity entity = getEntity(id);
        getRepository().delete(entity);
    }

    /**
     * get by Id or throw {@link NotFoundException}
     *
     * @param id unique identifier
     * @return entity
     */
    protected Entity getEntity(ID id) {
        return getRepository().findById(id)
                .orElseThrow(() -> new NotFoundException());
    }


}
