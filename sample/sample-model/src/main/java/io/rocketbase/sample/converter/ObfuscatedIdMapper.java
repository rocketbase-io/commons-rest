package io.rocketbase.sample.converter;

import io.rocketbase.commons.obfuscated.IdObfuscator;
import io.rocketbase.commons.obfuscated.ObfuscatedId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObfuscatedIdMapper {

    private final IdObfuscator idObfuscator;

    public ObfuscatedId asObfuscatedId(Long id) {
        return id != null ? idObfuscator.obfuscate(id) : null;
    }
}
