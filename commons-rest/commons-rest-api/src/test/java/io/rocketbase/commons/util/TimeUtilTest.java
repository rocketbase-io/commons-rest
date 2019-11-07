package io.rocketbase.commons.util;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class TimeUtilTest {

    private static LocalDateTime twoThousand = LocalDateTime.of(2000, 1, 1, 10, 0, 0, 0);
    private static LocalDateTime twoThousandOne = LocalDateTime.of(2001, 1, 1, 11, 0, 0, 0);

    @Test
    public void min() {
        // LocalDateTime
        assertThat(TimeUtil.min(twoThousand, twoThousandOne), equalTo(twoThousand));
        assertThat(TimeUtil.min(twoThousand, twoThousand), equalTo(twoThousand));
        assertThat(TimeUtil.min(null, twoThousand), equalTo(twoThousand));
        // LocalDate
        assertThat(TimeUtil.min(twoThousand.toLocalDate(), twoThousandOne.toLocalDate()), equalTo(twoThousand.toLocalDate()));
        assertThat(TimeUtil.min(twoThousand.toLocalDate(), twoThousand.toLocalDate()), equalTo(twoThousand.toLocalDate()));
        assertThat(TimeUtil.min(null, twoThousand.toLocalDate()), equalTo(twoThousand.toLocalDate()));
        // LocalTime
        assertThat(TimeUtil.min(twoThousand.toLocalTime(), twoThousandOne.toLocalTime()), equalTo(twoThousand.toLocalTime()));
        assertThat(TimeUtil.min(twoThousand.toLocalTime(), twoThousand.toLocalTime()), equalTo(twoThousand.toLocalTime()));
        assertThat(TimeUtil.min(null, twoThousand.toLocalTime()), equalTo(twoThousand.toLocalTime()));
        // Instant
        assertThat(TimeUtil.min(twoThousand.toInstant(UTC), twoThousandOne.toInstant(UTC)), equalTo(twoThousand.toInstant(UTC)));
        assertThat(TimeUtil.min(twoThousand.toInstant(UTC), twoThousand.toInstant(UTC)), equalTo(twoThousand.toInstant(UTC)));
        assertThat(TimeUtil.min(null, twoThousand.toInstant(UTC)), equalTo(twoThousand.toInstant(UTC)));
    }

    @Test
    public void max() {
        // LocalDateTime
        assertThat(TimeUtil.max(twoThousand, twoThousandOne), equalTo(twoThousandOne));
        assertThat(TimeUtil.max(twoThousand, twoThousand), equalTo(twoThousand));
        assertThat(TimeUtil.max(null, twoThousand), equalTo(twoThousand));
        // LocalDate
        assertThat(TimeUtil.max(twoThousand.toLocalDate(), twoThousandOne.toLocalDate()), equalTo(twoThousandOne.toLocalDate()));
        assertThat(TimeUtil.max(twoThousand.toLocalDate(), twoThousand.toLocalDate()), equalTo(twoThousand.toLocalDate()));
        assertThat(TimeUtil.max(null, twoThousand.toLocalDate()), equalTo(twoThousand.toLocalDate()));
        // LocalTime
        assertThat(TimeUtil.max(twoThousand.toLocalTime(), twoThousandOne.toLocalTime()), equalTo(twoThousandOne.toLocalTime()));
        assertThat(TimeUtil.max(twoThousand.toLocalTime(), twoThousand.toLocalTime()), equalTo(twoThousand.toLocalTime()));
        assertThat(TimeUtil.max(null, twoThousand.toLocalTime()), equalTo(twoThousand.toLocalTime()));
        // Instant
        assertThat(TimeUtil.max(twoThousand.toInstant(UTC), twoThousandOne.toInstant(UTC)), equalTo(twoThousandOne.toInstant(UTC)));
        assertThat(TimeUtil.max(twoThousand.toInstant(UTC), twoThousand.toInstant(UTC)), equalTo(twoThousand.toInstant(UTC)));
        assertThat(TimeUtil.max(null, twoThousand.toInstant(UTC)), equalTo(twoThousand.toInstant(UTC)));
    }

    @Test
    public void firstDayOfYear() {
        assertThat(TimeUtil.firstDayOfYear(2000), equalTo(LocalDate.of(2000, 1, 1)));
    }

    @Test
    public void lastDayOfYear() {
        assertThat(TimeUtil.lastDayOfYear(2000), equalTo(LocalDate.of(2000, 12, 31)));
    }

    @Test
    public void isBeforeOrEquals() {
        // LocalDateTime
        assertThat(TimeUtil.isBeforeOrEquals(twoThousand, twoThousandOne), equalTo(true));
        assertThat(TimeUtil.isBeforeOrEquals(twoThousand, twoThousand), equalTo(true));
        assertThat(TimeUtil.isBeforeOrEquals(twoThousandOne, twoThousand), equalTo(false));
        // LocalDate
        assertThat(TimeUtil.isBeforeOrEquals(twoThousand.toLocalDate(), twoThousandOne.toLocalDate()), equalTo(true));
        assertThat(TimeUtil.isBeforeOrEquals(twoThousand.toLocalDate(), twoThousand.toLocalDate()), equalTo(true));
        assertThat(TimeUtil.isBeforeOrEquals(twoThousandOne.toLocalDate(), twoThousand.toLocalDate()), equalTo(false));
        // Instant
        assertThat(TimeUtil.isBeforeOrEquals(twoThousand.toInstant(UTC), twoThousandOne.toInstant(UTC)), equalTo(true));
        assertThat(TimeUtil.isBeforeOrEquals(twoThousand.toInstant(UTC), twoThousand.toInstant(UTC)), equalTo(true));
        assertThat(TimeUtil.isBeforeOrEquals(twoThousandOne.toInstant(UTC), twoThousand.toInstant(UTC)), equalTo(false));
    }


    @Test
    public void isAfterOrEquals() {
        // LocalDateTime
        assertThat(TimeUtil.isAfterOrEquals(twoThousand, twoThousandOne), equalTo(false));
        assertThat(TimeUtil.isAfterOrEquals(twoThousand, twoThousand), equalTo(true));
        assertThat(TimeUtil.isAfterOrEquals(twoThousandOne, twoThousand), equalTo(true));
        // LocalDate
        assertThat(TimeUtil.isAfterOrEquals(twoThousand.toLocalDate(), twoThousandOne.toLocalDate()), equalTo(false));
        assertThat(TimeUtil.isAfterOrEquals(twoThousand.toLocalDate(), twoThousand.toLocalDate()), equalTo(true));
        assertThat(TimeUtil.isAfterOrEquals(twoThousandOne.toLocalDate(), twoThousand.toLocalDate()), equalTo(true));
        // Instant
        assertThat(TimeUtil.isAfterOrEquals(twoThousand.toInstant(UTC), twoThousandOne.toInstant(UTC)), equalTo(false));
        assertThat(TimeUtil.isAfterOrEquals(twoThousand.toInstant(UTC), twoThousand.toInstant(UTC)), equalTo(true));
        assertThat(TimeUtil.isAfterOrEquals(twoThousandOne.toInstant(UTC), twoThousand.toInstant(UTC)), equalTo(true));
    }
}