package io.rocketbase.commons.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

@Data
public class PageableResult<E> implements Iterable<E>, Serializable {

    private long totalElements;

    private int totalPages;

    private int page;

    private int pageSize;

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


}
