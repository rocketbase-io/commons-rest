package io.rocketbase.commons.util;

public final class StringShorten {

    public static String left(String value, int maxLength) {
        return left(value, maxLength, "...");
    }

    public static String left(String value, int maxLength, String end) {
        if (value == null) {
            return "";
        } else if (value.length() < maxLength) {
            return value;
        } else {
            return value.substring(0, maxLength - Nulls.notEmpty(end, "").length()) + Nulls.notEmpty(end, "");
        }
    }

    public static String right(String value, int maxLength) {
        return right(value, maxLength, "...");
    }

    public static String right(String value, int maxLength, String beginning) {
        if (value == null) {
            return "";
        } else if (value.length() < maxLength) {
            return value;
        } else {
            return Nulls.notEmpty(beginning, "") + value.substring(value.length() - (maxLength - Nulls.notEmpty(beginning, "").length()));
        }
    }
}
