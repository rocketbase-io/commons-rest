package io.rocketbase.commons.tsid;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import io.hypersistence.tsid.TSID;
import org.springframework.util.StringUtils;

public class TsidDeserializer extends StdDeserializer<TSID> {

    public TsidDeserializer() {
        super(TSID.class);
    }


    @Override
    public TSID deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        String value = jsonParser.getValueAsString();
        if (StringUtils.hasText(value) && TSID.isValid(value)) {
            return TSID.from(value);
        }
        return null;
    }
}
