package io.rocketbase.commons.tsid;

import io.hypersistence.tsid.TSID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

public class TsidConverter implements Converter<String, TSID> {

    @Nullable
    @Override
    public TSID convert(String value) {
        return TSID.isValid(value) ? TSID.from(value) : null;
    }
}
