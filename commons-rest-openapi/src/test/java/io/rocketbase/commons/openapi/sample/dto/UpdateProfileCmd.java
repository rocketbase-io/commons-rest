package io.rocketbase.commons.openapi.sample.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Java Record for testing record support in TypeScript generation.
 */
public record UpdateProfileCmd(
    @NotBlank
    @Size(min = 1, max = 100)
    String displayName,

    @Size(max = 500)
    String bio,

    String avatarUrl
) {
}
