package io.rocketbase.commons.validation;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.ValidationException;
import jakarta.validation.metadata.ConstraintDescriptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Specific interpolation context management for resthub validation utilities
 * <p>
 * Allows to manage messages and interpolations related to a {@link ConstraintDescriptor}
 * and a value. Managing the value allows to resolve dynamic message interpolation for error messages
 * containing parameters
 */
@RequiredArgsConstructor
@Getter
public class ValidationContext implements MessageInterpolator.Context {

    /**
     * Managed constraint descriptor
     */
    private final ConstraintDescriptor<?> constraintDescriptor;

    /**
     * Managed object value
     */
    private final Object validatedValue;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T unwrap(Class<T> type) {
        if (type.isAssignableFrom(ValidationContext.class)) {
            return type.cast(this);
        }
        throw new ValidationException(String.format("Type %s not supported for unwrapping", type));
    }

}
