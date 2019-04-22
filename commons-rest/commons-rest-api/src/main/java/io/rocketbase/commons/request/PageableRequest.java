package io.rocketbase.commons.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.rocketbase.commons.util.Nulls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * please use {@link org.springframework.data.domain.Pageable}
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Deprecated
public class PageableRequest {

    private Integer page;
    private Integer pageSize;
    private Sort sort;

    @JsonIgnore
    public PageRequest toPageRequest() {
        return PageRequest.of(Nulls.notNull(getPage(), 0), Nulls.notNull(getPageSize(), 25), getSort());
    }

}
