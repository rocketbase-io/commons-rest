package io.rocketbase.commons.openapi.sample.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CommentActivity.class, name = "comment"),
        @JsonSubTypes.Type(value = RecommendationActivity.class, name = "dossier"),
})
public abstract class Activity {

    private String id;

    private Instant dated;

    private String user;
}
