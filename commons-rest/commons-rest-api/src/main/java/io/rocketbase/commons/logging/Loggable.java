package io.rocketbase.commons.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * this annotation is only working on spring proxied beans!<br>
 * internal method call within the bean itself will not get logged - other bean calls will work
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Loggable {

    /**
     * Shall we track the duration from start to end
     */
    boolean duration() default true;

    /**
     * log current logged in user
     */
    boolean audit() default true;

    /**
     * allow to skip args...
     */
    boolean args() default true;

    /**
     * allow to skip results from log
     */
    boolean result() default false;

    String logLevel() default "DEBUG";

    /**
     * NONE will disable error logs
     */
    String errorLogLevel() default "WARN";


    /**
     * Shall we trim long texts in order to make log lines more readable<br>
     * <= 0 means disable trimming
     */
    int trimLength() default 100;

}
