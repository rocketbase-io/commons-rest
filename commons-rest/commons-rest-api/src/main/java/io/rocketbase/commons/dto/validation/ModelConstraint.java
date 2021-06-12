package io.rocketbase.commons.dto.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object representation of validation constraints defined on a model domain object
 *
 * <b>Sample JSON serialization:</b>
 * <pre>
 * {
 *   "model": "io.rocketbase.commons.model.User",
 *      "constraints": {
 *          "lastName": [{
 *              "type": "NotBlank",
 *              "message": "may not be empty"
 *          }],
 *          "email": [{
 *              "type": "NotNull",
 *              "message": "may not be null"
 *           }, {
 *              "type": "Email",
 *              "message": "not a well-formed email address",
 *              "flags": [],
 *              "regexp": ".*"
 *          }],
 *          "login": [{
 *              "type": "NotNull",
 *              "message": "may not be null"
 *          }, {
 *              "type": "Length",
 *              "message": "length must be between 8 and 2147483647",
 *              "min": 8,
 *              "max": 2147483647
 *          }],
 *          "firstName": [{
 *              "type": "NotBlank",
 *              "message": "may not be empty"
 *          }]
 *      }
 * }
 * </pre>
 *
 * @see ValidationConstraint
 */
@Data
@EqualsAndHashCode(of = "model")
@JsonPropertyOrder(value = {"model", "constraints"})
@Schema(example = "{\n" +
        "  \"model\": \"io.rocketbase.commons.model.User\",\n" +
        "     \"constraints\": {\n" +
        "         \"lastName\": [{\n" +
        "             \"type\": \"NotBlank\",\n" +
        "             \"message\": \"may not be empty\"\n" +
        "         }],\n" +
        "         \"email\": [{\n" +
        "             \"type\": \"NotNull\",\n" +
        "             \"message\": \"may not be null\"\n" +
        "          }, {\n" +
        "             \"type\": \"Email\",\n" +
        "             \"message\": \"not a well-formed email address\",\n" +
        "             \"flags\": [],\n" +
        "             \"regexp\": \".*\"\n" +
        "         }],\n" +
        "         \"login\": [{\n" +
        "             \"type\": \"NotNull\",\n" +
        "             \"message\": \"may not be null\"\n" +
        "         }, {\n" +
        "             \"type\": \"Length\",\n" +
        "             \"message\": \"length must be between 8 and 2147483647\",\n" +
        "             \"min\": 8,\n" +
        "             \"max\": 2147483647\n" +
        "         }],\n" +
        "         \"firstName\": [{\n" +
        "             \"type\": \"NotBlank\",\n" +
        "             \"message\": \"may not be empty\"\n" +
        "         }]\n" +
        "     }\n" +
        "}")
public class ModelConstraint {

    /**
     * Model class name
     */
    @Schema(description = "Model class name", example = "io.rocketbase.commons.model.User")
    private String model;

    /**
     * Map of all validation constraints on each model property
     *
     * @see ValidationConstraint
     */
    @Schema(description = "Map of all validation constraints on each model property")
    private Map<String, List<ValidationConstraint>> constraints;

    @JsonCreator
    public ModelConstraint(@JsonProperty("model") String modelRef) {
        this.model = modelRef;
        this.constraints = new HashMap<>();
    }

    /**
     * Add a {@link ValidationConstraint} for a given property for the current represented model
     *
     * @param property   model property holding the constraint
     * @param constraint {@link ValidationConstraint} instance to add to model constraints definitions
     * @return the updated list of {@link ValidationConstraint} for the given <tt>property</tt> parameter.
     */
    public List<ValidationConstraint> addConstraint(String property, ValidationConstraint constraint) {

        if (null == this.constraints) {
            this.constraints = new HashMap<>();
        }

        List<ValidationConstraint> propertyConstraints = this.constraints.get(property);
        if (null == propertyConstraints) {
            propertyConstraints = new ArrayList<>();
        }
        propertyConstraints.add(constraint);

        return this.constraints.put(property, propertyConstraints);
    }
}
