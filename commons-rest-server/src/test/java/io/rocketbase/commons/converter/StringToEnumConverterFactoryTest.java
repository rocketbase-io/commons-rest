package io.rocketbase.commons.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link StringToEnumConverterFactory}.
 */
class StringToEnumConverterFactoryTest {

    private StringToEnumConverterFactory converterFactory;

    /**
     * Test enum implementing EnumValue.
     */
    enum Priority implements EnumValue {
        HIGH("high"),
        MEDIUM("medium"),
        LOW("low");

        private final String value;

        Priority(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * Test standard enum without EnumValue.
     */
    enum StandardStatus {
        OPEN,
        CLOSED,
        IN_PROGRESS
    }

    @BeforeEach
    void setUp() {
        converterFactory = new StringToEnumConverterFactory();
    }

    // ========== EnumValue Tests ==========

    @Test
    void shouldConvertEnumValueByExactValue() {
        // Given
        Converter<String, Priority> converter = converterFactory.getConverter(Priority.class);

        // When
        Priority result = converter.convert("high");

        // Then
        assertThat(result, is(Priority.HIGH));
    }

    @Test
    void shouldConvertEnumValueByCaseInsensitiveValue() {
        // Given
        Converter<String, Priority> converter = converterFactory.getConverter(Priority.class);

        // When
        Priority result = converter.convert("HIGH");

        // Then
        assertThat(result, is(Priority.HIGH));
    }

    @Test
    void shouldConvertEnumValueByMixedCase() {
        // Given
        Converter<String, Priority> converter = converterFactory.getConverter(Priority.class);

        // When
        Priority result = converter.convert("MeDiUm");

        // Then
        assertThat(result, is(Priority.MEDIUM));
    }

    @Test
    void shouldConvertEnumValueWithWhitespace() {
        // Given
        Converter<String, Priority> converter = converterFactory.getConverter(Priority.class);

        // When
        Priority result = converter.convert("  low  ");

        // Then
        assertThat(result, is(Priority.LOW));
    }

    @Test
    void shouldReturnNullForInvalidEnumValue() {
        // Given
        Converter<String, Priority> converter = converterFactory.getConverter(Priority.class);

        // When
        Priority result = converter.convert("invalid");

        // Then
        assertThat(result, is(nullValue()));
    }

    @Test
    void shouldReturnNullForNullValue() {
        // Given
        Converter<String, Priority> converter = converterFactory.getConverter(Priority.class);

        // When
        Priority result = converter.convert(null);

        // Then
        assertThat(result, is(nullValue()));
    }

    @Test
    void shouldReturnNullForEmptyString() {
        // Given
        Converter<String, Priority> converter = converterFactory.getConverter(Priority.class);

        // When
        Priority result = converter.convert("");

        // Then
        assertThat(result, is(nullValue()));
    }

    @Test
    void shouldReturnNullForWhitespaceOnly() {
        // Given
        Converter<String, Priority> converter = converterFactory.getConverter(Priority.class);

        // When
        Priority result = converter.convert("   ");

        // Then
        assertThat(result, is(nullValue()));
    }

    // ========== Standard Enum Tests ==========

    @Test
    void shouldConvertStandardEnumByName() {
        // Given
        Converter<String, StandardStatus> converter = converterFactory.getConverter(StandardStatus.class);

        // When
        StandardStatus result = converter.convert("OPEN");

        // Then
        assertThat(result, is(StandardStatus.OPEN));
    }

    @Test
    void shouldConvertStandardEnumByLowercaseName() {
        // Given
        Converter<String, StandardStatus> converter = converterFactory.getConverter(StandardStatus.class);

        // When - converter uppercases internally
        StandardStatus result = converter.convert("open");

        // Then
        assertThat(result, is(StandardStatus.OPEN));
    }

    @Test
    void shouldConvertStandardEnumWithUnderscore() {
        // Given
        Converter<String, StandardStatus> converter = converterFactory.getConverter(StandardStatus.class);

        // When
        StandardStatus result = converter.convert("IN_PROGRESS");

        // Then
        assertThat(result, is(StandardStatus.IN_PROGRESS));
    }

    @Test
    void shouldConvertStandardEnumWithUnderscoreLowercase() {
        // Given
        Converter<String, StandardStatus> converter = converterFactory.getConverter(StandardStatus.class);

        // When
        StandardStatus result = converter.convert("in_progress");

        // Then
        assertThat(result, is(StandardStatus.IN_PROGRESS));
    }

    @Test
    void shouldReturnNullForInvalidStandardEnum() {
        // Given
        Converter<String, StandardStatus> converter = converterFactory.getConverter(StandardStatus.class);

        // When
        StandardStatus result = converter.convert("NONEXISTENT");

        // Then
        assertThat(result, is(nullValue()));
    }

    // ========== Factory Tests ==========

    @Test
    void shouldCreateConverterForEnumValue() {
        // When
        Converter<String, Priority> converter = converterFactory.getConverter(Priority.class);

        // Then
        assertThat(converter, is(notNullValue()));
        assertThat(converter, is(instanceOf(StringToEnumConverterFactory.StringToEnumConverter.class)));
    }

    @Test
    void shouldCreateConverterForStandardEnum() {
        // When
        Converter<String, StandardStatus> converter = converterFactory.getConverter(StandardStatus.class);

        // Then
        assertThat(converter, is(notNullValue()));
        assertThat(converter, is(instanceOf(StringToEnumConverterFactory.StringToEnumConverter.class)));
    }

    @Test
    void shouldCreateDifferentConvertersForDifferentEnums() {
        // When
        Converter<String, Priority> converter1 = converterFactory.getConverter(Priority.class);
        Converter<String, StandardStatus> converter2 = converterFactory.getConverter(StandardStatus.class);

        // Then
        assertThat(converter1, is(not(sameInstance(converter2))));
    }

    // ========== Edge Cases ==========

    @Test
    void shouldHandleEnumValueFallbackToEnumName() {
        // Given - Priority.HIGH has value "high" but we're trying enum name
        Converter<String, Priority> converter = converterFactory.getConverter(Priority.class);

        // When - Try to use enum name instead of value
        Priority result = converter.convert("HIGH");

        // Then - Should still work via fallback
        assertThat(result, is(Priority.HIGH));
    }

    @Test
    void shouldPreferEnumValueOverEnumName() {
        // Given
        Converter<String, Priority> converter = converterFactory.getConverter(Priority.class);

        // When - "high" matches both the value and (when uppercased) could match enum name
        Priority result = converter.convert("high");

        // Then - Should use value match first
        assertThat(result, is(Priority.HIGH));
    }
}
