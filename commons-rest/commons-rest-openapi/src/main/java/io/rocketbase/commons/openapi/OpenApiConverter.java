package io.rocketbase.commons.openapi;

import com.google.common.collect.Sets;
import io.rocketbase.commons.util.Nulls;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Set;

public interface OpenApiConverter {

    /**
     * @param genericReturnType full classpath of java return class
     * @return should return a typescript value datatype
     */
    String getReturnType(String genericReturnType);

    /**
     * @param schema extra type infos
     * @return typescript valid type name
     */
    String convertType(Schema schema);

    /**
     * @param allTypes returnTypes, parameters etc.
     * @return unqiue set of typescriptTypes that should get imported
     */
    Set<String> getImportTypes(Set<String> allTypes);

    /**
     * used to remove package string from full classpath of class
     */
    default String removePackage(String value) {
        return Nulls.notNull(value).substring(Nulls.notNull(value).lastIndexOf(".") + 1);
    }

    default String removeRefPath(String value) {
        return value.substring(value.lastIndexOf("/") + 1);
    }

    default Set<String> getListTypes() {
        return Sets.newHashSet("java.util.List", "java.util.Collection", "java.util.Set");
    }

    default Set<String> getNativeTypes() {
        return Sets.newHashSet("string", "boolean", "long", "void", "any", "unknown", "integer", "number");
    }

    default boolean hasPageableParameter(List<String> parameterTypes) {
        return Nulls.notNull(parameterTypes)
                .contains("org.springframework.data.domain.Pageable");
    }
}
