package io.rocketbase.commons.tsid;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.hypersistence.tsid.TSID;

import java.io.IOException;

public class TsidSerializer extends StdSerializer<TSID> {

    public TsidSerializer() {
        super(TSID.class);
    }


    @Override
    public void serialize(TSID tsid, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(tsid.toString());
    }
}
