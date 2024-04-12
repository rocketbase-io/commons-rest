package io.rocketbase.commons.openapi.sample.resource;

import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.generator.InfiniteHook;
import io.rocketbase.commons.generator.QueryHook;
import io.rocketbase.commons.openapi.sample.dto.Tile;
import io.rocketbase.commons.openapi.sample.dto.UserPreference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@SecurityRequirement(name = "OAuth2", scopes = {"user"})
@Tag(name = "tile", description = "tile stuff :)")
@RequestMapping(path = "/tile", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
public interface TileApi {

    @InfiniteHook(value = "findAll", cacheKeys = "tile,list")
    @Operation(description = "list all tiles (pinboards, searchconfigs, briefings or showrooms a user has access to")
    @GetMapping(
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE,
            consumes = MimeTypeUtils.ALL_VALUE
    )
    ResponseEntity<PageableResult<Tile>> loadTiles(@ParameterObject
                                                   @SortDefault.SortDefaults({
                                                           @SortDefault(sort = "lastModified", direction = Sort.Direction.DESC)
                                                   }) Pageable pageable,
                                                   @Parameter(description = "freetext search, tries to find phrase in any of the tile name, description") @Valid @RequestParam(value = "query", required = false) Optional<String> query,
                                                   @Parameter(description = "filters by tiletype") @Valid @RequestParam(value = "tileType", required = false) Optional<String> tileType,
                                                   @Parameter(description = "filters by labels") @Valid @RequestParam(value = "labels", required = false) List<String> labelNames,
                                                   @Parameter(description = "filters by sharelevel") @Valid @RequestParam(value = "shareLevel", required = false) Optional<String> shareLevel,
                                                   @Parameter(description = "filters by userPreference") @Valid @RequestParam(value = "userPreference", required = false) Optional<UserPreference> userPreference,
                                                   @Parameter(description = "filters by categoryId") @Valid @RequestParam(value = "categoryId", required = false) Set<Integer> categoryIds);

    @QueryHook(value = "findOne", cacheKeys = "tile,detail,${id}")
    @GetMapping(
            path = "/tile/{id}",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE,
            consumes = MimeTypeUtils.ALL_VALUE
    )
    Tile get(@Parameter(description = "id of tile", required = true)
             @PathVariable("id") String id);
}
