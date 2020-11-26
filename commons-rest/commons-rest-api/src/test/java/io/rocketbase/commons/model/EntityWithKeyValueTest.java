package io.rocketbase.commons.model;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EntityWithKeyValueTest {

    @Test
    public void testAddKeyValueString() {
        SampleEntity entity = new SampleEntity();
        entity.addKeyValue("k1", "Test");
        assertThat(entity.getKeyValue("k1"), equalTo("Test"));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddKeyValueStringInvalid() {
        SampleEntity entity = new SampleEntity();
        entity.addKeyValue("k1öä:", "Test");
    }

    @Test(expected = IllegalStateException.class)
    public void testAddKeyValueStringTooLong() {
        SampleEntity entity = new SampleEntity();
        entity.addKeyValue("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", "Test");
    }

    @Test(expected = IllegalStateException.class)
    public void testAddKeyValueStringTooLongValue() {
        SampleEntity entity = new SampleEntity();
        entity.addKeyValue("abc", String.format("%0" + 513 + "d", 0));
    }

    @Test
    public void testValidateKeyValues() {
        SampleEntity entity = new SampleEntity();
        entity.addKeyValue("k1", "Test");
        entity.validateKeyValues();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateKeyValuesInvalid() {
        SampleEntity entity = new SampleEntity();
        entity.addKeyValue("k1", "Test");
        entity.getKeyValues().put("k3", String.format("%0" + 513 + "d", 0));
        entity.validateKeyValues();
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
        entity.addKeyValue("k1", Arrays.asList(1L,2L));
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