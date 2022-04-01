package io.rocketbase.commons.openapi;

public interface InfiniteOptionsTemplateBuilder {

    String buildQueryOptions(OpenApiControllerMethodExtraction method);

    String buildQueryParams(OpenApiControllerMethodExtraction method);
}
