package io.rocketbase.commons.model;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.beans.Transient;

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
    @Nullable
    default String getFullName() {
        boolean emptyFirstName = !StringUtils.hasText(getFirstName());
        boolean emptyLastName = !StringUtils.hasText(getLastName());
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
