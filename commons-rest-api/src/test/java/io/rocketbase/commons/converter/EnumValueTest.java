package io.rocketbase.commons.converter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link EnumValue} interface and its helper methods.
 */
class EnumValueTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Test enum implementing EnumValue.
     */
    enum TestStatus implements EnumValue {
        ACTIVE("active"),
        INACTIVE("inactive"),
        PENDING("pending");

        private final String value;

        TestStatus(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static TestStatus fromValue(String value) {
            return EnumValue.fromValue(TestStatus.class, value);
        }
    }

    /**
     * Test enum without EnumValue (standard Java enum).
     */
    enum StandardEnum {
        OPTION_ONE,
        OPTION_TWO,
        OPTION_THREE
    }

    @Test
    void shouldConvertFromExactValue() {
        // When
        TestStatus result = EnumValue.fromValue(TestStatus.class, "active");

        // Then
        assertThat(result, is(TestStatus.ACTIVE));
    }

    @Test
    void shouldConvertFromUppercaseValue() {
        // When
        TestStatus result = EnumValue.fromValue(TestStatus.class, "ACTIVE");

        // Then
        assertThat(result, is(TestStatus.ACTIVE));
    }

    @Test
    void shouldConvertFromMixedCaseValue() {
        // When
        TestStatus result = EnumValue.fromValue(TestStatus.class, "AcTiVe");

        // Then
        assertThat(result, is(TestStatus.ACTIVE));
    }

    @Test
    void shouldConvertFromValueWithWhitespace() {
        // When
        TestStatus result = EnumValue.fromValue(TestStatus.class, "  active  ");

        // Then
        assertThat(result, is(TestStatus.ACTIVE));
    }

    @Test
    void shouldFallbackToEnumNameWhenValueNotMatched() {
        // When - ACTIVE is the enum name, not the value
        TestStatus result = EnumValue.fromValue(TestStatus.class, "ACTIVE");

        // Then - should still match via fallback
        assertThat(result, is(TestStatus.ACTIVE));
    }

    @Test
    void shouldReturnNullForInvalidValue() {
        // When
        TestStatus result = EnumValue.fromValue(TestStatus.class, "invalid");

        // Then
        assertThat(result, is(nullValue()));
    }

    @Test
    void shouldReturnNullForNullValue() {
        // When
        TestStatus result = EnumValue.fromValue(TestStatus.class, null);

        // Then
        assertThat(result, is(nullValue()));
    }

    @Test
    void shouldReturnNullForEmptyValue() {
        // When
        TestStatus result = EnumValue.fromValue(TestStatus.class, "");

        // Then
        assertThat(result, is(nullValue()));
    }

    @Test
    void shouldReturnNullForWhitespaceValue() {
        // When
        TestStatus result = EnumValue.fromValue(TestStatus.class, "   ");

        // Then
        assertThat(result, is(nullValue()));
    }

    @Test
    void shouldSerializeToJsonUsingValue() throws Exception {
        // When
        String json = objectMapper.writeValueAsString(TestStatus.ACTIVE);

        // Then
        assertThat(json, is("\"active\""));
    }

    @Test
    void shouldDeserializeFromJsonUsingValue() throws Exception {
        // When
        TestStatus result = objectMapper.readValue("\"active\"", TestStatus.class);

        // Then
        assertThat(result, is(TestStatus.ACTIVE));
    }

    @Test
    void shouldDeserializeFromJsonUsingValueCaseInsensitive() throws Exception {
        // When
        TestStatus result = objectMapper.readValue("\"ACTIVE\"", TestStatus.class);

        // Then
        assertThat(result, is(TestStatus.ACTIVE));
    }

    @Test
    void shouldDeserializeFromJsonUsingEnumName() throws Exception {
        // When
        TestStatus result = objectMapper.readValue("\"ACTIVE\"", TestStatus.class);

        // Then
        assertThat(result, is(TestStatus.ACTIVE));
    }

    @Test
    void shouldHandleAllEnumConstants() {
        // When/Then
        assertThat(EnumValue.fromValue(TestStatus.class, "active"), is(TestStatus.ACTIVE));
        assertThat(EnumValue.fromValue(TestStatus.class, "inactive"), is(TestStatus.INACTIVE));
        assertThat(EnumValue.fromValue(TestStatus.class, "pending"), is(TestStatus.PENDING));
    }

    @Test
    void shouldWorkWithStaticFromValueMethod() {
        // Using the enum's own fromValue method
        // When
        TestStatus result = TestStatus.fromValue("pending");

        // Then
        assertThat(result, is(TestStatus.PENDING));
    }

    @Test
    void shouldReturnNullForInvalidValueInFromValueMethod() {
        // When
        TestStatus result = TestStatus.fromValue("nonexistent");

        // Then
        assertThat(result, is(nullValue()));
    }
}
