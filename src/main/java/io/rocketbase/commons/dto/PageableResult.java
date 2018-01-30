package io.rocketbase.commons.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @_(@JsonCreator))
public class PageableResult<E> implements Iterable<E>, Serializable {

    private long totalElements;

    private int totalPages;

    private int page;

    private int pageSize;

    private List<E> content;

    public static <E> PageableResult<E> contentPage(@NotNull List<E> content, @NotNull Page page) {
        return PageableResult.<E>builder()
                .content(content)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .page(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    public static <E> PageableResult<E> page(@NotNull Page<E> page) {
        return PageableResult.<E>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .page(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    @Override
    public Iterator<E> iterator() {
        return content.iterator();
    }


}
