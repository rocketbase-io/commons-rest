package io.rocketbase.sample.converter;

import org.mapstruct.Builder;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = "spring",
        uses = {ObfuscatedIdMapper.class},
        builder = @Builder(disableBuilder = true)
)
public interface CentralConfig {
}
