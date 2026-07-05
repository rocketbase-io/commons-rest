package io.rocketbase.commons.tsid;

import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleModule;
import io.hypersistence.tsid.TSID;

public class TsidModule extends SimpleModule {

    public TsidModule() {
        this(false);
    }

    public TsidModule(boolean invalidAllowed) {
        super(TsidModule.class.getSimpleName(), new Version(1, 0, 0, null, null, null));
        addSerializer(TSID.class, new TsidSerializer());
        addDeserializer(TSID.class, new TsidDeserializer(invalidAllowed));
    }
}
