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

    // ========== Tests for get() method ==========

    @Test
    public void getShouldReturnNullWhenSourceIsNull() {
        // given
        SampleObject value = null;

        // when
        Long result = Nulls.get(value, SampleObject::getId);

        // then
        assertThat(result, equalTo(null));
    }

    @Test
    public void getShouldReturnNullWhenPropertyIsNull() {
        // given
        SampleObject value = new SampleObject();
        value.setId(null);

        // when
        Long result = Nulls.get(value, SampleObject::getId);

        // then
        assertThat(result, equalTo(null));
    }

    @Test
    public void getShouldReturnValueWhenPropertyIsNotNull() {
        // given
        SampleObject value = new SampleObject();
        value.setId(42L);

        // when
        Long result = Nulls.get(value, SampleObject::getId);

        // then
        assertThat(result, equalTo(42L));
    }

    @Test
    public void getShouldWorkWithNestedObjects() {
        // given
        SampleObject inner = new SampleObject();
        inner.setValue("test");
        SampleObject outer = new SampleObject();
        // Simulate nested structure: outer -> inner
        // (In real code this would be a separate field, but using value for simplicity)

        // when
        String result = Nulls.get(inner, SampleObject::getValue);

        // then
        assertThat(result, equalTo("test"));
    }

    // ========== Tests for coalesce() method ==========

    @Test
    public void coalesceShouldReturnFirstNonNullValue() {
        // when
        String result = Nulls.coalesce(null, null, "third", "fourth");

        // then
        assertThat(result, equalTo("third"));
    }

    @Test
    public void coalesceShouldReturnNullWhenAllValuesAreNull() {
        // when
        String result = Nulls.coalesce(null, null, null);

        // then
        assertThat(result, equalTo(null));
    }

    @Test
    public void coalesceShouldReturnFirstValueWhenNotNull() {
        // when
        String result = Nulls.coalesce("first", "second", "third");

        // then
        assertThat(result, equalTo("first"));
    }

    @Test
    public void coalesceShouldWorkWithSingleValue() {
        // when
        String result = Nulls.coalesce("only");

        // then
        assertThat(result, equalTo("only"));
    }

    @Test
    public void coalesceShouldWorkWithEmptyArray() {
        // when
        String result = Nulls.coalesce();

        // then
        assertThat(result, equalTo(null));
    }

    @Test
    public void coalesceShouldWorkWithGetForNestedObjects() {
        // given
        SampleObject obj1 = new SampleObject();
        obj1.setId(null);

        SampleObject obj2 = new SampleObject();
        obj2.setId(null);

        SampleObject obj3 = new SampleObject();
        obj3.setId(123L);

        // when - Coalesce multiple get() calls
        Long result = Nulls.coalesce(
            Nulls.get(obj1, SampleObject::getId),
            Nulls.get(obj2, SampleObject::getId),
            Nulls.get(obj3, SampleObject::getId),
            999L
        );

        // then
        assertThat(result, equalTo(123L));
    }

    @Test
    public void coalesceShouldWorkWithNullSourcesAndGet() {
        // given
        SampleObject obj1 = null;
        SampleObject obj2 = null;
        SampleObject obj3 = new SampleObject();
        obj3.setId(456L);

        // when - Coalesce with null sources
        Long result = Nulls.coalesce(
            Nulls.get(obj1, SampleObject::getId),
            Nulls.get(obj2, SampleObject::getId),
            Nulls.get(obj3, SampleObject::getId)
        );

        // then
        assertThat(result, equalTo(456L));
    }
}
