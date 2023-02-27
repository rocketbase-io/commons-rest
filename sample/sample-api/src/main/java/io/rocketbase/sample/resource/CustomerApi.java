package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.obfuscated.ObfuscatedId;
import io.rocketbase.sample.dto.customer.CustomerRead;
import io.rocketbase.sample.dto.customer.CustomerWrite;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public interface CustomerApi {


    @GetMapping(path = "/customer",
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    PageableResult<CustomerRead> find(@ParameterObject @SortDefault.SortDefaults({@SortDefault(sort = "id", direction = Sort.Direction.ASC)}) Pageable pageable);


    @PostMapping(path = "/customer",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    CustomerRead create(@RequestBody @NotNull @Validated CustomerWrite write);


    @GetMapping(path = "/customer/{id}",
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    CustomerRead getById(@PathVariable("id") ObfuscatedId id);


    @PutMapping(path = "/customer/{id}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    CustomerRead update(@PathVariable("id") ObfuscatedId id, @RequestBody @NotNull @Validated CustomerWrite write);


    @DeleteMapping(path = "/customer/{id}")
    void delete(@PathVariable("id") ObfuscatedId id);
}
