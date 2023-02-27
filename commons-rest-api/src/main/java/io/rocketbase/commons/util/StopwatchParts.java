package io.rocketbase.commons.util;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

import static io.rocketbase.commons.util.StopwatchParts.PartOrdering.NATURAL;
import static io.rocketbase.commons.util.StopwatchParts.PartOrdering.TIME_DESC;

/**
 * improved stopwatch for time-tracking that supports parts to know which area of a run tooks how long<br>
 * sample output could look like:<br>
 * ⏱ 4109 ms [29% syncValues: 1197 ms | 23% queryProduct: 953 ms | 21% colorMapping: 880 ms | 10% brainMap: 444 ms ...]
 */
public class StopwatchParts {

    private StopwatchPartitionedConfig config;
    private Map<String, Long> detailTimings;
    private long lastLogged;

    private String lastCaption;

    StopwatchParts(StopwatchPartitionedConfig config) {
        this.config = config;
        detailTimings = new LinkedHashMap<>();
        lastLogged = System.currentTimeMillis();
    }

    public static StopwatchParts start() {
        return new StopwatchPartitionedConfig().start();
    }

    public static StopwatchPartitionedConfig build() {
        return new StopwatchPartitionedConfig();
    }

    enum PartOrdering {
        NATURAL, TIME_DESC;
    }

    @Getter(AccessLevel.PACKAGE)
    public static class StopwatchPartitionedConfig {

        StopwatchPartitionedConfig() {
        }

        private String printPrefix = "⏱ ";
        private PartOrdering partOrdering = TIME_DESC;
        private boolean partPercentage = true;
        private boolean partTimings = true;
        private int limitParts = -1;

        private boolean skippedSummary = true;
        private String skippedPartName = "\"other\"";

        public StopwatchPartitionedConfig withPrefix(String printPrefix) {
            this.printPrefix = printPrefix;
            return this;
        }

        public StopwatchPartitionedConfig withOrdered(PartOrdering partOrdering) {
            this.partOrdering = partOrdering;
            return this;
        }

        public StopwatchPartitionedConfig withPercentage(boolean partPercentage) {
            this.partPercentage = partPercentage;
            return this;
        }

        public StopwatchPartitionedConfig withTimings(boolean partTimings) {
            this.partTimings = partTimings;
            return this;
        }

        public StopwatchPartitionedConfig limitParts(int limitParts) {
            this.limitParts = limitParts;
            return this;
        }

        public StopwatchPartitionedConfig skippedPartName(String skippedPartName) {
            this.skippedPartName = skippedPartName;
            return this;
        }

        public StopwatchPartitionedConfig withSkippedSummary(boolean skippedSummary) {
            this.skippedSummary = skippedSummary;
            return this;
        }

        public StopwatchParts start() {
            return new StopwatchParts(this);
        }

    }

    /**
     * if part is already existing it will get counted as total of part
     */
    public void part(String caption) {
        long time = System.currentTimeMillis() - lastLogged;
        detailTimings.put(caption, detailTimings.getOrDefault(caption, 0L) + time);
        lastLogged = System.currentTimeMillis();
        lastCaption = caption;
    }

    /**
     * used to print all statistics as configured
     */
    public String print() {
        if (lastCaption != null) {
            // log last part as finished
            part(lastCaption);
        }
        StringBuilder builder = new StringBuilder();
        long totalTime = totalMillis();
        builder.append(config.getPrintPrefix()).append(TimeUtil.convertMillisToMinSecFormat(totalTime));

        List<Map.Entry<String, Long>> values = NATURAL.equals(config.getPartOrdering()) ? new ArrayList<>(detailTimings.entrySet()) : detailTimings.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).collect(Collectors.toList());
        int partsToPrint = (config.getLimitParts() >= 0 ? Math.min(config.getLimitParts(), values.size()) : values.size()) - 1;

        if (partsToPrint > 0) {
            builder.append(" [");
            for (int x = 0; x <= partsToPrint; x++) {
                Map.Entry<String, Long> entry = values.get(x);
                appendPart(builder, entry.getKey(), entry.getValue(), totalTime);
                if (x != partsToPrint) {
                    builder.append(" | ");
                }
            }

            if (config.isSkippedSummary() && (values.size() - 1) > partsToPrint) {
                long otherTotal = 0;
                for (int x = partsToPrint + 1; x < values.size(); x++) {
                    otherTotal += values.get(x).getValue();
                }
                builder.append(" | ");
                appendPart(builder, config.getSkippedPartName(), otherTotal, totalTime);
            }

            builder.append("]");
        }
        return builder.toString();
    }

    private void appendPart(StringBuilder builder, String part, long time, long totalTime) {
        if (config.isPartPercentage()) {
            builder.append((int) ((time / (double) totalTime) * 100.0)).append("% ");
        }
        builder.append(part);
        if (config.isPartTimings()) {
            builder.append(": ").append(TimeUtil.convertMillisToFormatted(time));
        }
    }

    /**
     * internal timing details - unmodifiable map
     */
    public Map<String, Long> details() {
        return Collections.unmodifiableMap(detailTimings);
    }

    /**
     * current total millis
     */
    public Long totalMillis() {
        // used to get last part that has not been finished
        long additional = System.currentTimeMillis() - lastLogged;
        return detailTimings.values().stream().mapToLong(Long::longValue).sum() + additional;
    }

}
