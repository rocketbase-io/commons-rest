package io.rocketbase.sample.converter;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = "spring",
        uses = {ObfuscatedIdMapper.class}
)
public interface CentralConfig {
}
