package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.sample.dto.employee.EmployeeRead;
import io.rocketbase.sample.dto.employee.EmployeeWrite;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


public interface EmployeeApi {

    @GetMapping(path = "/company/{parentId}/employee",
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    PageableResult<EmployeeRead> find(@PathVariable("parentId") String parentId,
                                      @ParameterObject @SortDefault.SortDefaults({@SortDefault(sort = "id", direction = Sort.Direction.ASC)}) Pageable pageable);

    @GetMapping(path = "/company/{parentId}/employee/{id}",
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    EmployeeRead getById(@PathVariable("parentId") String parentId, @PathVariable("id") String id);


    @PostMapping(path = "/company/{parentId}/employee",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    EmployeeRead create(@PathVariable("parentId") String parentId, @RequestBody @NotNull @Validated EmployeeWrite write);


    @PutMapping(path = "/company/{parentId}/employee/{id}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    EmployeeRead update(@PathVariable("parentId") String parentId, @PathVariable("id") String id, @RequestBody @NotNull @Validated EmployeeWrite write);


    @DeleteMapping(path = "/company/{parentId}/employee/{id}")
    void delete(@PathVariable("parentId") String parentId, @PathVariable("id") String id);

}
