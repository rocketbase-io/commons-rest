package io.rocketbase.commons.generator;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Controls how the commons-rest Zod schema generator treats a type or field.
 *
 * <p>By default the generator discovers schemas transitively from {@code @MutationHook}
 * request-body types and the object graph reachable through their fields. This annotation
 * lets a project step outside that default in three ways, selected via {@link #value()}:
 *
 * <ul>
 *   <li>{@link Mode#INCLUDE} (on a <b>type</b>) — treat the type as an additional generation
 *       root even when no mutation endpoint references it. Useful for payloads validated in
 *       the frontend outside the generated react-query mutation flow (CSV import rows,
 *       WebSocket messages, manually constructed forms).</li>
 *   <li>{@link Mode#IGNORE} (on a <b>type</b> or <b>field</b>) — exclude it from schema
 *       generation. An ignored type referenced by a field degrades to {@code z.any()}; an
 *       ignored field is omitted from its parent schema entirely. The escape hatch for
 *       legacy/cyclic/opaque shapes you don't want to model in Zod.</li>
 *   <li>{@link Mode#ANY} (on a <b>field</b>) — keep the field in the schema but emit
 *       {@code z.any()} for it, sidestepping any mapping complexity for that one property
 *       while still validating the rest of the object.</li>
 * </ul>
 *
 * <p>Lives in {@code commons-rest-api} (next to {@link MutationHook}/{@link QueryHook}) so
 * consumer projects can annotate their DTOs without depending on the openapi generator
 * module.
 */
@Target({TYPE, FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ZodSchema {

    /**
     * How the generator should treat the annotated element. Defaults to {@link Mode#INCLUDE},
     * so the common case — {@code @ZodSchema} on a type to force its generation — needs no
     * argument.
     */
    Mode value() default Mode.INCLUDE;

    enum Mode {
        /** Type-level: add as an extra generation root. */
        INCLUDE,
        /** Type- or field-level: exclude from generation. */
        IGNORE,
        /** Field-level: keep the field but emit {@code z.any()}. */
        ANY
    }
}
