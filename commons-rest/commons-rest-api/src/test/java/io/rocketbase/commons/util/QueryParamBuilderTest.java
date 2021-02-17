package io.rocketbase.commons.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class QueryParamBuilderTest {

    @Test
    void appendParamsString() {
        // given
        UriComponentsBuilder builderOne = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderTwo = UriComponentsBuilder.fromUriString("http://localhost");
        String value = null;
        // when
        QueryParamBuilder.appendParams(builderOne, "test", "value");
        QueryParamBuilder.appendParams(builderTwo, "test", value);
        // then
        assertThat(builderOne.toUriString(), equalTo("http://localhost?test=value"));
        assertThat(builderTwo.toUriString(), equalTo("http://localhost"));
    }

    @Test
    void appendParamsPageable() {
        // given
        UriComponentsBuilder builderOne = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderTwo = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderThree = UriComponentsBuilder.fromUriString("http://localhost");
        Pageable value = null;
        // when
        QueryParamBuilder.appendParams(builderOne, PageRequest.of(0, 10));
        QueryParamBuilder.appendParams(builderTwo, PageRequest.of(0, 10, Sort.by("id").descending()));
        QueryParamBuilder.appendParams(builderThree, value);
        // then
        assertThat(builderOne.toUriString(), equalTo("http://localhost?page=0&pageSize=10"));
        assertThat(builderTwo.toUriString(), equalTo("http://localhost?page=0&pageSize=10&sort=id,desc"));
        assertThat(builderThree.toUriString(), equalTo("http://localhost"));
    }

    @Test
    void appendParamsPageableWithCustomKeys() {
        // given
        UriComponentsBuilder builderOne = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderTwo = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderThree = UriComponentsBuilder.fromUriString("http://localhost");
        Pageable value = null;
        // when
        QueryParamBuilder.appendParams(builderOne, "s", "p", "o", PageRequest.of(0, 10));
        QueryParamBuilder.appendParams(builderTwo, "s", "p", "o", PageRequest.of(0, 10, Sort.by("id").descending()));
        QueryParamBuilder.appendParams(builderThree, "s", "p", "o", value);
        // then
        assertThat(builderOne.toUriString(), equalTo("http://localhost?p=0&s=10"));
        assertThat(builderTwo.toUriString(), equalTo("http://localhost?p=0&s=10&o=id,desc"));
        assertThat(builderThree.toUriString(), equalTo("http://localhost"));
    }

    @Test
    void appendParamsNumber() {
        // given
        UriComponentsBuilder builderOne = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderTwo = UriComponentsBuilder.fromUriString("http://localhost");
        Long value = null;
        // when
        QueryParamBuilder.appendParams(builderOne, "test", 100);
        QueryParamBuilder.appendParams(builderTwo, "test", value);
        // then
        assertThat(builderOne.toUriString(), equalTo("http://localhost?test=100"));
        assertThat(builderTwo.toUriString(), equalTo("http://localhost"));
    }

    @Test
    void appendParamsBoolean() {
        // given
        UriComponentsBuilder builderOne = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderTwo = UriComponentsBuilder.fromUriString("http://localhost");
        Boolean value = null;
        // when
        QueryParamBuilder.appendParams(builderOne, "test", true);
        QueryParamBuilder.appendParams(builderTwo, "test", value);
        // then
        assertThat(builderOne.toUriString(), equalTo("http://localhost?test=true"));
        assertThat(builderTwo.toUriString(), equalTo("http://localhost"));
    }

    @Test
    void appendParamsLocalDate() {
        // given
        UriComponentsBuilder builderOne = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderTwo = UriComponentsBuilder.fromUriString("http://localhost");
        LocalDate value = null;
        // when
        QueryParamBuilder.appendParams(builderOne, "test", LocalDate.of(1970, 1, 1));
        QueryParamBuilder.appendParams(builderTwo, "test", value);
        // then
        assertThat(builderOne.toUriString(), equalTo("http://localhost?test=1970-01-01"));
        assertThat(builderTwo.toUriString(), equalTo("http://localhost"));
    }

    @Test
    void appendParamsLocalTime() {
        // given
        UriComponentsBuilder builderOne = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderTwo = UriComponentsBuilder.fromUriString("http://localhost");
        LocalTime value = null;
        // when
        QueryParamBuilder.appendParams(builderOne, "test", LocalTime.of(12, 0));
        QueryParamBuilder.appendParams(builderTwo, "test", value);
        // then
        assertThat(builderOne.toUriString(), equalTo("http://localhost?test=12:00:00"));
        assertThat(builderTwo.toUriString(), equalTo("http://localhost"));
    }

    @Test
    void appendParamsLocalDateTime() {
        // given
        UriComponentsBuilder builderOne = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderTwo = UriComponentsBuilder.fromUriString("http://localhost");
        LocalDateTime value = null;
        // when
        QueryParamBuilder.appendParams(builderOne, "test", LocalDateTime.of(1970, 1, 1, 12, 15, 30));
        QueryParamBuilder.appendParams(builderTwo, "test", value);
        // then
        assertThat(builderOne.toUriString(), equalTo("http://localhost?test=1970-01-01T12:15:30"));
        assertThat(builderTwo.toUriString(), equalTo("http://localhost"));
    }

    enum TestSample {
        GOOD,
        NOT_GOOD;
    }

    @Test
    void appendParamsEnum() {
        // given
        UriComponentsBuilder builderOne = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderTwo = UriComponentsBuilder.fromUriString("http://localhost");
        TestSample value = null;
        // when
        QueryParamBuilder.appendParams(builderOne, "test", TestSample.GOOD);
        QueryParamBuilder.appendParams(builderTwo, "test", value);
        // then
        assertThat(builderOne.toUriString(), equalTo("http://localhost?test=GOOD"));
        assertThat(builderTwo.toUriString(), equalTo("http://localhost"));
    }

    @Test
    void appendParamEnums() {
        // given
        UriComponentsBuilder builderOne = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderTwo = UriComponentsBuilder.fromUriString("http://localhost");
        Collection<TestSample> value = null;
        // when
        QueryParamBuilder.appendParamEnums(builderOne, "test", Arrays.asList(TestSample.GOOD, TestSample.NOT_GOOD));
        QueryParamBuilder.appendParamEnums(builderTwo, "test", value);
        // then
        assertThat(builderOne.toUriString(), equalTo("http://localhost?test=GOOD&test=NOT_GOOD"));
        assertThat(builderTwo.toUriString(), equalTo("http://localhost"));
    }

    @Test
    void appendParamNumbers() {
        // given
        UriComponentsBuilder builderOne = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderTwo = UriComponentsBuilder.fromUriString("http://localhost");
        Collection<Long> value = null;
        // when
        QueryParamBuilder.appendParamNumbers(builderOne, "test", Arrays.asList(10.1, 214.56));
        QueryParamBuilder.appendParamNumbers(builderTwo, "test", value);
        // then
        assertThat(builderOne.toUriString(), equalTo("http://localhost?test=10.1&test=214.56"));
        assertThat(builderTwo.toUriString(), equalTo("http://localhost"));
    }


    @Test
    void appendParamStrings() {
        // given
        UriComponentsBuilder builderOne = UriComponentsBuilder.fromUriString("http://localhost");
        UriComponentsBuilder builderTwo = UriComponentsBuilder.fromUriString("http://localhost");
        Collection<String> value = null;
        // when
        QueryParamBuilder.appendParamStrings(builderOne, "test", Arrays.asList("a", "bc"));
        QueryParamBuilder.appendParamStrings(builderTwo, "test", value);
        // then
        assertThat(builderOne.toUriString(), equalTo("http://localhost?test=a&test=bc"));
        assertThat(builderTwo.toUriString(), equalTo("http://localhost"));
    }

}