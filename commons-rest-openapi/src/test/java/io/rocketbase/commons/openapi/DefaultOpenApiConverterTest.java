package io.rocketbase.commons.openapi;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class DefaultOpenApiConverterTest {

    @Test
    void getReturnType() {
        // given
        DefaultOpenApiConverter converter = new DefaultOpenApiConverter();
        String genericReturnType = "java.util.List<io.productspace.base.common.Label>";
        // when
        String result = converter.getReturnType(genericReturnType);
        // then
        assertThat(result, equalTo("Label[]"));
    }
}