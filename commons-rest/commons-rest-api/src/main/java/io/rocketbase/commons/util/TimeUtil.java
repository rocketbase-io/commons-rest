package io.rocketbase.commons.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public abstract class TimeUtil {

    public static LocalDate min(LocalDate one, LocalDate two) {
        return Nulls.noneNullValue(one, two) ?
                (one.isAfter(two) ? two : one) :
                Nulls.notNull(one, two);
    }

    public static LocalTime min(LocalTime one, LocalTime two) {
        return Nulls.noneNullValue(one, two) ?
                (one.isAfter(two) ? two : one) :
                Nulls.notNull(one, two);
    }

    public static LocalDateTime min(LocalDateTime one, LocalDateTime two) {
        return Nulls.noneNullValue(one, two) ?
                (one.isAfter(two) ? two : one) :
                Nulls.notNull(one, two);
    }

    public static Instant min(Instant one, Instant two) {
        return Nulls.noneNullValue(one, two) ?
                (one.isAfter(two) ? two : one) :
                Nulls.notNull(one, two);
    }

    public static LocalDate max(LocalDate one, LocalDate two) {
        return Nulls.noneNullValue(one, two) ?
                (one.isBefore(two) ? two : one) :
                Nulls.notNull(one, two);
    }

    public static LocalTime max(LocalTime one, LocalTime two) {
        return Nulls.noneNullValue(one, two) ?
                (one.isBefore(two) ? two : one) :
                Nulls.notNull(one, two);
    }

    public static LocalDateTime max(LocalDateTime one, LocalDateTime two) {
        return Nulls.noneNullValue(one, two) ?
                (one.isBefore(two) ? two : one) :
                Nulls.notNull(one, two);
    }

    public static Instant max(Instant one, Instant two) {
        return Nulls.noneNullValue(one, two) ?
                (one.isBefore(two) ? two : one) :
                Nulls.notNull(one, two);
    }

    public static LocalDate firstDayOfYear(int year) {
        return LocalDate.of(year, 1, 1);
    }

    public static LocalDate lastDayOfYear(int year) {
        return LocalDate.of(year, 12, 31);
    }

    public static boolean isBeforeOrEquals(LocalDate val, LocalDate compare) {
        return val.isBefore(compare) || val.isEqual(compare);
    }

    public static boolean isBeforeOrEquals(LocalDateTime val, LocalDateTime compare) {
        return val.isBefore(compare) || val.isEqual(compare);
    }

    public static boolean isBeforeOrEquals(Instant val, Instant compare) {
        return val.isBefore(compare) || val.compareTo(compare) == 0;
    }

    public static boolean isAfterOrEquals(LocalDate val, LocalDate compare) {
        return val.isAfter(compare) || val.isEqual(compare);
    }

    public static boolean isAfterOrEquals(LocalDateTime val, LocalDateTime compare) {
        return val.isAfter(compare) || val.isEqual(compare);
    }

    public static boolean isAfterOrEquals(Instant val, Instant compare) {
        return val.isAfter(compare) || val.compareTo(compare) == 0;
    }

    public static String convertMillisToMinSecFormat(long millis) {
        long ms = millis % 1000;
        long s = (millis / 1000) % 60;
        long m = ((millis / 1000) / 60) % 60;
        if (s > 0 && m <= 0) {
            return String.format("%d sec %d ms", s, ms);
        } else if (s > 0 || m > 0) {
            return String.format("%d min %d sec %d ms", m, s, ms);
        } else {
            return String.format("%d ms", ms);
        }
    }
}
