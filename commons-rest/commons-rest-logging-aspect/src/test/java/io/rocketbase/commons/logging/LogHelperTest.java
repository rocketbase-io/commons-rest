package io.rocketbase.commons.logging;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class LogHelperTest {

    @Test
    public void leftOnlyMillis() {
        // given
        long millis = 121;

        // when
        String result = new TestLogHelper().convertMillisToMinSecFormat(millis);
        // then
        assertThat(result, notNullValue());
        assertThat(result, equalTo("121 ms"));
    }

    @Test
    public void leftOnlyWithSeconds() {
        // given
        long millis = 1424;

        // when
        String result = new TestLogHelper().convertMillisToMinSecFormat(millis);
        // then
        assertThat(result, notNullValue());
        assertThat(result, equalTo("1 sec 424 ms"));
    }

    @Test
    public void leftOnlyWithMinute() {
        // given
        long millis = 60424;

        // when
        String result = new TestLogHelper().convertMillisToMinSecFormat(millis);
        // then
        assertThat(result, notNullValue());
        assertThat(result, equalTo("1 min 0 sec 424 ms"));
    }

    public static class TestLogHelper extends LogHelper {

    }
}