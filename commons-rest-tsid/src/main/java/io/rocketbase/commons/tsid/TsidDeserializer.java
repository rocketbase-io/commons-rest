package io.rocketbase.commons.tsid;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.hypersistence.tsid.TSID;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class TsidDeserializer extends StdDeserializer<TSID> {

    public TsidDeserializer() {
        super(TSID.class);
    }


    @Override
    public TSID deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String value = jsonParser.getValueAsString();
        if (StringUtils.hasText(value) && TSID.isValid(value)) {
            return TSID.from(value);
        }
        return null;
    }
}
