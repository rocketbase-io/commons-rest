package io.rocketbase.commons.obfuscated;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SimpleObfuscatedId implements ObfuscatedId {

    private final Long id;
    private final String obfuscated;

}
