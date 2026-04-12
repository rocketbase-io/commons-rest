package io.rocketbase.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Interface for paginated result lists.
 * <p>
 * This interface provides a clean, serializable contract for REST API pagination responses,
 * decoupled from Spring Data's internal Page implementation.
 * </p>
 *
 * @param <T> the type of elements in the page content
 */
@Schema(description = "Interface for paginated result lists")
@JsonDeserialize(as = PageableResultImpl.class)
public interface PageableResult<T> extends Iterable<T>, Serializable {

    /**
     * @return the page content as a list
     */
    @Schema(description = "content of current page. count of elements is less or equals pageSize (depends on totalElements and page/pageSize)")
    @JsonProperty("content")
    List<T> content();

    /**
     * @return the current page number (0-indexed)
     */
    @Schema(description = "current page (starts by 0)")
    @JsonProperty("page")
    int page();

    /**
     * @return the size of the page
     */
    @Schema(description = "maximum size of content list")
    @JsonProperty("pageSize")
    int pageSize();

    /**
     * @return the total number of elements across all pages
     */
    @Schema(description = "total count of values in database")
    @JsonProperty("totalElements")
    long totalElements();

    /**
     * @return the total number of pages
     */
    @Schema(description = "count of pages in total with given pageSize")
    @JsonProperty("totalPages")
    int totalPages();

    // ==================== Default Methods ====================

    /**
     * Returns whether there is a next page.
     *
     * @return true if there is a next page, false otherwise
     */
    @JsonIgnore
    default boolean hasNextPage() {
        return page() < (totalPages() - 1);
    }

    /**
     * Returns whether there is a previous page.
     *
     * @return true if there is a previous page, false otherwise
     */
    @JsonIgnore
    default boolean hasPreviousPage() {
        return page() > 0;
    }

    /**
     * Returns an iterator over the content.
     *
     * @return iterator over page content
     */
    @Override
    default Iterator<T> iterator() {
        return content().iterator();
    }

    /**
     * Converts this PageableResult to a Spring Data Page.
     *
     * @return Spring Data Page representation
     */
    @JsonIgnore
    default Page<T> toPage() {
        return new PageImpl<>(content(), PageRequest.of(page(), pageSize()), totalElements());
    }

    /**
     * Returns a new PageableResult with the content mapped by the given function.
     *
     * @param <U>       the type of elements in the mapped page
     * @param converter the function to map content elements
     * @return a new PageableResult with mapped content
     */
    default <U> PageableResult<U> map(Function<? super T, ? extends U> converter) {
        Assert.notNull(converter, "Converter must not be null!");

        List<U> mappedContent = content().stream()
                .map(converter)
                .collect(Collectors.toList());

        return new PageableResultImpl<>(
                mappedContent,
                page(),
                pageSize(),
                totalElements(),
                totalPages()
        );
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a PageableResult from a Spring Data Page with custom content.
     * Useful when you need to convert entities to DTOs.
     *
     * @param <T>     the type of elements in the result
     * @param <S>     the type of elements in the source page
     * @param content the converted content list
     * @param page    the source Spring Data Page
     * @return a new PageableResult
     */
    static <T, S> PageableResult<T> contentPage(List<T> content, Page<S> page) {
        Assert.notNull(page, "page is null - not allowed");

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
     *
     * @param <T>       the type of elements in the result
     * @param <S>       the type of elements in the source page
     * @param page      the source Spring Data Page
     * @param converter function to convert page elements
     * @return a new PageableResult
     */
    static <T, S> PageableResult<T> fromPage(Page<S> page, Function<S, T> converter) {
        Assert.notNull(page, "page is null - not allowed");
        Assert.notNull(converter, "converter not defined");

        List<T> content = page.getContent().stream()
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
     * @param <T>  the type of elements
     * @param page the source Spring Data Page
     * @return a new PageableResult
     */
    static <T> PageableResult<T> fromPage(Page<T> page) {
        Assert.notNull(page, "page is null - not allowed");

        return new PageableResultImpl<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /**
     * Creates a PageableResult from a simple content list.
     * Treats the entire list as a single page.
     *
     * @param <T>     the type of elements
     * @param content the content list
     * @return a new PageableResult
     */
    static <T> PageableResult<T> of(List<T> content) {
        Assert.notNull(content, "content is null - not allowed");

        int size = content.size();
        return new PageableResultImpl<>(
                content,
                0,
                size,
                size,
                1
        );
    }

    /**
     * Creates an empty PageableResult.
     *
     * @param <T> the type of elements
     * @return an empty PageableResult
     */
    static <T> PageableResult<T> empty() {
        return new PageableResultImpl<>(
                Collections.emptyList(),
                0,
                0,
                0,
                0
        );
    }
}
