package io.rocketbase.commons.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @_(@JsonCreator))
@Builder
public class ErrorResponse {

    private int status;
    private String message;

    @Singular
    private Map<String, String> fields;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public ErrorResponse(String errorMessage) {
        this.message = message;
    }
}
