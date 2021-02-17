package io.rocketbase.commons.validation;

import io.rocketbase.commons.dto.address.ContactDto;
import io.rocketbase.commons.dto.validation.ModelConstraint;
import io.rocketbase.commons.dto.validation.ValidationConstraint;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
class ValidationServiceTest {

    @Test
    void getConstraintsForClassName() {
        // given
        ValidationServiceImpl validationService = new ValidationServiceImpl();
        // when
        ModelConstraint result = validationService.getConstraintsForClass(ContactDto.class, Locale.GERMAN);
        // then
        assertThat(result, notNullValue());
        assertThat(result.getModel(), equalTo("io.rocketbase.commons.dto.address.ContactDto"));
        assertThat(result.getConstraints().keySet(), containsInAnyOrder("firstName", "lastName", "cellphone", "salutation", "landline", "title", "email"));
        assertThat(result.getConstraints().get("firstName"), containsInAnyOrder(ValidationConstraint.builder()
                .type("Size")
                .message("Größe muss zwischen 0 und 100 sein")
                .build()
        ));
        assertThat(result.getConstraints().get("email"), containsInAnyOrder(ValidationConstraint.builder()
                        .type("Email")
                        .message("muss eine korrekt formatierte E-Mail-Adresse sein")
                        .build(),
                ValidationConstraint.builder()
                        .type("Size")
                        .message("Größe muss zwischen 0 und 255 sein")
                        .build()
        ));
    }
}