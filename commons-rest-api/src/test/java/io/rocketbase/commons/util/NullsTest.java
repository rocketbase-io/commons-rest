package io.rocketbase.commons.util;

import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class NullsTest {

    @Test
    public void simpleNotEmpty() {
        // given
        String value = null;

        // when
        String result = Nulls.notEmpty(value, "-");
        // then
        assertThat(result, notNullValue());
        assertThat(result, equalTo("-"));
    }

    @Data
    public static class SampleObject {
        private Long id;
        private String value;
    }

    @Test
    public void propertyProviderNullObject() {
        // given
        SampleObject value = null;

        // when
        String result = Nulls.notEmpty(value, SampleObject::getValue, "-");
        // then
        assertThat(result, notNullValue());
        assertThat(result, equalTo("-"));
    }

    @Test
    public void propertyProviderNullProperty() {
        // given
        SampleObject value = new SampleObject();

        // when
        String result = Nulls.notEmpty(value, SampleObject::getValue, "-");
        // then
        assertThat(result, notNullValue());
        assertThat(result, equalTo("-"));
    }

    @Test
    public void propertyProviderEmptyProperty() {
        // given
        SampleObject value = new SampleObject();
        value.setValue(" ");

        // when
        String result = Nulls.notEmpty(value, SampleObject::getValue, "-");
        // then
        assertThat(result, notNullValue());
        assertThat(result, equalTo("-"));
    }

    @Test
    public void propertyProviderNotNullProperty() {
        // given
        SampleObject value = new SampleObject();

        // when
        Long result = Nulls.notNull(value, SampleObject::getId, 100L);
        // then
        assertThat(result, notNullValue());
        assertThat(result, equalTo(100L));
    }
}
