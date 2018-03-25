package io.rocketbase.commons.dto;

import lombok.*;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private Integer status;
    private String message;

    @Singular
    private Map<String, String> fields;

    public ErrorResponse(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public ErrorResponse(String errorMessage) {
        this.message = message;
    }
}
