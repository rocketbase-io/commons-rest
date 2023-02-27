package io.rocketbase.commons.model;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EntityWithKeyValueTest {

    @Test
    public void testAddKeyValueString() {
        SampleEntity entity = new SampleEntity();
        entity.addKeyValue("k1[0]", "Test");
        assertThat(entity.getKeyValue("k1[0]"), equalTo("Test"));
    }

    @Test
    public void testAddKeyValueStringInvalid() {
        assertThrows(IllegalStateException.class, () -> {
            SampleEntity entity = new SampleEntity();
            entity.addKeyValue("k1öä:", "Test");
        });
    }

    @Test
    public void testAddKeyValueStringTooLong() {
        assertThrows(IllegalStateException.class, () -> {
            SampleEntity entity = new SampleEntity();
            entity.addKeyValue("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", "Test");
        });
    }

    @Test
    public void testAddKeyValueStringTooLongValue() {
        assertThrows(IllegalStateException.class, () -> {
            SampleEntity entity = new SampleEntity();
            entity.addKeyValue("abc", String.format("%0" + 256 + "d", 0));
        });
    }

    @Test
    public void testValidateKeyValues() {
        SampleEntity entity = new SampleEntity();
        entity.addKeyValue("k1", "Test");
        entity.validateKeyValues();
    }

    @Test
    public void testValidateKeyValuesInvalid() {
        assertThrows(IllegalStateException.class, () -> {
            SampleEntity entity = new SampleEntity();
            entity.addKeyValue("k1", "Test");
            entity.getKeyValues().put("k3", String.format("%0" + 256 + "d", 0));
            entity.validateKeyValues();
        });
    }

    @Test
    public void testAddKeyValueLong() {
        SampleEntity entity = new SampleEntity();
        entity.addKeyValue("k1", 1);
        assertThat(entity.getKeyValue("k1"), equalTo("1"));
    }

    @Test
    public void testAddKeyValueBoolean() {
        SampleEntity entity = new SampleEntity();
        entity.addKeyValue("k1", true);
        assertThat(entity.getKeyValue("k1"), equalTo("true"));
    }

    @Test
    public void testAddKeyValueCollectionString() {
        SampleEntity entity = new SampleEntity();
        entity.addKeyValue("k1", Arrays.asList("1", "2"));
        assertThat(entity.getKeyValue("k1"), equalTo("[\"1\",\"2\"]"));
        assertThat(entity.getKeyValueCollection("k1", Collections.emptyList()), equalTo(Arrays.asList("1", "2")));
        assertThat(entity.getKeyValue("k1", new TypeReference<Collection<Integer>>() {
        }, Collections.emptyList()), equalTo(Arrays.asList(1, 2)));
    }

    @Test
    public void testAddKeyValueCollectionInteger() {
        SampleEntity entity = new SampleEntity();
        entity.addKeyValue("k1", Arrays.asList(1L, 2L));
        assertThat(entity.getKeyValue("k1"), equalTo("[1,2]"));
        assertThat(entity.getKeyValueCollection("k1", Collections.emptyList()), equalTo(Arrays.asList("1", "2")));
        assertThat(entity.getKeyValue("k1", new TypeReference<Collection<Integer>>() {
        }, Collections.emptyList()), equalTo(Arrays.asList(1, 2)));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SampleEntity implements EntityWithKeyValue<SampleEntity> {
        private Map<String, String> keyValues = new HashMap<>();
    }
}