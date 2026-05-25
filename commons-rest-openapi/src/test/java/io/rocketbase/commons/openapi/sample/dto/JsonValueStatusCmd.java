package io.rocketbase.commons.openapi.sample.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotNull;

/**
 * Test fixture: a command whose enum field exposes lowercase wire values via
 * {@code @JsonValue} (common pattern when the Java constant names are uppercase but
 * the JSON contract is lowercase). The Zod generator must emit those wire values
 * inside {@code z.enum([...])}, not the bare Java constant names.
 */
public class JsonValueStatusCmd {

    @NotNull
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        ACTIVE("active"),
        DISABLED("disabled");

        private final String wire;

        Status(String wire) {
            this.wire = wire;
        }

        @JsonValue
        public String getWire() {
            return wire;
        }
    }
}
