package io.rocketbase.commons.openapi.sample.resource;

import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.generator.InfiniteHook;
import io.rocketbase.commons.generator.QueryHook;
import io.rocketbase.commons.openapi.sample.dto.Activity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@SecurityRequirement(name = "OAuth2", scopes = {"user"})
@Tag(name = "activity", description = "actions within system.")
@RequestMapping(path = "/activity", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
public interface ActivityApi {

    @InfiniteHook(value = "findAll", cacheKeys = "activity,list")
    @Operation(description = "list all tiles (pinboards, searchconfigs, briefings or showrooms a user has access to")
    @GetMapping(
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE,
            consumes = MimeTypeUtils.ALL_VALUE
    )
    ResponseEntity<PageableResult<Activity>> loadActivities(@ParameterObject
                                                            @SortDefault.SortDefaults({
                                                                    @SortDefault(sort = "lastModified", direction = Sort.Direction.DESC)
                                                            }) Pageable pageable,
                                                            @Parameter(description = "freetext search, tries to find phrase in any of the activity name") @Valid @RequestParam(value = "query", required = false) Optional<String> query,
                                                            @Parameter(description = "filters by activity type") @Valid @RequestParam(value = "activityType", required = false) Optional<String> activityType);


    @QueryHook(value = "findById", cacheKeys = "activity,detail,${id}")
    @GetMapping(
            path = "/{id}",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE,
            consumes = MimeTypeUtils.ALL_VALUE
    )
    ResponseEntity<Activity> findById(@PathVariable("id") String id);

    @GetMapping(
            path = "/hosted/{id}",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE,
            consumes = MimeTypeUtils.ALL_VALUE
    )
    ResponseEntity<Activity> findByHosted(@PathVariable("id") String id);

    @InfiniteHook(cacheKeys = "validate, logs", staleTime = 1)
    @Operation(summary = "Returns the last 10 synchronization log entries for content with given id. The results are ordered from latest to earliest.")
    @ApiResponse(responseCode = "200", description = "The list of the last 10 sync log entries")
    @GetMapping(path = "/validate/logs", produces = MediaType.APPLICATION_JSON_VALUE)
    PageableResult<Activity> getAll(@ParameterObject @SortDefault(sort = "dated", direction = Sort.Direction.DESC) Pageable pageable);

}
