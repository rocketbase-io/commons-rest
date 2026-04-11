package io.rocketbase.commons.openapi.sample.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic command for testing generic type handling.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericWrapperCmd<T> {

    @NotNull
    private String id;

    @NotNull
    private T data;

    private List<T> items;

    private Integer version;
}
