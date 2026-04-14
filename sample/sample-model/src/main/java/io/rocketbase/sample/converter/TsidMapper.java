package io.rocketbase.sample.converter;

import io.hypersistence.tsid.TSID;
import org.springframework.stereotype.Component;

@Component
public class TsidMapper {

    public TSID asTsid(Long id) {
        return id != null ? TSID.from(id) : null;
    }
}
