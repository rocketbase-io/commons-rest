package io.rocketbase.commons.logging;

import io.rocketbase.commons.util.StringShorten;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;

public class LogHelper {

    protected String toText(LoggableConfig config, ProceedingJoinPoint point) {

        Method method = MethodSignature.class.cast(point.getSignature()).getMethod();
        Object[] args = point.getArgs();

        StringBuilder log = new StringBuilder();
        log.append(method.getName())
                .append("(");
        if (!config.isSkipArgs()) {
            for (int pos = 0; pos < args.length; ++pos) {
                if (pos > 0) {
                    log.append(", ");
                }
                log.append(argToText(config, args[pos]));
            }
        } else {
            log.append("...");
        }
        log.append(")");
        return log.toString();
    }

    protected String throwableToText(Throwable exp) {
        StringBuilder text = new StringBuilder();
        text.append(exp.getClass()
                .getName());
        String msg = exp.getMessage();
        if (msg != null) {
            text.append("(")
                    .append(msg)
                    .append(")");
        }
        return text.toString();
    }

    protected String argToText(LoggableConfig config, Object arg) {
        StringBuilder text = new StringBuilder();
        if (arg == null) {
            text.append("null");
        } else if (!config.isSkipArgs()) {
            try {
                text.append(objToText(config, arg));
            } catch (Throwable ex) {
                text.append(String.format("[%s thrown %s]", arg.getClass().getName(), throwableToText(ex)));
            }
        }
        return text.toString();
    }

    protected String objToText(LoggableConfig config, Object arg) {
        String text;
        if (arg.getClass()
                .isArray()) {
            if (arg instanceof Object[]) {
                text = objectArrays(config, (Object[]) arg);
            } else {
                text = primitiveArrays(config, arg);
            }
        } else {
            String origin = arg.toString();
            if (arg instanceof String || origin.contains(" ")
                    || origin.isEmpty()) {
                text = StringShorten.left(origin, config.getTrimLength());
            } else {
                text = origin;
            }
        }
        return text;
    }

    private String objectArrays(LoggableConfig config, Object... arg) {
        StringBuilder bldr = new StringBuilder();
        bldr.append("[");
        for (Object item : arg) {
            if (bldr.length() > 1) {
                bldr.append(", ");
            }
            bldr.append(objToText(config, item));
        }
        if (config.isTrim() && bldr.length() > config.getTrimLength()) {
            return String.format("[%s]", StringShorten.left(bldr.toString(), config.getTrimLength()));
        } else {
            return bldr.append("]")
                    .toString();
        }
    }

    private String primitiveArrays(LoggableConfig config, Object arg) {
        String text;
        if (arg instanceof char[]) {
            text = Arrays.toString((char[]) arg);
        } else if (arg instanceof byte[]) {
            text = Arrays.toString((byte[]) arg);
        } else if (arg instanceof short[]) {
            text = Arrays.toString((short[]) arg);
        } else if (arg instanceof int[]) {
            text = Arrays.toString((int[]) arg);
        } else if (arg instanceof long[]) {
            text = Arrays.toString((long[]) arg);
        } else if (arg instanceof float[]) {
            text = Arrays.toString((float[]) arg);
        } else if (arg instanceof double[]) {
            text = Arrays.toString((double[]) arg);
        } else if (arg instanceof boolean[]) {
            text = Arrays.toString((boolean[]) arg);
        } else {
            text = "[unknown]";
        }

        if (config.isTrim() && text != null && text.length() > config.getTrimLength()) {
            return String.format("[%s]", StringShorten.left(text, config.getTrimLength()));
        } else {
            return text;
        }
    }
}
