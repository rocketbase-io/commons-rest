package io.rocketbase.commons.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Simple record implementation of {@link PageableResult}.
 * <p>
 * This implementation provides an immutable pageable result for basic pagination scenarios.
 * All fields are required and validated at construction time.
 * </p>
 *
 * @param <T> the type of elements in the page content
 */
@Schema(description = "Standard pageable result implementation")
public record PageableResultImpl<T>(
        List<T> content,
        int page,
        int pageSize,
        long totalElements,
        int totalPages
) implements PageableResult<T> {

    /**
     * Compact constructor with validation.
     * Ensures all values are within valid ranges.
     */
    public PageableResultImpl {
        // Validation
        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (pageSize < 0) {
            throw new IllegalArgumentException("pageSize must be >= 0");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must be >= 0");
        }
        if (totalPages < 0) {
            throw new IllegalArgumentException("totalPages must be >= 0");
        }
    }
}
