package io.rocketbase.commons.tsid;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.hypersistence.tsid.TSID;

import java.util.List;
import java.util.Map;

public class TsidModule extends SimpleModule {

    public TsidModule() {
        super(TsidModule.class.getSimpleName(), new Version(1, 0, 0, null, null, null),
                Map.of(TSID.class, new TsidDeserializer()),
                List.of(new TsidSerializer()));
    }
}
