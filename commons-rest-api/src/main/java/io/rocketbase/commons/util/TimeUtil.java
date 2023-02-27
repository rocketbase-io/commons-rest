package io.rocketbase.commons.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public abstract class TimeUtil {

    public static LocalDate min(LocalDate one, LocalDate two) {
        return Nulls.noneNullValue(one, two) ? (one.isAfter(two) ? two : one) : Nulls.notNull(one, two);
    }

    public static LocalTime min(LocalTime one, LocalTime two) {
        return Nulls.noneNullValue(one, two) ? (one.isAfter(two) ? two : one) : Nulls.notNull(one, two);
    }

    public static LocalDateTime min(LocalDateTime one, LocalDateTime two) {
        return Nulls.noneNullValue(one, two) ? (one.isAfter(two) ? two : one) : Nulls.notNull(one, two);
    }

    public static Instant min(Instant one, Instant two) {
        return Nulls.noneNullValue(one, two) ? (one.isAfter(two) ? two : one) : Nulls.notNull(one, two);
    }

    public static LocalDate max(LocalDate one, LocalDate two) {
        return Nulls.noneNullValue(one, two) ? (one.isBefore(two) ? two : one) : Nulls.notNull(one, two);
    }

    public static LocalTime max(LocalTime one, LocalTime two) {
        return Nulls.noneNullValue(one, two) ? (one.isBefore(two) ? two : one) : Nulls.notNull(one, two);
    }

    public static LocalDateTime max(LocalDateTime one, LocalDateTime two) {
        return Nulls.noneNullValue(one, two) ? (one.isBefore(two) ? two : one) : Nulls.notNull(one, two);
    }

    public static Instant max(Instant one, Instant two) {
        return Nulls.noneNullValue(one, two) ? (one.isBefore(two) ? two : one) : Nulls.notNull(one, two);
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
        long s = TimeUnit.MILLISECONDS.toSeconds(millis);
        long m = TimeUnit.MILLISECONDS.toMinutes(millis);

        if (m > 0) {
            return String.format("%d min %d sec %d ms", m, s % 60, ms);
        } else if (s > 0) {
            return String.format("%d sec %d ms", s, ms);
        }
        return String.format("%d ms", millis);
    }

    /**
     * format millis may to X days, hours etc...<br>
     * example output for 3600600 millis -> 1 hours 0 min 0 sec 600 ms
     */
    public static String convertMillisToFormatted(long millis) {
        long ms = millis % 1000;
        long s = TimeUnit.MILLISECONDS.toSeconds(millis);
        long m = TimeUnit.MILLISECONDS.toMinutes(millis);
        long h = TimeUnit.MILLISECONDS.toHours(millis);
        long d = TimeUnit.MILLISECONDS.toDays(millis);

        if (d > 0) {
            return String.format("%d day %d hour %d min %d sec %d ms", d, h % 60, m % 60, s % 60, ms);
        } else if (h > 0) {
            return String.format("%d hour %d min %d sec %d ms", h, m % 60, s % 60, ms);
        }
        return convertMillisToMinSecFormat(millis);
    }
}
