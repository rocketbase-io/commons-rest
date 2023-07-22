package io.rocketbase.commons.openapi.sample.resource;

import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.openapi.sample.dto.Tile;
import io.rocketbase.commons.openapi.sample.dto.UserPreference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
public class TileController implements TileApi {
    @Override
    public ResponseEntity<PageableResult<Tile>> loadTiles(Pageable pageable, Optional<String> query, Optional<String> tileType, List<String> labelNames, Optional<String> shareLevel, Optional<UserPreference> userPreference, Set<Integer> categoryIds) {
        return null;
    }

    @Override
    public Tile get(String id) {
        return null;
    }
}
