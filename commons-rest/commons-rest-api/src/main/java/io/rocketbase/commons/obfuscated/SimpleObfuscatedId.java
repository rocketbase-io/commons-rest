package io.rocketbase.commons.obfuscated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SimpleObfuscatedId implements ObfuscatedId {

    private Long id;
    private String obfuscated;
}
