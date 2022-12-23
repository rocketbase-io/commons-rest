package io.rocketbase.sample.controller;

import io.rocketbase.commons.controller.BaseController;
import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.sample.repository.jpa.CustomJpaRepository;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
public abstract class AbstractBaseCrudController<Entity, Read, Write, ID extends Serializable, Converter extends EntityReadWriteConverter<Entity, Read, Write>> implements BaseController {

    @Getter(AccessLevel.PROTECTED)
    private final CustomJpaRepository<Entity, ID> repository;

    @Getter(AccessLevel.PROTECTED)
    private final Converter converter;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public PageableResult<Read> find(@RequestParam(required = false) MultiValueMap<String, String> params) {
        Page<Entity> entities = repository.findAll(parsePageRequest(params, getDefaultSort()));
        return PageableResult.contentPage(converter.fromEntities(entities.getContent()), entities);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    public Read create(@RequestBody @NotNull @Validated Write write) {
        Entity entity = repository.save(converter.newEntity(write));
        return converter.fromEntity(entity);
    }

    /**
     * @return default sort in case nothing is given via parameter
     */
    protected Sort getDefaultSort() {
        return Sort.unsorted();
    }


}
