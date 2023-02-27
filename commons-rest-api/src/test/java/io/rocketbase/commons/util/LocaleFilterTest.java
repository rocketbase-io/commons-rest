package io.rocketbase.commons.util;


import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class LocaleFilterTest {

    @Test
    public void findClosestExact() {
        // given
        Map<Locale, String> valueMap = new HashMap<>();
        valueMap.put(Locale.GERMANY, "Moin moin");
        valueMap.put(Locale.GERMAN, "Hallo");
        valueMap.put(Locale.FRANCE, "Salute");
        // when
        Map.Entry<Locale, String> result = LocaleFilter.findClosest(Locale.GERMANY, valueMap);
        // then
        assertThat(result, notNullValue());
        assertThat(result.getValue(), equalTo("Moin moin"));
    }

    @Test
    public void findClosestViaLanguage() {
        // given
        Map<Locale, String> valueMap = new HashMap<>();
        valueMap.put(Locale.GERMANY, "Moin moin");
        valueMap.put(Locale.FRANCE, "Salute");
        // when
        Map.Entry<Locale, String> result = LocaleFilter.findClosest(Locale.GERMAN, valueMap);
        // then
        assertThat(result, notNullValue());
        assertThat(result.getValue(), equalTo("Moin moin"));
    }

    @Test
    public void findClosestViaFallbackExact() {
        // given
        Map<Locale, String> valueMap = new HashMap<>();
        valueMap.put(Locale.GERMANY, "Moin moin");
        valueMap.put(Locale.GERMAN, "Hallo");
        valueMap.put(Locale.FRANCE, "Salute");
        // when
        Map.Entry<Locale, String> result = LocaleFilter.findClosest(Locale.CHINESE, valueMap, Locale.GERMANY);
        // then
        assertThat(result, notNullValue());
        assertThat(result.getValue(), equalTo("Moin moin"));
    }

    @Test
    public void findClosestViaFallbackLanguage() {
        // given
        Map<Locale, String> valueMap = new HashMap<>();
        valueMap.put(Locale.GERMANY, "Moin moin");
        valueMap.put(Locale.FRANCE, "Salute");
        // when
        Map.Entry<Locale, String> result = LocaleFilter.findClosest(Locale.CHINESE, valueMap, Locale.GERMAN);
        // then
        assertThat(result, notNullValue());
        assertThat(result.getValue(), equalTo("Moin moin"));
    }

    @Test
    public void findClosestViaFallbackRootLanguage() {
        // given
        Map<Locale, String> valueMap = new HashMap<>();
        valueMap.put(Locale.GERMANY, "Moin moin");
        valueMap.put(Locale.ROOT, "Hello");
        // when
        Map.Entry<Locale, String> result = LocaleFilter.findClosest(Locale.CHINESE, valueMap, Locale.FRENCH);
        // then
        assertThat(result, notNullValue());
        assertThat(result.getValue(), equalTo("Hello"));
    }

}