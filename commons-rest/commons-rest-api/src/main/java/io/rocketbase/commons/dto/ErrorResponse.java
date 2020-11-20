package io.rocketbase.commons.dto;

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
public class ErrorResponse {

    /**
     * http status code
     */
    private Integer status;

    /**
     * user readable error explanation
     */
    private String message;

    /**
     * in case of form validations details related to properties
     */
    @Singular
    @Nullable
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
