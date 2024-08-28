package io.rocketbase.commons.openapi.sample.dto;

import io.hypersistence.tsid.TSID;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
@Data
public class Tile implements Serializable {

    private TSID id;

    private TileType type;

    private String name;

    private String description;

    private int commentCount;

    private int itemCount;

    private UserPreference userPreference;

    private Instant created;

    private Instant lastModified;
}
