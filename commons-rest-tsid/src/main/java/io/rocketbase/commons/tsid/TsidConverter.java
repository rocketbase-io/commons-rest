package io.rocketbase.commons.tsid;

import io.hypersistence.tsid.TSID;
import io.rocketbase.commons.exception.TsidDecodeException;
import org.springframework.core.convert.converter.Converter;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

public class TsidConverter implements Converter<String, TSID> {

    private final boolean invalidAllowed;

    public TsidConverter() {
        this(false);
    }

    public TsidConverter(boolean invalidAllowed) {
        this.invalidAllowed = invalidAllowed;
    }

    /**
     * missing/blank values convert to null - invalid values raise {@link TsidDecodeException}
     * unless {@code tsid.invalid.allowed} is enabled (then they turn into null as well)
     */
    @Nullable
    @Override
    public TSID convert(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (TSID.isValid(value)) {
            return TSID.from(value);
        }
        if (invalidAllowed) {
            return null;
        }
        throw new TsidDecodeException();
    }
}
