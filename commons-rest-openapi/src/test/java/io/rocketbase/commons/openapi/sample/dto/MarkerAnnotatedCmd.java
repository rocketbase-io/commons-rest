package io.rocketbase.commons.openapi.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command using the non-null marker annotations (instead of jakarta.validation constraints)
 * that the generator accepts as required-markers by default.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarkerAnnotatedCmd {

    @org.springframework.lang.NonNull
    private String springMarked;

    @jakarta.annotation.Nonnull
    private String jakartaMarked;

    // jspecify is TYPE_USE-only: the annotation sits on the type, not the field
    private @org.jspecify.annotations.NonNull String jspecifyMarked;

    private String plain;
}
