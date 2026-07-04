package io.rocketbase.commons.tsid;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import io.hypersistence.tsid.TSID;

public class TsidSerializer extends StdSerializer<TSID> {

    public TsidSerializer() {
        super(TSID.class);
    }


    @Override
    public void serialize(TSID tsid, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
        jsonGenerator.writeString(tsid.toLowerCase());
    }
}
