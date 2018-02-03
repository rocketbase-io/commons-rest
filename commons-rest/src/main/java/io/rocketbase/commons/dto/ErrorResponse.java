package io.rocketbase.commons.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ErrorResponse {

    private int status;
    private String message;

    private Map<String, String> fields;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public ErrorResponse(String errorMessage) {
        this.message = message;
    }
}
