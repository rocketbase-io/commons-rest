package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.sample.dto.company.CompanyRead;
import io.rocketbase.sample.dto.company.CompanyWrite;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


public interface CompanyApi {

    @GetMapping(path = "/company/{id}",
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    CompanyRead findById(@PathVariable("id") String id);


    @PutMapping(path = "/company/{id}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    CompanyRead update(@PathVariable("id") String id, @RequestBody @NotNull @Valid CompanyWrite CompanyWrite);


    @DeleteMapping(path = "/company/{id}")
    void delete(@PathVariable("id") String String);


    @GetMapping(path = "/company",
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    PageableResult<CompanyRead> find(@ParameterObject @SortDefault.SortDefaults({@SortDefault(sort = "id", direction = Sort.Direction.ASC)}) Pageable pageable);


    @PostMapping(path = "/company",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    CompanyRead create(@RequestBody @NotNull @Valid CompanyWrite CompanyWrite);
}
