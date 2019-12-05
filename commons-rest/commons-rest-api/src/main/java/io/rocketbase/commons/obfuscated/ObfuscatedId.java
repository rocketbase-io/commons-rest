package io.rocketbase.commons.obfuscated;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ObfuscatedIdSerializer.class)
public interface ObfuscatedId {

    String getObfuscated();

    Long getId();

}
