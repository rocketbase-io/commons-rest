package io.rocketbase.sample.controller;

import io.rocketbase.commons.controller.BaseController;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.NotFoundException;
import io.rocketbase.commons.obfuscated.ObfuscatedId;
import io.rocketbase.sample.converter.CustomerConverter;
import io.rocketbase.sample.dto.customer.CustomerRead;
import io.rocketbase.sample.dto.customer.CustomerWrite;
import io.rocketbase.sample.model.CustomerEntity;
import io.rocketbase.sample.repository.jpa.CustomerRepository;
import io.rocketbase.sample.resource.CustomerApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api")
public class CustomerController implements BaseController, CustomerApi {

    private final CustomerRepository repository;
    private final CustomerConverter converter;


    public PageableResult<CustomerRead> find(Pageable pageable) {
        Page<CustomerEntity> entities = repository.findAll(pageable);
        return PageableResult.contentPage(converter.fromEntities(entities.getContent()), entities);
    }

    public CustomerRead create(CustomerWrite write) {
        CustomerEntity entity = repository.save(converter.newEntity(write));
        return converter.fromEntity(entity);
    }


    public CustomerRead getById(ObfuscatedId id) {
        CustomerEntity entity = getEntity(id);
        return converter.fromEntity(entity);
    }

    public CustomerRead update(ObfuscatedId id, CustomerWrite write) {
        CustomerEntity entity = getEntity(id);
        converter.updateEntityFromEdit(write, entity);
        repository.save(entity);
        return converter.fromEntity(entity);
    }

    public void delete(ObfuscatedId id) {
        CustomerEntity entity = getEntity(id);
        repository.delete(entity);
    }

    /**
     * get by Id or throw {@link NotFoundException}
     *
     * @param id obfuscated unique identifier
     * @return entity
     */
    protected CustomerEntity getEntity(ObfuscatedId id) {
        return repository.findById(id.getId())
                .orElseThrow(() -> new NotFoundException());
    }

}
