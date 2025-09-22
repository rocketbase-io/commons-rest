package io.rocketbase.commons.openapi;

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
     * in some cases you want to import dependencies from other packages then your code... then you need to implement this method by your-own
     *
     * @param type raw name of the java-class that is elected to be imported
     * @return a path or package to get it from
     */
    default String getImportPackage(String type) {
        return "../../model";
    }

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
        return Set.of("java.util.List", "java.util.Collection", "java.util.Set");
    }

    default Set<String> getNativeTypes() {
        return Set.of("string", "boolean", "long", "void", "any", "unknown", "integer", "number", "object", "file", "blob", "never", "record", "bigint", "symbol");
    }

    default Set<String> getJavaToUnknowns() {
        return Set.of("InputStreamResource", "InputStream", "Resource", "byte", "byte[]", "OutputStream", "Object");
    }

    default boolean hasPageableParameter(List<String> parameterTypes) {
        return Nulls.notNull(parameterTypes)
                .contains("org.springframework.data.domain.Pageable");
    }
}
