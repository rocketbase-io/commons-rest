package io.rocketbase.commons.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * wrapped response in case of errors
 */
@Data
@Builder
@AllArgsConstructor
@Schema(description = "wrapped response in case of errors")
public class ErrorResponse {

    /**
     * http status code
     */
    @Schema(description = "http status code", example = "400")
    private Integer status;

    /**
     * user readable error explanation
     */
    @Schema(description = "user readable error explanation", example = "bean validation exception")
    private String message;

    /**
     * in case of form validations details related to properties. key is the filed value list of related errors
     */
    @Singular
    @Nullable
    @Schema(description = "in case of form validations details related to properties. key is the filed value list of related errors", example = "[\"status\": [\"not empty\"]]")
    private Map<String, List<String>> fields;

    public ErrorResponse() {
    }

    public ErrorResponse(String errorMessage) {
        this.message = errorMessage;
    }

    public ErrorResponse(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * check if path already exists, add message to list or create new one
     */
    public ErrorResponse addField(String path, String message) {
        if (fields == null) {
            fields = new HashMap<>();
        }
        if (!fields.containsKey(path)) {
            fields.put(path, new ArrayList<>());
        }
        fields.get(path).add(message);
        return this;
    }

    /**
     * check if ErrorResponse has field info
     */
    public boolean hasField(String path) {
        return fields != null && fields.containsKey(path);
    }

    /**
     * check within fields and get first value<br>
     * return null when not found or empty
     */
    public String getFirstFieldValue(String path) {
        return hasField(path) && !fields.get(path).isEmpty() ? fields.get(path).get(0) : null;
    }

    /*
     * check within fields and get values<br>
     * return null when path not found
     */
    public List<String> getFieldValue(String path) {
        return hasField(path) ? fields.get(path) : null;
    }
}
