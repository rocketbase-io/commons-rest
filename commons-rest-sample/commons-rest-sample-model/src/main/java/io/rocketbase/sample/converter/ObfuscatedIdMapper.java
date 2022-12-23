package io.rocketbase.sample.converter;

import io.rocketbase.commons.obfuscated.IdObfuscator;
import io.rocketbase.commons.obfuscated.ObfuscatedId;
import jakarta.annotation.Resource;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public abstract class ObfuscatedIdMapper {

    @Resource
    private IdObfuscator idObfuscator;

    public ObfuscatedId convert(long id) {
        return idObfuscator.obfuscate(id);
    }

}
