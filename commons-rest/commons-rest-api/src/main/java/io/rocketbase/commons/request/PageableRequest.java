package io.rocketbase.commons.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PageableRequest {

    private Integer page;
    private Integer pageSize;
    private Sort sort;

}
