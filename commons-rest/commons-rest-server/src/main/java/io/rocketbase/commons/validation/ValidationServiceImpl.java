package io.rocketbase.commons.validation;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * JSR303, BeanValidation compliant implementation of {@link io.rocketbase.commons.validation.ValidationService}.
 * <p>
 * This implementation relies on BeanValidation standard but is Validation framework agnostic.
 * <p>
 * This service is defined under the "resthub-validation" Spring profile and can be activated by adding this
 * profile to your context.
 *
 * @see io.rocketbase.commons.validation.ValidationService
 */
public class ValidationServiceImpl implements ValidationService {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelConstraint getConstraintsForClassName(String canonicalClassName) throws ClassNotFoundException {
        return this.getConstraintsForClassName(canonicalClassName, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelConstraint getConstraintsForClassName(String canonicalClassName, Locale locale) throws ClassNotFoundException {
        return this.getConstraintsForClass(Class.forName(canonicalClassName), locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelConstraint getConstraintsForClass(Class<?> clazz) {
        return this.getConstraintsForClass(clazz, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelConstraint getConstraintsForClass(Class<?> clazz, Locale locale) {
        ModelConstraint modelConstraint = new ModelConstraint(clazz.getCanonicalName());
        BeanDescriptor bd = VALIDATOR.getConstraintsForClass(clazz);

        if (bd.isBeanConstrained() && !Modifier.isAbstract(clazz.getModifiers())) {
            modelConstraint.setConstraints(this.getConstraints(bd, locale));
        }

        return modelConstraint;
    }

    /**
     * Build a complete map of object property / list of {@link io.rocketbase.commons.validation.ValidationConstraint}
     * from a given {@link BeanDescriptor} <tt>bd</tt> instance and a given {@link Locale}
     * <tt>locale</tt>
     */
    private Map<String, List<ValidationConstraint>> getConstraints(BeanDescriptor bd, Locale locale) {
        Map<String, List<ValidationConstraint>> constraints = new HashMap<String, List<ValidationConstraint>>();

        for (PropertyDescriptor pd : bd.getConstrainedProperties()) {
            // if property has defined constraints directly or delegates validation through cascading option
            if ((pd.getPropertyName() != null) && (pd.hasConstraints() || pd.isCascaded())) {
                constraints.put(pd.getPropertyName(), this.getValidationConstraints(pd, locale));
            }
        }

        return constraints;
    }

    /**
     * Build a list of {@link io.rocketbase.commons.validation.ValidationConstraint} associated to a given
     * {@link PropertyDescriptor} <tt>pd</tt> instance and a given {@link Locale}
     * <tt>locale</tt>
     */
    private List<ValidationConstraint> getValidationConstraints(PropertyDescriptor pd, Locale locale) {
        List<ValidationConstraint> validationConstraints = new ArrayList<ValidationConstraint>();

        // copy any directly defined constraint into wrapper
        for (ConstraintDescriptor cd : pd.getConstraintDescriptors()) {
            ValidationConstraint validationConstraint = new ValidationConstraint();

            validationConstraint.setType(this.getType(cd));
            validationConstraint.setMessage(this.getMessage(cd, locale));
            validationConstraint.setAttributes(this.getAttributes(cd));

            validationConstraints.add(validationConstraint);
        }

        // manage cascading option by adding a custom "Valid" type and referencing underling model class name
        if (pd.isCascaded()) {
            ValidationConstraint validationConstraint = new ValidationConstraint();
            validationConstraint.setType("Valid");
            validationConstraint.addAttribute("model", pd.getElementClass().getCanonicalName());
            validationConstraints.add(validationConstraint);
        }

        return validationConstraints;
    }

    private String getType(ConstraintDescriptor cd) {
        String type = cd.getAnnotation().annotationType().toString();
        return type.substring(type.lastIndexOf('.') + 1, type.length());
    }

    /**
     * Resolves message for a {@link ConstraintDescriptor} <tt>cd</tt> against the given
     * {@link Locale} <tt>locale</tt>.
     * <p>
     * If <tt>locale</tt> is null, returns the default message depending on the underlying validation framework.
     * <p>
     * This methods resolves also messages parameters through message interpolation.
     */
    private String getMessage(ConstraintDescriptor cd, Locale locale) {
        String msgKey = cd.getAttributes().get("message").toString();

        String msg;
        ValidationContext validationContext = new ValidationContext(cd, null);

        if (null == locale) {
            msg = FACTORY.getMessageInterpolator().interpolate(msgKey, validationContext);
        } else {
            msg = FACTORY.getMessageInterpolator().interpolate(msgKey, validationContext, locale);
        }

        return msg.replaceAll("[{}]", "");
    }

    /**
     * Retrieves complementary constraint attributes from a given {@link ConstraintDescriptor}
     * <tt>cd</tt>
     */
    private Map<String, Object> getAttributes(ConstraintDescriptor cd) {
        Map<String, Object> attributes = new HashMap<String, Object>(cd.getAttributes());

        attributes.remove("payload");
        attributes.remove("groups");
        attributes.remove("message");

        return attributes;
    }
}
