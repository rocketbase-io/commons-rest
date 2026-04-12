package io.rocketbase.commons.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Extended record implementation of {@link PageableResult} with additional metadata support.
 * <p>
 * This implementation allows you to attach custom metadata to paginated results.
 * The metadata field can be used for various purposes such as:
 * <ul>
 *   <li>Summary information (totals, aggregations, statistics)</li>
 *   <li>GeoJSON feature collections</li>
 *   <li>Revision hashes or versioning information</li>
 *   <li>Any other domain-specific metadata</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of elements in the page content
 * @param <M> the type of metadata attached to this result
 */
@Schema(description = "Pageable result with additional metadata support")
public record PageableResultWithMeta<T, M>(
        @Schema(description = "content of current page")
        List<T> content,

        @Schema(description = "current page (starts by 0)")
        int page,

        @Schema(description = "maximum size of content list")
        int pageSize,

        @Schema(description = "total count of values in database")
        long totalElements,

        @Schema(description = "count of pages in total with given pageSize")
        int totalPages,

        @Schema(description = "additional metadata (summary, GeoJSON, etc.)")
        @Nullable
        M meta
) implements PageableResult<T> {

    /**
     * Compact constructor with validation.
     * Ensures all values are within valid ranges. Meta can be null.
     */
    public PageableResultWithMeta {
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

    /**
     * Creates a PageableResultWithMeta from an existing PageableResult by adding metadata.
     *
     * @param <T>    the type of elements in the page content
     * @param <M>    the type of metadata
     * @param result the source PageableResult
     * @param meta   the metadata to attach
     * @return a new PageableResultWithMeta with the provided metadata
     */
    public static <T, M> PageableResultWithMeta<T, M> withMeta(PageableResult<T> result, @Nullable M meta) {
        return new PageableResultWithMeta<>(
                result.content(),
                result.page(),
                result.pageSize(),
                result.totalElements(),
                result.totalPages(),
                meta
        );
    }
}
