package io.rocketbase.sample.resource;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.sample.BaseIntegrationTest;
import io.rocketbase.sample.dto.employee.EmployeeRead;
import io.rocketbase.sample.dto.employee.EmployeeWrite;
import io.rocketbase.sample.model.CompanyEntity;
import io.rocketbase.sample.model.EmployeeEntity;
import io.rocketbase.sample.repository.mongo.CompanyRepository;
import io.rocketbase.sample.repository.mongo.EmployeeRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EmployeeResourceTest extends BaseIntegrationTest {

    @Resource
    private CompanyRepository companyRepository;

    @Resource
    private EmployeeRepository employeeRepository;

    @BeforeEach
    public void setup() {
        super.setup();
        cleanup();
    }

    @AfterEach
    public void cleanup() {
        companyRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    public void shouldGetEmployee() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());
        EmployeeEntity employee = createDefaultEmployee();
        employee.setCompanyId(company.getId());
        employee = employeeRepository.save(employee);

        // when
        EmployeeRead data = given()
                .when()
                .get("/api/company/{parentId}/employee/{id}", company.getId(), employee.getId())
                .getBody()
                .as(EmployeeRead.class);

        // then
        assertEmployeeSame(employee, data);
    }

    @Test
    public void shouldNotGetUnknownEmployee() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());
        EmployeeEntity employee = createDefaultEmployee();
        employee.setCompanyId(company.getId());
        employee = employeeRepository.save(employee);

        // when
        given()
                .when()
                .get("/api/company/{parentId}/employee/{id}", company.getId(), "notexisting")
                .then()
                .status(HttpStatus.NOT_FOUND);

        given()
                .when()
                .get("/api/company/{parentId}/employee/{id}", "notexisting", employee.getId())
                .then()
                .status(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFindAllEmployees() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());
        EmployeeEntity employee = createDefaultEmployee();
        employee.setCompanyId(company.getId());
        employee = employeeRepository.save(employee);


        CompanyEntity otherCompany = createDefaultCompany();
        otherCompany.setName("Other");
        CompanyEntity other = companyRepository.save(otherCompany);
        EmployeeEntity otherEmployee = createDefaultEmployee();
        otherEmployee.setFirstName("Other");
        otherEmployee.setEmail("other@test.de");
        otherEmployee.setCompanyId(other.getId());
        employeeRepository.save(otherEmployee);

        // when
        PageableResult<EmployeeRead> result = given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/company/{parentId}/employee", company.getId())
                .getBody()
                .as(new TypeRef<PageableResult<EmployeeRead>>() {
                });

        // then
        assertThat(result, notNullValue());
        assertThat(result.getTotalElements(), is(1L));
        assertThat(result.getPage(), is(0));
        assertThat(result.getTotalPages(), is(1));
        assertThat(result.getPageSize(), is(10));
        assertThat(result.getContent(), hasSize(1));
        assertEmployeeSame(employee,
                result.getContent()
                        .get(0));
    }

    @Test
    public void shouldCreateEmployee() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());

        EmployeeWrite employeeWrite = EmployeeWrite.builder()
                .firstName("max")
                .lastName("Müller")
                .email("max@test.de")
                .dateOfBirth(LocalDate.now())
                .female(false)
                .build();

        // when
        given()
                .contentType(ContentType.JSON)
                .body(employeeWrite)
                .post("/api/company/{parentId}/employee", company.getId())
                .then()
                .status(HttpStatus.CREATED)
                .body("company.id", is(company.getId()))
                .body("firstName", is(employeeWrite.getFirstName()))
                .body("lastName", is(employeeWrite.getLastName()))
                .body("email", is(employeeWrite.getEmail()));
    }

    @Test
    public void shouldGetStatusCode404OnInvalidCreate() {
        // given
        EmployeeWrite employeeWrite = EmployeeWrite.builder()
                .firstName("mew")
                .lastName("max")
                .email("new@test.de")
                .dateOfBirth(LocalDate.now())
                .female(false)
                .build();

        // when
        given()
                .contentType(ContentType.JSON)
                .body(employeeWrite)
                .post("/api/company/{parentId}/employee", "unkown-id")
                .then()
                .status(HttpStatus.NOT_FOUND);
    }


    @Test
    public void shouldNotCreateInvalidEmployee() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());

        EmployeeWrite employeeWrite = EmployeeWrite.builder()
                .firstName("max")
                .lastName("Müller")
                .email("wrongEmail")
                .female(false)
                .build();

        // when
        given()
                .contentType(ContentType.JSON)
                .body(employeeWrite)
                .post("/api/company/{parentId}/employee", company.getId())
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("fields.email", not(emptyString()));
    }

    @Test
    public void shouldUpdateEmployee() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());
        EmployeeEntity employee = createDefaultEmployee();
        employee.setCompanyId(company.getId());
        employee = employeeRepository.save(employee);

        EmployeeWrite employeeWrite = EmployeeWrite.builder()
                .firstName("max")
                .lastName("Müller")
                .email("max@test.de")
                .dateOfBirth(LocalDate.now())
                .female(false)
                .build();


        // when
        given()
                .contentType(ContentType.JSON)
                .body(employeeWrite)
                .put("/api/company/{parentId}/employee/{id}", company.getId(), employee.getId())
                .then()
                .status(HttpStatus.OK)
                .body("id", is(employee.getId()))
                .body("company.id", is(company.getId()))
                .body("firstName", is(employeeWrite.getFirstName()))
                .body("lastName", is(employeeWrite.getLastName()))
                .body("email", is(employeeWrite.getEmail()));
    }

    @Test
    public void shouldDeleteCompany() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());
        EmployeeEntity employee = createDefaultEmployee();
        employee.setCompanyId(company.getId());
        employee = employeeRepository.save(employee);

        // when
        given()
                .delete("/api/company/{parentId}/employee/{id}", company.getId(), employee.getId());

        // then
        assertThat(employeeRepository.findById(employee.getId()).isPresent(), equalTo(false));
    }


    private void assertEmployeeSame(EmployeeEntity entity, EmployeeRead data) {
        assertThat(data, notNullValue());
        assertThat(data.getId(), is(entity.getId()));
        assertThat(data.getFirstName(), is(entity.getFirstName()));
        assertThat(data.getLastName(), is(entity.getLastName()));
        assertThat(data.getEmail(), is(entity.getEmail()));
        assertThat(data.getCompany(), notNullValue());
        assertThat(data.getCompany().getId(), is(entity.getCompanyId()));
        assertThat(data.getDateOfBirth(), is(entity.getDateOfBirth()));
        assertThat(data.isFemale(), is(entity.isFemale()));
    }


    private CompanyEntity createDefaultCompany() {
        return CompanyEntity.builder()
                .name("testcompany")
                .email("test@company.org")
                .url("https://company.org")
                .build();
    }

    private EmployeeEntity createDefaultEmployee() {
        return EmployeeEntity.builder()
                .firstName("firstname")
                .lastName("lastname")
                .email("test@test.de")
                .companyId(createDefaultCompany().getId())
                .dateOfBirth(LocalDate.now().minusYears(5))
                .female(true)
                .build();
    }


}
