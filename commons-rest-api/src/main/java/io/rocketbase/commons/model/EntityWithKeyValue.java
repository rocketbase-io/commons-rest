package io.rocketbase.commons.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.LinkedHashMap;

public interface EntityWithKeyValue<T> extends HasKeyValue {

    /**
     * @param key   max length of 50 characters (Allowed key chars are a-Z, 0-9 and _-.#)<br>
     *              key with _ as prefix will not get displayed in REST_API
     * @param value max length of 255 characters
     * @return itself for fluent api
     */
    @SuppressWarnings({"rawtypes", "cast"})
    default T addKeyValue(String key, String value) {
        checkKeyValue(key, value);
        if (getKeyValues() == null) {
            setKeyValues(new LinkedHashMap<>());
        }
        getKeyValues().put(key, value);
        return (T) this;
    }

    /**
     * @param key   max length of 50 characters (Allowed key chars are a-Z, 0-9 and _-.#)<br>
     *              key with _ as prefix will not get displayed in REST_API
     * @param value will get converted to String
     * @return itself for fluent api
     */
    default T addKeyValue(String key, Number value) {
        return addKeyValue(key, String.valueOf(value));
    }

    /**
     * @param key   max length of 50 characters (Allowed key chars are a-Z, 0-9 and _-.#)<br>
     *              key with _ as prefix will not get displayed in REST_API
     * @param value will get converted to String
     * @return itself for fluent api
     */
    default T addKeyValue(String key, Boolean value) {
        return addKeyValue(key, String.valueOf(value));
    }

    /**
     * @param key    max length of 50 characters (Allowed key chars are a-Z, 0-9 and _-.#)<br>
     *               key with _ as prefix will not get displayed in REST_API
     * @param values will get converted via ObjectMapper as json array
     * @return itself for fluent api
     */
    @SneakyThrows
    default T addKeyValue(String key, Collection values) {
        return addKeyValue(key, new ObjectMapper().findAndRegisterModules().writeValueAsString(values));
    }

    default void removeKeyValue(String key) {
        if (getKeyValues() == null) {
            return;
        }
        getKeyValues().remove(key);
    }

    /**
     * validate key and value
     *
     * @param key   not empty and max 50 chars<br>
     *              Allowed key chars are a-Z, 0-9 and _-.#[]
     * @param value maximum length 255 chars
     */
    default void checkKeyValue(String key, String value) {
        Assert.hasText(key, "Key must not be empty");
        Assert.state(key.length() <= 50, "Key is too long - at least 50 chars");
        Assert.state(key.matches("[a-zA-Z0-9_\\-\\.\\#\\[\\]]+"), "Allowed key chars are a-Z, 0-9 and _-.#[]");
        if (value != null) {
            Assert.state(value.length() <= 255, "Value is too long - at least 255 chars");
        }
    }

    /**
     * validate all keyValues<br>
     * should be triggered before storing in database
     */
    default void validateKeyValues() {
        if (getKeyValues() == null) {
            return;
        }
        getKeyValues().forEach(this::checkKeyValue);
    }
}
