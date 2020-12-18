package io.rocketbase.commons.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UrlPartsTest {

    @Test
    public void testConcatPaths() {
        // given
        // when
        String blaSample = UrlParts.concatPaths("bla", 23);
        String nullSample = UrlParts.concatPaths(null);
        String withEndSample = UrlParts.concatPaths("/bla/", 23);

        // then
        assertThat(blaSample, equalTo("bla/23"));
        assertThat(nullSample, equalTo(""));
        assertThat(withEndSample, equalTo("/bla/23"));
    }

    @Test
    public void testEnsureEndsWithSlash() {
        // given
        // when
        String blaSample = UrlParts.ensureEndsWithSlash("/bla");
        String nullSample = UrlParts.ensureEndsWithSlash(null);
        String withEndSample = UrlParts.ensureEndsWithSlash("/bla/");

        // then
        assertThat(blaSample, equalTo("/bla/"));
        assertThat(nullSample, equalTo("/"));
        assertThat(withEndSample, equalTo("/bla/"));
    }

    @Test
    public void ensureStartsWithSlash() {
        // given
        // when
        String blaSample = UrlParts.ensureStartsWithSlash("/bla");
        String nullSample = UrlParts.ensureStartsWithSlash(null);
        String withEndSample = UrlParts.ensureStartsWithSlash("bla/");

        // then
        assertThat(blaSample, equalTo("/bla"));
        assertThat(nullSample, equalTo("/"));
        assertThat(withEndSample, equalTo("/bla/"));
    }

    @Test
    public void ensureStartsAndEndsWithSlash() {
        // given
        // when
        String blaSample = UrlParts.ensureStartsAndEndsWithSlash("/bla");
        String nullSample = UrlParts.ensureStartsAndEndsWithSlash(null);
        String withEndSample = UrlParts.ensureStartsAndEndsWithSlash("bla/");
        String withoutSample = UrlParts.ensureStartsAndEndsWithSlash("bla");

        // then
        assertThat(blaSample, equalTo("/bla/"));
        assertThat(nullSample, equalTo("/"));
        assertThat(withEndSample, equalTo("/bla/"));
        assertThat(withoutSample, equalTo("/bla/"));
    }

    @Test
    public void removeEndsWithSlash() {
        // given
        // when
        String blaSample = UrlParts.removeEndsWithSlash("/bla");
        String nullSample = UrlParts.removeEndsWithSlash(null);
        String withEndSample = UrlParts.removeEndsWithSlash("bla/");
        String withoutSample = UrlParts.removeEndsWithSlash("bla");

        // then
        assertThat(blaSample, equalTo("/bla"));
        assertThat(nullSample, equalTo(""));
        assertThat(withEndSample, equalTo("bla"));
        assertThat(withoutSample, equalTo("bla"));
    }
}