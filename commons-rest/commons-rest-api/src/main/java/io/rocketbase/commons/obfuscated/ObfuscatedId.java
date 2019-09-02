package io.rocketbase.commons.obfuscated;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ObfuscatedIdSerializer.class)
@JsonDeserialize(using = ObfuscatedIdDeserializer.class)
public interface ObfuscatedId {

    class ObfuscatedDecodeException extends RuntimeException {
    }

    String getObfuscated();

    Long getId();

}
