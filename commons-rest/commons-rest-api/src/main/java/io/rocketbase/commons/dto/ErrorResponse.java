package io.rocketbase.commons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ErrorResponse {

    private Integer status;
    private String message;

    @Singular
    private Map<String, String> fields;

    public ErrorResponse() {
    }

    public ErrorResponse(String errorMessage) {
        this.message = errorMessage;
    }

    public ErrorResponse(Integer status, String message) {
        this.status = status;
        this.message = message;
    }
}
