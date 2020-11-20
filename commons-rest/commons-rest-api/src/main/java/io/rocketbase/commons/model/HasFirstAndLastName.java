package io.rocketbase.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Transient;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * entity/dto has firstName + lastName capability
 */
public interface HasFirstAndLastName {

    @Nullable
    String getFirstName();

    @Nullable
    String getLastName();

    /**
     * combines first + last name<br>
     * will return null in case firstName & lastName is empty
     */
    @Transient
    @JsonIgnore
    default String getFullName() {
        boolean emptyFirstName = StringUtils.isEmpty(getFirstName());
        boolean emptyLastName = StringUtils.isEmpty(getLastName());
        if (emptyFirstName && emptyLastName) {
            return null;
        } else if (!emptyFirstName && !emptyLastName) {
            return String.format("%s %s", getFirstName(), getLastName());
        } else if (!emptyFirstName) {
            return getFirstName();
        } else {
            return getLastName();
        }
    }
}
