package io.rocketbase.commons.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static io.rocketbase.commons.util.StopwatchParts.PartOrdering.NATURAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

class StopwatchPartsTest {

    @Test
    void printDefault() throws InterruptedException {
        StopwatchParts stopwatch = StopwatchParts.start();
        for (int x = 0; x < 10; x++) {
            TimeUnit.MILLISECONDS.sleep(6 * x + 10);
            stopwatch.part("part" + x);
        }
        String print = stopwatch.print();
        assertThat(print, matchesPattern(
                "⏱ [0-9]+ ms \\[[0-9]{1,3}% part9\\: [0-9]+ ms \\| [0-9]{1,3}% part8\\: [0-9]+ ms \\| [0-9]{1,3}% part7\\: [0-9]+ ms \\| [0-9]{1,3}% part6\\: [0-9]+ ms \\| [0-9]{1,3}% part5\\: [0-9]+ ms \\| [0-9]{1,3}% part4\\: [0-9]+ ms \\| [0-9]{1,3}% part3\\: [0-9]+ ms \\| [0-9]{1,3}% part2\\: [0-9]+ ms \\| [0-9]{1,3}% part1\\: [0-9]+ ms \\| [0-9]{1,3}% part0\\: [0-9]+ ms\\]"));

    }

    @Test
    void printConfiguredNaturalOrder() throws InterruptedException {
        StopwatchParts stopwatch = StopwatchParts.build()
                .withOrdered(NATURAL)
                .start();
        for (int x = 0; x < 10; x++) {
            TimeUnit.MILLISECONDS.sleep(6 * x + 10);
            stopwatch.part("part" + x);
        }
        String print = stopwatch.print();
        assertThat(print, matchesPattern(
                "⏱ [0-9]+ ms \\[[0-9]{1,3}% part0: [0-9]+ ms \\| [0-9]{1,3}% part1: [0-9]+ ms \\| [0-9]{1,3}% part2: [0-9]+ ms \\| [0-9]{1,3}% part3: [0-9]+ ms \\| [0-9]{1,3}% part4: [0-9]+ ms \\| [0-9]{1,3}% part5: [0-9]+ ms \\| [0-9]{1,3}% part6: [0-9]+ ms \\| [0-9]{1,3}% part7: [0-9]+ ms \\| [0-9]{1,3}% part8: [0-9]+ ms \\| [0-9]{1,3}% part9: [0-9]+ ms\\]"));
    }

    @Test
    void printConfiguredLimit() throws InterruptedException {
        StopwatchParts stopwatch = StopwatchParts.build()
                .limitParts(4)
                .withSkippedSummary(false)
                .start();
        for (int x = 0; x < 10; x++) {
            TimeUnit.MILLISECONDS.sleep(6 * x + 10);
            stopwatch.part("part" + x);
        }
        String print = stopwatch.print();
        assertThat(print, matchesPattern(
                "⏱ [0-9]+ ms \\[([0-9]{1,3}% part[0-9]: [0-9]+ ms \\| ){3}[0-9]{1,3}% part[0-9]: [0-9]+ ms\\]"));
    }

    @Test
    void printConfiguredLimitCustomOrder() throws InterruptedException {
        StopwatchParts stopwatch = StopwatchParts.build()
                .limitParts(3)
                .withOrdered(NATURAL)
                .withSkippedSummary(false)
                .start();
        for (int x = 0; x < 10; x++) {
            TimeUnit.MILLISECONDS.sleep(6 * x + 10);
            stopwatch.part("part" + x);
        }
        String print = stopwatch.print();
        assertThat(print, matchesPattern(
                "⏱ [0-9]+ ms \\[[0-9]{1,3}% part0: [0-9]+ ms \\| [0-9]{1,3}% part1: [0-9]+ ms \\| [0-9]{1,3}% part2: [0-9]+ ms\\]"));
    }

    @Test
    void printConfiguredNoPercentage() throws InterruptedException {
        StopwatchParts stopwatch = StopwatchParts.build()
                .limitParts(3)
                .withOrdered(NATURAL)
                .withPercentage(false)
                .withSkippedSummary(false)
                .start();
        for (int x = 0; x < 10; x++) {
            TimeUnit.MILLISECONDS.sleep(6 * x + 10);
            stopwatch.part("part" + x);
        }
        String print = stopwatch.print();
        assertThat(print, matchesPattern(
                "⏱ [0-9]+ ms \\[part0: [0-9]+ ms \\| part1: [0-9]+ ms \\| part2: [0-9]+ ms\\]"));
    }

    @Test
    void printConfiguredNoTimings() throws InterruptedException {
        StopwatchParts stopwatch = StopwatchParts.build()
                .limitParts(3)
                .withOrdered(NATURAL)
                .withTimings(false)
                .withSkippedSummary(false)
                .start();
        for (int x = 0; x < 10; x++) {
            TimeUnit.MILLISECONDS.sleep(6 * x + 10);
            stopwatch.part("part" + x);
        }
        String print = stopwatch.print();
        assertThat(print, matchesPattern(
                "⏱ [0-9]+ ms \\[[0-9]{1,3}% part0 \\| [0-9]{1,3}% part1 \\| [0-9]{1,3}% part2\\]"));
    }

    @Test
    void printConfiguredNoParts() throws InterruptedException {
        StopwatchParts stopwatch = StopwatchParts.build()
                .limitParts(0)
                .start();
        for (int x = 0; x < 10; x++) {
            TimeUnit.MILLISECONDS.sleep(6 * x + 10);
            stopwatch.part("part" + x);
        }
        String print = stopwatch.print();
        assertThat(print, matchesPattern(
                "⏱ [0-9]+ ms"));
    }

    @Test
    void printConfiguredOtherPrefix() throws InterruptedException {
        StopwatchParts stopwatch = StopwatchParts.build()
                .limitParts(0)
                .withPrefix("🎉 ")
                .start();
        for (int x = 0; x < 10; x++) {
            TimeUnit.MILLISECONDS.sleep(6 * x + 10);
            stopwatch.part("part" + x);
        }
        String print = stopwatch.print();
        assertThat(print, matchesPattern(
                "🎉 [0-9]+ ms"));
    }

    @Test
    void printConfiguredLimitSummary() throws InterruptedException {
        StopwatchParts stopwatch = StopwatchParts.build()
                .limitParts(3)
                .start();
        for (int x = 0; x < 10; x++) {
            TimeUnit.MILLISECONDS.sleep(6 * x + 10);
            stopwatch.part("part" + x);
        }
        String print = stopwatch.print();
        assertThat(print, matchesPattern(
                "⏱ [0-9]+ ms \\[([0-9]{1,3}% part[0-9]: [0-9]+ ms \\| ){2}[0-9]{1,3}% part[0-9]: [0-9]+ ms \\| [0-9]{1,3}% \"other\": [0-9]+ ms\\]"));
    }
}