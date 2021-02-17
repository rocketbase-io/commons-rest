package io.rocketbase.commons.dto.validation;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Object representation of validation constraints.
 *
 * <b>Sample JSON serialization:</b>
 * <pre>
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
 * </pre>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"type", "message"})
public class ValidationConstraint {

    /**
     * Constraint type
     */
    private String type;

    /**
     * Associated constraint message
     */
    private String message;

    /**
     * Optional constraints attributes
     */
    @Nullable
    private Map<String, Object> attributes;
    
    @JsonAnyGetter
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @JsonAnySetter
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Add attribute to the current constraint definition.
     *
     * @param attrKey   attribute key identifier
     * @param attribute attribute value
     * @return the previous value associated with <tt>attrKey</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>attrKey</tt>.
     */
    public Object addAttribute(String attrKey, Object attribute) {

        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }

        return this.attributes.put(attrKey, attribute);
    }
}
