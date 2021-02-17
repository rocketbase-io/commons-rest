package io.rocketbase.commons.validation;

import io.rocketbase.commons.dto.address.ContactDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

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
    }
}