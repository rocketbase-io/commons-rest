package io.rocketbase.sample.resource;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.sample.dto.company.CompanyRead;
import io.rocketbase.sample.dto.company.CompanyWrite;
import io.rocketbase.sample.model.CompanyEntity;
import io.rocketbase.sample.repository.mongo.CompanyRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CompanyResourceTest extends BaseIntegrationTest {

    @Resource
    private CompanyRepository companyRepository;


    @BeforeEach
    public void setup() {
        super.setup();
        companyRepository.deleteAll();
    }

    @AfterEach
    public void cleanup() throws Exception {
        companyRepository.deleteAll();
    }

    @Test
    public void shouldGetCompany() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());

        // when
        given()
                .when()
                .get("/api/company/{id}", company.getId())
                .then()
                .status(HttpStatus.OK)
                .body("id", is(company.getId()))
                .body("name", is(company.getName()))
                .body("url", is(company.getUrl()))
                .body("email", is(company.getEmail()));
    }

    @Test
    public void shouldNotGetUnknownCompany() throws Exception {
        given()
                .when()
                .get("/api/company/{id}", "notexisting")
                .then()
                .status(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFindAllCompanys() throws Exception {
        // given
        CompanyEntity Company = companyRepository.save(createDefaultCompany());

        // when
        PageableResult<CompanyRead> result = given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .get("/api/company")
                .getBody()
                .as(new TypeRef<PageableResult<CompanyRead>>() {
                });

        // then
        assertThat(result, notNullValue());
        assertThat(result.getTotalElements(), is(1L));
        assertThat(result.getPage(), is(0));
        assertThat(result.getTotalPages(), is(1));
        assertThat(result.getPageSize(), is(10));
        assertThat(result.getContent(), hasSize(1));
    }

    @Test
    public void shouldCreateCompany() throws Exception {
        // given
        CompanyWrite companyWrite = CompanyWrite.builder()
                .name("testcompany")
                .email("test@company.org")
                .url("https://company.org")
                .build();

        // when
        given()
                .contentType(ContentType.JSON)
                .body(companyWrite)
                .post("/api/company")
                .then()
                .status(HttpStatus.CREATED)
                .body("id", notNullValue())
                .body("name", is(companyWrite.getName()))
                .body("email", is(companyWrite.getEmail()))
                .body("url", is(companyWrite.getUrl()));
    }

    @Test
    public void shouldNotCreateInvalidCompany() throws Exception {
        // given
        CompanyWrite companyWrite = CompanyWrite.builder()
                .name("testcompany")
                .url("https://company.org")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(companyWrite)
                .post("/api/company")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("fields.email", not(emptyString()));

    }

    @Test
    public void shouldUpdateCompany() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());
        CompanyWrite companyWrite = CompanyWrite.builder()
                .name("testcompany all new")
                .email("test@company2.org")
                .url("https://company2.org")
                .build();


        // when
        CompanyRead companyRead = given()
                .contentType(ContentType.JSON)
                .body(companyWrite)
                .put("/api/company/{id}", company.getId())
                .getBody()
                .as(CompanyRead.class);

        // then
        assertThat(companyRead, notNullValue());
        assertThat(companyRead.getId(), is(company.getId()));

        assertThat(companyRead.getName(), is(companyWrite.getName()));
        assertThat(companyRead.getEmail(), is(companyWrite.getEmail()));
        assertThat(companyRead.getUrl(), is(companyWrite.getUrl()));
    }

    @Test
    public void shouldDeleteCompany() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());

        // when
        given().delete("/api/company/{id}", company.getId());

        // then
        assertThat(companyRepository.findById(company.getId()).isPresent(), equalTo(false));
    }

    @Test
    public void shouldSortAsc() {
        // given
        CompanyEntity aCompany = CompanyEntity.builder()
                .name("a-company")
                .email("a@company.org")
                .url("https://a-company.org")
                .build();
        companyRepository.saveAll(Arrays.asList(createDefaultCompany(), aCompany));


        // when
        PageableResult<CompanyRead> result = given()
                .queryParam("page", 0)
                .queryParam("size", 100)
                .queryParam("sort", "name")
                .get("/api/company")
                .getBody()
                .as(new TypeRef<PageableResult<CompanyRead>>() {
                });


        // then
        assertThat(result, notNullValue());
        assertThat(result.getTotalElements(), is(2L));
        assertThat(result.getPage(), is(0));
        assertThat(result.getTotalPages(), is(1));
        assertThat(result.getContent(), hasSize(2));
    }

    @Test
    public void shouldSortDesc() {
        // given
        CompanyEntity aCompany = CompanyEntity.builder()
                .name("a-company")
                .email("a@company.org")
                .url("https://a-company.org")
                .build();
        companyRepository.saveAll(Arrays.asList(createDefaultCompany(), aCompany));


        // when
        PageableResult<CompanyRead> result = given()
                .queryParam("page", 0)
                .queryParam("size", 100)
                .queryParam("sort", "name,desc")
                .get("/api/company")
                .getBody()
                .as(new TypeRef<PageableResult<CompanyRead>>() {
                });

        // then
        assertThat(result, notNullValue());
        assertThat(result.getTotalElements(), is(2L));
        assertThat(result.getPage(), is(0));
        assertThat(result.getTotalPages(), is(1));
        assertThat(result.getContent(), hasSize(2));
    }


    private CompanyEntity createDefaultCompany() {
        return CompanyEntity.builder()
                .name("testcompany")
                .email("test@company.org")
                .url("https://company.org")
                .build();
    }

}
