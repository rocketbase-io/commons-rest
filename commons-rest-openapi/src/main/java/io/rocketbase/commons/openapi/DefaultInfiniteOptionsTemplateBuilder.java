package io.rocketbase.commons.openapi;

import io.rocketbase.commons.util.Nulls;

public class DefaultInfiniteOptionsTemplateBuilder implements InfiniteOptionsTemplateBuilder {

    @Override
    public String buildQueryOptions(OpenApiControllerMethodExtraction method)  {
        return "...createPaginationOptions()";
    }

    @Override
    public String buildQueryParams(OpenApiControllerMethodExtraction method) {
        return "page: pageParam";
    }
}
