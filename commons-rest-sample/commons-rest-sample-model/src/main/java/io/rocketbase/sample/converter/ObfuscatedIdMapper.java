package io.rocketbase.sample.converter;

import io.rocketbase.commons.obfuscated.IdObfuscator;
import io.rocketbase.commons.obfuscated.ObfuscatedId;
import org.mapstruct.Mapper;

import javax.annotation.Resource;

@Mapper(componentModel = "spring")
public abstract class ObfuscatedIdMapper {

    @Resource
    private IdObfuscator idObfuscator;

    public ObfuscatedId convert(long id) {
        return idObfuscator.obfuscate(id);
    }

}
