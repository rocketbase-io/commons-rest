package io.rocketbase.commons.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @_(@JsonCreator))
@Builder
public class ErrorResponse {

    private int errorCode;
    private String errorMessage;

    @Singular
    private Map<String, String> fields;

    public ErrorResponse(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
