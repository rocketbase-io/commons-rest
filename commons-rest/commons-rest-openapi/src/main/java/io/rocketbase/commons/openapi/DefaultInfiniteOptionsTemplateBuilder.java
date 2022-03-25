package io.rocketbase.commons.openapi;

public class DefaultInfiniteOptionsTemplateBuilder implements InfiniteOptionsTemplateBuilder{
    @Override
    public String buildQueryOptions(String returnType) {
        return "createInfiniteOptions({ staleTime: {{ method.staleTime }} * 1000, ...options })";
    }
}
