package io.rocketbase.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.core.convert.converter.Converter;
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
 * wrapping object for paged result lists
 */
@Data
@Schema(description = "wrapping object for paged result lists")
public class PageableResult<E> implements Iterable<E>, Serializable {

    /**
     * total count of values in database
     */
    @Schema(description = "total count of values in database")
    private long totalElements;

    /**
     * count of pages in total with given pageSize
     */
    @Schema(description = "count of pages in total with given pageSize")
    private int totalPages;

    /**
     * current page (starts by 0)
     */
    @Schema(description = "current page (starts by 0)")
    private int page;

    /**
     * maximum size of content list
     */
    @Schema(description = "maximum size of content list")
    private int pageSize;

    /**
     * content of current page. count of elements is less or equals pageSize (depends on totalElements and page/pageSize)
     */
    @Schema(description = "content of current page. count of elements is less or equals pageSize (depends on totalElements and page/pageSize)")
    private List<E> content;

    public static <T, E> PageableResult<E> contentPage(List<E> content, Page<T> page) {
        Assert.notNull(page, "page is null - not allowed");

        PageableResult<E> result = new PageableResult<>();
        result.setContent(content != null ? content : Collections.emptyList());
        result.setTotalPages(page.getTotalPages());
        result.setTotalElements(page.getTotalElements());
        result.setPage(page.getNumber());
        result.setPageSize(page.getSize());
        return result;
    }

    public static <T, E> PageableResult<E> page(Page<T> page, Function<T, E> converter) {
        Assert.notNull(page, "page is null - not allowed");
        Assert.notNull(converter, "converter not defined");

        PageableResult<E> result = new PageableResult<>();
        result.setContent(page.getContent().stream().map(converter).collect(Collectors.toList()));
        result.setTotalPages(page.getTotalPages());
        result.setTotalElements(page.getTotalElements());
        result.setPage(page.getNumber());
        result.setPageSize(page.getSize());
        return result;
    }

    public static <E> PageableResult<E> page(Page<E> page) {
        Assert.notNull(page, "page is null - not allowed");

        PageableResult<E> result = new PageableResult<>();
        result.setContent(page.getContent());
        result.setTotalPages(page.getTotalPages());
        result.setTotalElements(page.getTotalElements());
        result.setPage(page.getNumber());
        result.setPageSize(page.getSize());
        return result;
    }

    public static <E> PageableResult<E> content(List<E> content) {
        Assert.notNull(content, "content is null - not allowed");

        PageableResult<E> result = new PageableResult<>();
        result.setContent(content);
        result.setTotalPages(1);
        int totalElements = content.size();
        result.setTotalElements(totalElements);
        result.setPage(0);
        result.setPageSize(totalElements);
        return result;
    }

    @Override
    public Iterator<E> iterator() {
        return content.iterator();
    }

    @JsonIgnore
    public boolean hasNextPage() {
        return page < (totalPages - 1);
    }

    @JsonIgnore
    public boolean hasPreviousPage() {
        return page > 0;
    }

    @JsonIgnore
    public Page<E> toPage() {
        return new PageImpl<>(getContent(), PageRequest.of(page, pageSize), totalElements);
    }

    /**
     * Returns a new {@link PageableResult} with the content of the current one mapped by the given {@link Converter}.
     *
     * @param converter must not be {@literal null}.
     */
    public <U> PageableResult<U> map(Function<? super E, ? extends U> converter) {
        Assert.notNull(converter, "Function must not be null!");

        PageableResult<U> result = new PageableResult<>();
        result.setContent(getContent().stream().map(converter::apply).collect(Collectors.toList()));
        result.setTotalPages(getTotalPages());
        result.setTotalElements(getTotalElements());
        result.setPage(getPage());
        result.setPageSize(getPageSize());
        return result;
    }

}
