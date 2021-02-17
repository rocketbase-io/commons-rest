package io.rocketbase.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * wrapping object for paged result lists
 */
@Data
public class PageableResult<E> implements Iterable<E>, Serializable {

    /**
     * total count of values in database
     */
    private long totalElements;

    /**
     * count of pages in total with given pageSize
     */
    private int totalPages;

    /**
     * current page (starts by 0)
     */
    private int page;

    /**
     * maximum size of content list
     */
    private int pageSize;

    /**
     * content of current page. count of elements is less or equals pageSize (depends on totalElements and page/pageSize)
     */
    private List<E> content;

    public static <E> PageableResult<E> contentPage(List<E> content, Page page) {
        PageableResult result = new PageableResult();
        result.setContent(content);
        result.setTotalPages(page.getTotalPages());
        result.setTotalElements(page.getTotalElements());
        result.setPage(page.getNumber());
        result.setPageSize(page.getSize());
        return result;
    }

    public static <E> PageableResult<E> page(Page<E> page) {
        PageableResult result = new PageableResult();
        result.setContent(page.getContent());
        result.setTotalPages(page.getTotalPages());
        result.setTotalElements(page.getTotalElements());
        result.setPage(page.getNumber());
        result.setPageSize(page.getSize());
        return result;
    }

    public static <E> PageableResult<E> content(List<E> content) {
        PageableResult result = new PageableResult();
        result.setContent(content);
        result.setTotalPages(1);
        int totalElements = content != null ? content.size() : 0;
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
    public Page<E> toPage() {
        return new PageImpl<>(getContent(), PageRequest.of(page, pageSize), totalElements);
    }


}
