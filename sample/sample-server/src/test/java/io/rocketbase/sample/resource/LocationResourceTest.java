package io.rocketbase.sample.resource;

import io.hypersistence.tsid.TSID;
import io.restassured.common.mapper.TypeRef;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.sample.BaseIntegrationTest;
import io.rocketbase.sample.dto.localtion.LocationRead;
import io.rocketbase.sample.model.LocationEntity;
import io.rocketbase.sample.repository.jpa.LocationRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LocationResourceTest extends BaseIntegrationTest {

    @Resource
    private LocationRepository locationRepository;


    @BeforeEach
    public void setup() {
        super.setup();
        locationRepository.deleteAll();
    }

    @AfterEach
    public void cleanup() throws Exception {
        locationRepository.deleteAll();
    }

    @Test
    public void shouldGetLocation() throws Exception {
        // given
        LocationEntity location = locationRepository.save(createDefaultLocation());

        // when
        String tsid = TSID.from(location.getId()).toString();
        given()
                .when()
                .get("/api/location/{id}", tsid)
                .then()
                .status(HttpStatus.OK)
                .body("id", is(tsid))
                .body("name", is(location.getName()))
                .body("street", is(location.getStreet()))
                .body("city", is(location.getCity()));
    }

    @Test
    public void shouldNotGetUnknownLocation() throws Exception {
        given()
                .when()
                .get("/api/location/{id}", "notexisting")
                .then()
                .status(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFindAllLocations() throws Exception {
        // given
        LocationEntity location = locationRepository.save(createDefaultLocation());

        // when
        PageableResult<LocationRead> result = given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .get("/api/location")
                .getBody()
                .as(new TypeRef<PageableResult<LocationRead>>() {
                });

        // then
        assertThat(result, notNullValue());
        assertThat(result.getTotalElements(), is(1L));
        assertThat(result.getPage(), is(0));
        assertThat(result.getTotalPages(), is(1));
        assertThat(result.getPageSize(), is(10));
        assertThat(result.getContent(), hasSize(1));
    }


    private LocationEntity createDefaultLocation() {
        return LocationEntity.builder()
                .name("testcompany")
                .street("street 123")
                .city("hamburg")
                .country("de")
                .build();
    }

}
