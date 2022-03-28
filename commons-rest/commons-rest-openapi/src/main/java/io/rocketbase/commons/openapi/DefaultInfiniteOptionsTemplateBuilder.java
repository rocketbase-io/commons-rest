package io.rocketbase.commons.openapi;

import io.rocketbase.commons.util.Nulls;

public class DefaultInfiniteOptionsTemplateBuilder implements InfiniteOptionsTemplateBuilder {

    @Override
    public String buildQueryOptions(OpenApiControllerMethodExtraction method) {
        if (Nulls.notNull(method.getStaleTime(), 0) > 0) {
            return String.format("createInfiniteOptions({ staleTime: %d * 1000, ...options })", method.getStaleTime());
        }
        return "createInfiniteOptions({ options })";
    }
}
