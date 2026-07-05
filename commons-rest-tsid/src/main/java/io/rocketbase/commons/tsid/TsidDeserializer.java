package io.rocketbase.commons.tsid;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import io.hypersistence.tsid.TSID;
import io.rocketbase.commons.exception.TsidDecodeException;
import org.springframework.util.StringUtils;

public class TsidDeserializer extends StdDeserializer<TSID> {

    private final boolean invalidAllowed;

    public TsidDeserializer() {
        this(false);
    }

    public TsidDeserializer(boolean invalidAllowed) {
        super(TSID.class);
        this.invalidAllowed = invalidAllowed;
    }

    /**
     * missing/blank values deserialize to null - invalid values raise {@link TsidDecodeException}
     * unless {@code tsid.invalid.allowed} is enabled (then they turn into null as well)
     */
    @Override
    public TSID deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        String value = jsonParser.getValueAsString();
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
