package io.rocketbase.commons.openapi;

public class DefaultInfiniteOptionsTemplateBuilder implements InfiniteOptionsTemplateBuilder {

    @Override
    public String buildQueryOptions(OpenApiControllerMethodExtraction method) {
        return "...createPaginationOptions()";
    }

    @Override
    public String buildQueryParams(OpenApiControllerMethodExtraction method) {
        return "page: pageParam";
    }
}
