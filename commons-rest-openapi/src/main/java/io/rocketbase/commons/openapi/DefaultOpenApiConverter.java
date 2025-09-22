package io.rocketbase.commons.openapi;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.rocketbase.commons.util.Nulls;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.*;
import java.util.stream.Collectors;

import static io.rocketbase.commons.openapi.OpenApiControllerMethodExtraction.MULTIPART_TYPESCRIPT;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultOpenApiConverter implements OpenApiConverter {
    private static final Set<String> COLLECTION_TYPES = Set.of(
            "java.util.List", "java.util.ArrayList", "java.util.LinkedList",
            "java.util.Set", "java.util.HashSet", "java.util.LinkedHashSet", "java.util.SortedSet", "java.util.TreeSet",
            "java.util.Collection", "java.lang.Iterable",
            "java.util.Queue", "java.util.Deque", "java.util.ArrayDeque"
    );

    private static final Set<String> STREAM_TYPES = Set.of(
            "java.util.stream.Stream"
    );

    private static final Set<String> OPTIONAL_TYPES = Set.of(
            "java.util.Optional", "java.util.OptionalInt", "java.util.OptionalLong", "java.util.OptionalDouble"
    );

    private static final Set<String> FUTURE_TYPES = Set.of(
            "java.util.concurrent.CompletableFuture", "java.util.concurrent.Future"
    );

    private static final Set<String> MAP_TYPES = Set.of(
            "java.util.Map", "java.util.HashMap", "java.util.LinkedHashMap",
            "java.util.SortedMap", "java.util.TreeMap"
    );

    private boolean startsWithAny(String type, Set<String> prefixes) {
        for (String p : prefixes) {
            if (type.startsWith(p)) return true;
        }
        return false;
    }

    private boolean isCollectionType(String type) {
        return startsWithAny(type, COLLECTION_TYPES);
    }

    private boolean isStreamType(String type) {
        return startsWithAny(type, STREAM_TYPES);
    }

    private boolean isOptionalType(String type) {
        return startsWithAny(type, OPTIONAL_TYPES);
    }

    private boolean isFutureType(String type) {
        return startsWithAny(type, FUTURE_TYPES);
    }

    private boolean isMapType(String type) {
        return startsWithAny(type, MAP_TYPES);
    }


    private List<String> splitTopLevelGenerics(String typeWithGenerics) {
        int lt = typeWithGenerics.indexOf('<');
        int gt = typeWithGenerics.lastIndexOf('>');
        if (lt < 0 || gt < 0 || gt < lt) return List.of();

        String inner = typeWithGenerics.substring(lt + 1, gt).trim();
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '<') {
                depth++;
                cur.append(c);
            } else if (c == '>') {
                depth--;
                cur.append(c);
            } else if (c == ',' && depth == 0) {
                parts.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) parts.add(cur.toString().trim());
        return parts;
    }

    private String normalizeWildcard(String t) {
        // "? extends X" / "? super X" → "X", "?" → "any"
        String s = t.trim();
        if (s.equals("?")) return "any";
        s = s.replaceAll("^\\?\\s+extends\\s+", "")
                .replaceAll("^\\?\\s+super\\s+", "");
        return s.trim();
    }

    private String toTsKeyType(String javaKeyTypeRaw) {
        String t = normalizeWildcard(javaKeyTypeRaw);
        String simple = removePackage(t);

        if (simple.equalsIgnoreCase("String") ||
                simple.equalsIgnoreCase("UUID") ||
                simple.equalsIgnoreCase("CharSequence")) {
            return "string";
        }
        Set<String> numberTypes = Set.of(
                "int", "Integer", "long", "Long", "double", "Double", "float", "Float", "short", "Short", "byte", "Byte",
                "BigInteger", "BigDecimal"
        );
        if (numberTypes.contains(simple)) {
            return "number";
        }
        if (simple.equalsIgnoreCase("boolean") || simple.equalsIgnoreCase("Boolean")) {
            return "string";
        }
        if (simple.endsWith("Enum")) {
            return "string";
        }
        if (simple.equalsIgnoreCase("Object")) {
            return "any";
        }
        return "string";
    }

    @Override
    public String getReturnType(String genericReturnType) {
        if (genericReturnType == null) return "unknown";
        if (genericReturnType.equalsIgnoreCase("java.lang.void")) return "void";
        if (genericReturnType.equalsIgnoreCase("java.lang.object")) return "any";

        // --- Map<K,V> -> Record<K,V> ---
        if (isMapType(genericReturnType)) {
            List<String> kv = splitTopLevelGenerics(genericReturnType);
            if (kv.size() != 2) return "Record<string, any>";
            String tsKey = toTsKeyType(kv.get(0));
            String tsVal = getReturnType(normalizeWildcard(kv.get(1)));
            return "Record<" + tsKey + ", " + tsVal + ">";
        }

        // --- Optional<T> -> T | null ---
        if (isOptionalType(genericReturnType)) {
            List<String> inner = splitTopLevelGenerics(genericReturnType);
            String t = inner.isEmpty() ? "unknown" : getReturnType(normalizeWildcard(inner.get(0)));
            // OptionalInt/Long/Double: keine Type-Arg -> number
            if (genericReturnType.startsWith("java.util.Optional") && inner.isEmpty()) {
                t = "number";
            }
            return t + " | null";
        }

        // --- CompletableFuture<T> / Future<T> -> Promise<T> ---
        if (isFutureType(genericReturnType)) {
            List<String> inner = splitTopLevelGenerics(genericReturnType);
            String t = inner.isEmpty() ? "unknown" : getReturnType(normalizeWildcard(inner.get(0)));
            return "Promise<" + t + ">";
        }

        // --- Collection-/Iterable-/List-/Set-/Queue-/Deque<T> -> T[] ---
        if (isCollectionType(genericReturnType)) {
            List<String> inner = splitTopLevelGenerics(genericReturnType);
            String t = inner.isEmpty() ? "unknown" : getReturnType(normalizeWildcard(inner.get(0)));
            return t + "[]";
        }

        // --- Stream<T> -> T[] (praktisch für API-Outputs) ---
        if (isStreamType(genericReturnType)) {
            List<String> inner = splitTopLevelGenerics(genericReturnType);
            String t = inner.isEmpty() ? "unknown" : getReturnType(normalizeWildcard(inner.get(0)));
            return t + "[]";
        }

        // --- dein bisheriger Default-Zweig ---
        String name = convertInfiniteReturnTypes(genericReturnType);

        // (Optional) Falls du weiterhin getListTypes() verwenden willst, darf dieser Block stehen bleiben.
        Optional<String> arrayType = getListTypes().stream()
                .filter(v -> genericReturnType.startsWith(v)).findFirst();
        if (arrayType.isPresent()) {
            for (String l : getListTypes()) {
                if (name.startsWith(l)) {
                    name = name.replace(l + "<", "").replaceAll("[\\>]$", "");
                    break;
                }
            }
        }

        if (name != null && name.contains("<")) {
            String genericCenter = name.substring(name.lastIndexOf("<") + 1).replace(">", "");
            if (genericCenter.equals("?")) {
                name = name.replace(genericCenter, "any");
            } else {
                name = name.replace(genericCenter, removePackage(checkAndAddUnionType(genericCenter)));
            }
        }
        return convertType(removePackage(checkAndAddUnionType(name))) + (arrayType.isPresent() ? "[]" : "");
    }

    /**
     * checks if given className is an unionType typescript related...
     */
    protected String checkAndAddUnionType(String name) {
        if (name != null && !name.contains("<")) {
            try {
                Class clazz = Class.forName(name);
                if ((clazz.isAnnotationPresent(JsonSubTypes.class) && clazz.isAnnotationPresent(JsonTypeInfo.class))) {
                    return name + "Union";
                }
            } catch (ClassNotFoundException cnf) {
            }
        }
        return name;
    }

    protected String convertInfiniteReturnTypes(String genericReturnType) {
        return genericReturnType.replace("io.rocketbase.commons.dto.PageableResult", "PageableResult");
    }

    @Override
    public String convertType(Schema schema) {
        if (schema == null) {
            return "unknown";
        }
        String type = schema.get$ref() != null ? removeRefPath(schema.get$ref()) : schema.getType();
        if (schema instanceof ArraySchema) {
            Schema<?> item = ((ArraySchema) schema).getItems();
            type = convertType(item.getType() != null ? item.getType() : removeRefPath(item.get$ref())) + "[]";
        } else {
            type = convertType(type);
        }
        return type;
    }

    protected String convertType(String type) {
        if (type == null) return null;

        // sanitize: hängende Generics-Zeichen entfernen
        type = type.trim().replace(">", "").replace("<", "").replace(",", "");

        // java.lang.Object / Object -> any
        if ("java.lang.Object".equalsIgnoreCase(type) || "Object".equalsIgnoreCase(type)) {
            return "any";
        }

        if (getNativeTypes().contains(type.toLowerCase())) type = type.toLowerCase();
        if ("Void".equalsIgnoreCase(type)) type = "void";

        if (Set.of("Integer", "Long", "Double", "Float", "Short", "BigDecimal", "BigInteger").contains(type)) {
            type = "number";
        }
        if ("Boolean".equalsIgnoreCase(type)) type = "boolean";

        if (type.endsWith("TSID") || "UUID".equalsIgnoreCase(type)) return "string";
        if (type.endsWith("JsonNode")) return "any";

        for (String java : getJavaToUnknowns()) {
            if (java.equalsIgnoreCase(type)) return "unknown";
        }
        return type;
    }

    @Override
    public Set<String> getImportTypes(Set<String> allTypes) {
        Set<String> result = new HashSet<>();
        for (String t : Nulls.notNull(allTypes)) {
            if (t == null) continue;
            // Arrays entfernen
            String base = t.replace("[]", "");

            // Wrapper/Generics entpacken
            base = convertImportWrappers(base, result);

            // Paket entfernen & Rest-Generics (falls noch) abtrennen
            base = removePackage(base).trim();
            int lt = base.indexOf('<');
            if (lt >= 0) base = base.substring(0, lt);
            base = base.replace(">", "").replace(",", "").trim();

            // ggf. Union-Erweiterung prüfen (auf Basis des bereinigten Namens)
            String unionBase = removePackage(checkAndAddUnionType(base)).trim();

            result.add(base);
            result.add(unionBase);
        }

        return result.stream()
                .map(this::convertType)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(v -> !getNativeTypes().contains(v.toLowerCase(Locale.ROOT))) // any, unknown, void, string, number, ...
                .filter(v -> {
                    String vl = v.toLowerCase(Locale.ROOT);
                    return !vl.equals("promise") && !vl.equals("record"); // globale Builtins nicht importieren
                })
                .filter(v -> !MULTIPART_TYPESCRIPT.equalsIgnoreCase(v))
                .collect(Collectors.toSet());
    }

    protected String convertImportWrappers(String type, Set<String> importTypes) {
        for (String p : COLLECTION_TYPES)
            if (type.startsWith(p + "<")) return type.replace(p + "<", "").replace(">", "");
        for (String p : STREAM_TYPES) if (type.startsWith(p + "<")) return type.replace(p + "<", "").replace(">", "");
        for (String p : OPTIONAL_TYPES) if (type.startsWith(p + "<")) return type.replace(p + "<", "").replace(">", "");
        for (String p : FUTURE_TYPES) if (type.startsWith(p + "<")) return type.replace(p + "<", "").replace(">", "");
        for (String p : MAP_TYPES) if (type.startsWith(p + "<")) return type.replace(p + "<", "").replace(">", "");
        if (type.startsWith("io.rocketbase.commons.dto.PageableResult<")) {
            return type.replace("io.rocketbase.commons.dto.PageableResult<", "").replace(">", "");
        }

        int lt = type.indexOf('<');
        if (lt >= 0) {
            return type.substring(0, lt);
        }
        return type;
    }

}
