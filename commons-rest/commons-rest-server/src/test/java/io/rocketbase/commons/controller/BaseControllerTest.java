package io.rocketbase.commons.controller;

import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
        PageRequest request = getTestController().parsePageRequest(map);

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
        PageRequest request = getTestController().parsePageRequest(map);

        // then
        assertThat(request, notNullValue());
        assertThat(request.getPageSize(), equalTo(totalCount));
        assertThat(request.getPageNumber(), equalTo(page));
    }

    @Test
    public void parsePageRequestWithNegative() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        map.add("pageSize", String.valueOf(-1));
        map.add("page", String.valueOf(-1));

        // when
        PageRequest request = getTestController().parsePageRequest(map);

        // then
        assertThat(request, notNullValue());
        assertThat(request.getPageSize(), equalTo(BaseController.DEFAULT_MIN_PAGE_SIZE));
        assertThat(request.getPageNumber(), equalTo(0));
    }

    @Test
    public void parsePageRequestWithSorting() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        map.add("sort", "foo,desc");
        map.add("sort", "bla");
        map.add("sort", "invalid,up");

        // when
        PageRequest request = getTestController().parsePageRequest(map);

        // then
        assertThat(request, notNullValue());
        assertThat(request.getSort(), notNullValue());
        assertThat(request.getSort().getOrderFor("foo"), equalTo(new Sort.Order(Sort.Direction.DESC, "foo")));
        assertThat(request.getSort().getOrderFor("bla"), equalTo(new Sort.Order("bla")));
        assertThat(StreamSupport.stream(request.getSort().spliterator(), false).count(), equalTo(2L));
    }

    @Test
    public void parsePageRequestWithOnlyInvalidSorting() {
        // given
        MultiValueMap map = new LinkedMultiValueMap<String, String>();
        map.add("sort", "foo,ddd");
        map.add("sort", "bla,as");
        map.add("sort", "");

        // when
        PageRequest request = getTestController().parsePageRequest(map);

        // then
        assertThat(request, notNullValue());
        assertThat(request.getSort(), nullValue());
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

    private BaseController getTestController() {
        return new TestController();
    }

    private class TestController implements BaseController {
    }
}