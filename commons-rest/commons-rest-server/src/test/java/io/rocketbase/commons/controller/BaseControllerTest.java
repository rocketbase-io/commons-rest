package io.rocketbase.commons.controller;

import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.StreamSupport;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class BaseControllerTest {

    @Test
    public void parsePageRequestWithDefaults() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();

        // when
        Pageable request = getTestController().parsePageRequest(map);

        // then
        assertThat(request, notNullValue());
        assertThat(request.getPageSize(), equalTo(BaseController.DEFAULT_PAGE_SIZE));
        assertThat(request.getPageNumber(), equalTo(0));
    }

    @Test
    public void parsePageRequestWithParams() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        int totalCount = 111;
        int page = 3;
        map.add("pageSize", String.valueOf(totalCount));
        map.add("page", String.valueOf(page));

        // when
        Pageable request = getTestController().parsePageRequest(map);

        // then
        assertThat(request, notNullValue());
        assertThat(request.getPageSize(), equalTo(totalCount));
        assertThat(request.getPageNumber(), equalTo(page));
    }

    @Test
    public void parsePageRequestWithoutParams() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        Sort sort = Sort.by("id");

        // when
        Pageable request = getTestController().parsePageRequest(map, sort);

        // then
        assertThat(request, notNullValue());
        assertThat(request.getPageSize(), equalTo(BaseController.DEFAULT_PAGE_SIZE));
        assertThat(request.getPageNumber(), equalTo(0));
        assertThat(request.getSort(), equalTo(sort));
    }

    @Test
    public void parsePageRequestWithNegative() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        map.add("pageSize", String.valueOf(-1));
        map.add("page", String.valueOf(-1));

        // when
        Pageable request = getTestController().parsePageRequest(map);

        // then
        assertThat(request, notNullValue());
        assertThat(request.getPageSize(), equalTo(BaseController.DEFAULT_MIN_PAGE_SIZE));
        assertThat(request.getPageNumber(), equalTo(0));
    }

    @Test
    public void parseSort() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        map.add("sort", "foo,desc");
        map.add("sort", "bla");
        map.add("sort", "invalid,up");

        // when
        Sort sort = getTestController().parseSort(map, "sort");

        // then
        assertThat(sort.getOrderFor("foo"), equalTo(Sort.Order.desc("foo")));
        assertThat(sort.getOrderFor("bla"), equalTo(Sort.Order.asc("bla")));
        assertThat(StreamSupport.stream(sort.spliterator(), false).count(), equalTo(2L));
    }

    @Test
    public void parsePageRequestWithOnlyInvalidSorting() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        map.add("sort", "foo,ddd");
        map.add("sort", "bla,as");
        map.add("sort", "");

        // when
        Pageable request = getTestController().parsePageRequest(map);

        // then
        assertThat(request, notNullValue());
        assertThat(request.getSort(), equalTo(Sort.unsorted()));
    }

    @Test
    public void parseInteger() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        map.add("id", String.valueOf(100));

        // when
        Integer value = getTestController().parseInteger(map, "id", null);

        // then
        assertThat(value, notNullValue());
        assertThat(value, equalTo(100));
    }

    @Test
    public void parseIntegerInvalid() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        map.add("id", "abcd");

        // when
        Integer value = getTestController().parseInteger(map, "id", null);
        Integer valueWithDefault = getTestController().parseInteger(map, "id", -1);

        // then
        assertThat(value, nullValue());
        assertThat(valueWithDefault, equalTo(-1));
    }

    @Test
    public void parseLong() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        Long id = 438324924924024432L;
        map.add("id", String.valueOf(id));

        // when
        Long value = getTestController().parseLong(map, "id", null);

        // then
        assertThat(value, notNullValue());
        assertThat(value, equalTo(id));
    }

    @Test
    public void parseBoolean() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        map.add("ok", "true");
        map.add("no", "0");
        map.add("yes", "yes");
        map.add("off", "off");

        // when
        Boolean ok = getTestController().parseBoolean(map, "ok", null);
        Boolean no = getTestController().parseBoolean(map, "no", null);
        Boolean yes = getTestController().parseBoolean(map, "yes", null);
        Boolean off = getTestController().parseBoolean(map, "off", null);

        // then
        assertThat(ok, equalTo(true));
        assertThat(no, equalTo(false));
        assertThat(yes, equalTo(true));
        assertThat(off, equalTo(false));
    }

    @Test
    public void parseLocalDate() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        LocalDate date = LocalDate.of(2022, 9, 5);
        map.add("v1", "2022-09-05");
        map.add("v2", "05/09/2022");
        map.add("v3", "05.09.2022");
        map.add("invalid", "20220905");


        // when
        LocalDate v1 = getTestController().parseLocalDate(map, "v1", null);
        LocalDate v2 = getTestController().parseLocalDate(map, "v2", null);
        LocalDate v3 = getTestController().parseLocalDate(map, "v3", null);
        LocalDate invalid = getTestController().parseLocalDate(map, "invalid", null);
        LocalDate defaultValue = LocalDate.now();
        LocalDate invalidWithDefault = getTestController().parseLocalDate(map, "invalid", defaultValue);


        // then
        assertThat(v1, equalTo(date));
        assertThat(v2, equalTo(date));
        assertThat(v3, equalTo(date));
        assertThat(invalid, nullValue());
        assertThat(invalidWithDefault, equalTo(defaultValue));
    }

    @Test
    public void parseLocalTime() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        LocalTime time = LocalTime.of(10, 15, 33);
        map.add("v1", "10:15");
        map.add("v2", "10:15:33");
        map.add("invalid", "121234");


        // when
        LocalTime v1 = getTestController().parseLocalTime(map, "v1", null);
        LocalTime v2 = getTestController().parseLocalTime(map, "v2", null);
        LocalTime invalid = getTestController().parseLocalTime(map, "invalid", null);
        LocalTime defaultValue = LocalTime.now();
        LocalTime invalidWithDefault = getTestController().parseLocalTime(map, "invalid", defaultValue);


        // then
        assertThat(v1, equalTo(time.withSecond(0)));
        assertThat(v2, equalTo(time));
        assertThat(invalid, nullValue());
        assertThat(invalidWithDefault, equalTo(defaultValue));
    }

    @Test
    public void parseLocalDateTime() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        LocalDateTime localDateTime = LocalDateTime.now();
        map.add("v1", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime));
        map.add("invalid", "121234");


        // when
        LocalDateTime v1 = getTestController().parseLocalDateTime(map, "v1", null);
        LocalDateTime invalid = getTestController().parseLocalDateTime(map, "invalid", null);
        LocalDateTime defaultValue = LocalDateTime.now();
        LocalDateTime invalidWithDefault = getTestController().parseLocalDateTime(map, "invalid", defaultValue);


        // then
        assertThat(v1, equalTo(localDateTime));
        assertThat(invalid, nullValue());
        assertThat(invalidWithDefault, equalTo(defaultValue));
    }

    @Test
    public void parseInstant() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        Instant instant = Instant.now();
        map.add("v1", instant.toString());
        map.add("invalid", "121234");


        // when
        Instant v1 = getTestController().parseInstant(map, "v1", null);
        Instant invalid = getTestController().parseInstant(map, "invalid", null);
        Instant defaultValue = Instant.now();
        Instant invalidWithDefault = getTestController().parseInstant(map, "invalid", defaultValue);


        // then
        assertThat(v1, equalTo(instant));
        assertThat(invalid, nullValue());
        assertThat(invalidWithDefault, equalTo(defaultValue));
    }

    @Test
    public void parseInstantWithMillisOrSec() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        Instant instant = Instant.now();
        instant = instant.minusNanos(instant.getNano());

        Instant defaultValue = Instant.now();

        map.add("v1", String.valueOf(instant.getEpochSecond()));
        map.add("invalid", "121234");
        map.add("millisTooOld", "912380400000");
        Instant _2005 = Instant.ofEpochSecond(1104537600L);
        _2005 = _2005.minusNanos(_2005.getNano());
        map.add("secs", String.valueOf(_2005.getEpochSecond()));


        // when
        Instant v1 = getTestController().parseInstant(map, "v1", null);
        Instant invalid = getTestController().parseInstant(map, "invalid", null);
        Instant invalidWithDefault = getTestController().parseInstant(map, "invalid", defaultValue);
        Instant millisTooOld = getTestController().parseInstant(map, "millisTooOld", null);
        Instant millisTooOldWithDefault = getTestController().parseInstant(map, "millisTooOld", defaultValue);
        Instant secs = getTestController().parseInstant(map, "secs", defaultValue);


        // then
        assertThat(v1.minusNanos(v1.getNano()), equalTo(instant));
        assertThat(invalid, nullValue());
        assertThat(invalidWithDefault, equalTo(defaultValue));
        assertThat(millisTooOld, nullValue());
        assertThat(millisTooOldWithDefault, equalTo(defaultValue));
        assertThat(secs.minusNanos(secs.getNano()), equalTo(_2005));
    }

    private BaseController getTestController() {
        return new TestController();
    }

    private class TestController implements BaseController {
    }
}