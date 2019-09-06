package io.rocketbase.commons.resource;

import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class BaseRestResourceTest {

    @Test
    public void testEnsureEndsWithSlash() {
        // given
        // when
        String blaSample = getTestResource().ensureEndsWithSlash("/bla");
        String nullSample = getTestResource().ensureEndsWithSlash(null);
        String withEndSample = getTestResource().ensureEndsWithSlash("/bla/");

        // then
        assertThat(blaSample, equalTo("/bla/"));
        assertThat(nullSample, equalTo("/"));
        assertThat(withEndSample, equalTo("/bla/"));
    }

    @Test
    public void ensureStartsWithSlash() {
        // given
        // when
        String blaSample = getTestResource().ensureStartsWithSlash("/bla");
        String nullSample = getTestResource().ensureStartsWithSlash(null);
        String withEndSample = getTestResource().ensureStartsWithSlash("bla/");

        // then
        assertThat(blaSample, equalTo("/bla"));
        assertThat(nullSample, equalTo("/"));
        assertThat(withEndSample, equalTo("/bla/"));
    }

    @Test
    public void ensureStartsAndEndsWithSlash() {
        // given
        // when
        String blaSample = getTestResource().ensureStartsAndEndsWithSlash("/bla");
        String nullSample = getTestResource().ensureStartsAndEndsWithSlash(null);
        String withEndSample = getTestResource().ensureStartsAndEndsWithSlash("bla/");
        String withoutSample = getTestResource().ensureStartsAndEndsWithSlash("bla");

        // then
        assertThat(blaSample, equalTo("/bla/"));
        assertThat(nullSample, equalTo("/"));
        assertThat(withEndSample, equalTo("/bla/"));
        assertThat(withoutSample, equalTo("/bla/"));
    }


    private BaseRestResource getTestResource() {
        return new TestRestResource();
    }

    private class TestRestResource implements BaseRestResource {
    }
}