package io.rocketbase.sample.dto.customer;

import io.rocketbase.commons.obfuscated.ObfuscatedId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRead {

    private ObfuscatedId id;

    private String name;

}
