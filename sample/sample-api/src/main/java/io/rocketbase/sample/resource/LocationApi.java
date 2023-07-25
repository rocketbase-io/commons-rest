package io.rocketbase.sample.resource;

import io.hypersistence.tsid.TSID;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.sample.dto.localtion.LocationRead;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


public interface LocationApi {

    @GetMapping(path = "/location",
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    PageableResult<LocationRead> findAll(@ParameterObject @SortDefault.SortDefaults({@SortDefault(sort = "id", direction = Sort.Direction.ASC)}) Pageable pageable);

    @GetMapping(path = "/location/{id}",
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    LocationRead getById(@PathVariable("id") TSID id);


}
