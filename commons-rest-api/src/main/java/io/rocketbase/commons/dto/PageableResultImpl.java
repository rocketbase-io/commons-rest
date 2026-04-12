package io.rocketbase.commons.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        @Schema(description = "content of current page")
        List<T> content,

        @Schema(description = "current page (starts by 0)")
        int page,

        @Schema(description = "maximum size of content list")
        int pageSize,

        @Schema(description = "total count of values in database")
        long totalElements,

        @Schema(description = "count of pages in total with given pageSize")
        int totalPages
) implements PageableResult<T> {

    /**
     * Compact constructor with validation.
     * Ensures all values are within valid ranges.
     */
    public PageableResultImpl {
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
     * Creates a PageableResult from a Spring Data Page with custom content.
     * <p>
     * Use this when you want to transform the page content but keep the pagination metadata.
     * </p>
     *
     * @param content The transformed content list
     * @param page The source Spring Data Page for pagination metadata
     * @param <T> The original page content type
     * @param <E> The transformed content type
     * @return A new PageableResultImpl with the given content and page metadata
     */
    public static <T, E> PageableResultImpl<E> contentPage(List<E> content, Page<T> page) {
        if (page == null) {
            throw new IllegalArgumentException("page cannot be null");
        }
        return new PageableResultImpl<>(
            content != null ? content : Collections.emptyList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    /**
     * Creates a PageableResult from a Spring Data Page with a converter function.
     * <p>
     * Applies the converter to each element in the page content.
     * </p>
     *
     * @param page The source Spring Data Page
     * @param converter Function to transform each element
     * @param <T> The original page content type
     * @param <E> The transformed content type
     * @return A new PageableResultImpl with transformed content
     */
    public static <T, E> PageableResultImpl<E> page(Page<T> page, Function<T, E> converter) {
        if (page == null) {
            throw new IllegalArgumentException("page cannot be null");
        }
        if (converter == null) {
            throw new IllegalArgumentException("converter cannot be null");
        }

        List<E> content = page.getContent().stream()
            .map(converter)
            .collect(Collectors.toList());

        return new PageableResultImpl<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    /**
     * Creates a PageableResult directly from a Spring Data Page.
     *
     * @param page The source Spring Data Page
     * @param <E> The content type
     * @return A new PageableResultImpl with the same content and metadata
     */
    public static <E> PageableResultImpl<E> page(Page<E> page) {
        if (page == null) {
            throw new IllegalArgumentException("page cannot be null");
        }

        return new PageableResultImpl<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    /**
     * Creates a single-page PageableResult from a list of content.
     * <p>
     * The entire list is treated as page 0, with totalElements = content.size().
     * </p>
     *
     * @param content The content list
     * @param <E> The content type
     * @return A new PageableResultImpl containing all content in a single page
     */
    public static <E> PageableResultImpl<E> content(List<E> content) {
        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        int totalElements = content.size();
        return new PageableResultImpl<>(
            content,
            0,
            totalElements,
            totalElements,
            1
        );
    }

    /**
     * Creates a PageableResult from content, Pageable, and total count.
     * <p>
     * Useful for jOOQ and other libraries that don't use Spring Data Page.
     * Automatically calculates totalPages based on totalElements and pageSize.
     * </p>
     *
     * @param content The content list for the current page
     * @param pageable The pagination parameters
     * @param totalElements The total number of elements across all pages
     * @param <E> The content type
     * @return A new PageableResultImpl with calculated pagination metadata
     */
    public static <E> PageableResultImpl<E> content(List<E> content, Pageable pageable, long totalElements) {
        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("pageable cannot be null");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must be >= 0");
        }

        int pageSize = pageable.getPageSize();
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;

        return new PageableResultImpl<>(
            content,
            pageable.getPageNumber(),
            pageSize,
            totalElements,
            totalPages
        );
    }
}
