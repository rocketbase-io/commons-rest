package io.rocketbase.commons.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * wrapped response in case of errors - follows RFC 9457 (problem details for http apis)
 */
@Data
@SuperBuilder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "problem details in case of errors (RFC 9457)")
public class ErrorResponse {

    /**
     * URI reference that identifies the problem type - when not set clients should assume about:blank
     */
    @Nullable
    @Schema(description = "URI reference that identifies the problem type", example = "urn:problem-type:form-error")
    private String type;

    /**
     * short, human-readable summary of the problem type
     */
    @Nullable
    @Schema(description = "short, human-readable summary of the problem type", example = "Bad Request")
    private String title;

    /**
     * http status code
     */
    @Schema(description = "http status code", example = "400")
    private Integer status;

    /**
     * human-readable explanation specific to this occurrence of the problem
     */
    @JsonAlias("message")
    @Schema(description = "human-readable explanation specific to this occurrence of the problem", example = "bean validation exception")
    private String detail;

    /**
     * URI reference that identifies the specific occurrence of the problem
     */
    @Nullable
    @Schema(description = "URI reference that identifies the specific occurrence of the problem", example = "/api/employee/4711")
    private String instance;

    /**
     * in case of form validations details related to properties. key is the filed value list of related errors<br>
     * extension member as allowed by RFC 9457
     */
    @Singular
    @Nullable
    @Schema(description = "in case of form validations details related to properties. key is the filed value list of related errors", example = "{\"status\": [\"not empty\"]}")
    private Map<String, List<String>> fields;

    public ErrorResponse() {
    }

    public ErrorResponse(String detail) {
        this.detail = detail;
    }

    public ErrorResponse(Integer status, String detail) {
        this.status = status;
        this.detail = detail;
    }

    /**
     * bridge for the pre RFC 9457 format
     *
     * @deprecated use {@link #getDetail()}
     */
    @Deprecated
    @JsonIgnore
    public String getMessage() {
        return detail;
    }

    /**
     * bridge for the pre RFC 9457 format
     *
     * @deprecated use {@link #setDetail(String)}
     */
    @Deprecated
    public void setMessage(String message) {
        this.detail = message;
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
