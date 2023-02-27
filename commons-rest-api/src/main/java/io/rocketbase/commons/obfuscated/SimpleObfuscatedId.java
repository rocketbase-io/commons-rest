package io.rocketbase.commons.obfuscated;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = {"id", "obfuscated"})
public class SimpleObfuscatedId implements ObfuscatedId {

    private Long id;
    private String obfuscated;
}
