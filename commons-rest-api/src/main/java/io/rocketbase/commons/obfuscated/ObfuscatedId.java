package io.rocketbase.commons.obfuscated;

import tools.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ObfuscatedIdSerializer.class)
public interface ObfuscatedId {

    String getObfuscated();

    Long getId();

    default boolean isValid() {
        return getId() != null;
    }

}
