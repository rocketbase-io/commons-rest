package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.BadRequestException;
import io.rocketbase.sample.dto.employee.EmployeeRead;
import io.rocketbase.sample.dto.employee.EmployeeWrite;
import io.rocketbase.sample.model.CompanyEntity;
import io.rocketbase.sample.model.EmployeeEntity;
import io.rocketbase.sample.repository.mongo.CompanyRepository;
import io.rocketbase.sample.repository.mongo.EmployeeRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.AssertionErrors;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
public class EmployeeResourceTest {

    @LocalServerPort
    int randomServerPort;

    private EmployeeResource employeeResource;

    @Resource
    private CompanyRepository companyRepository;

    @Resource
    private EmployeeRepository employeeRepository;

    @Before
    public void setup() throws Exception {
        cleanup();
        employeeResource = new EmployeeResource(String.format("http://localhost:%d", randomServerPort));
    }

    @After
    public void cleanup() throws Exception {
        companyRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    public void shouldGetEmployee() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());
        EmployeeEntity employee = createDefaultEmployee();
        employee.setCompany(company);
        employee = employeeRepository.save(employee);

        // when
        Optional<EmployeeRead> data = employeeResource.getById(company.getId(), employee.getId());

        // then
        assertEmployeeSame(employee, data.get());
    }

    @Test
    public void shouldNotGetUnknownEmployee() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());
        EmployeeEntity employee = createDefaultEmployee();
        employee.setCompany(company);
        employee = employeeRepository.save(employee);

        // when
        Optional<EmployeeRead> data = employeeResource.getById(company.getId(), "notexisting");
        Optional<EmployeeRead> missMatch = employeeResource.getById("notexisting", employee.getId());

        // then
        assertThat(data, equalTo(Optional.empty()));
        assertThat(missMatch, equalTo(Optional.empty()));
    }

    @Test
    public void shouldFindAllEmployees() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());
        EmployeeEntity employee = createDefaultEmployee();
        employee.setCompany(company);
        employee = employeeRepository.save(employee);


        CompanyEntity otherCompany = createDefaultCompany();
        otherCompany.setName("Other");
        CompanyEntity other = companyRepository.save(otherCompany);
        EmployeeEntity otherEmployee = createDefaultEmployee();
        otherEmployee.setFirstName("Other");
        otherEmployee.setEmail("other@test.de");
        otherEmployee.setCompany(other);
        employeeRepository.save(otherEmployee);

        // when
        PageableResult<EmployeeRead> result = employeeResource.find(company.getId(), 0, 10);

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
        EmployeeRead employeeRead = employeeResource.create(company.getId(), employeeWrite);

        // then
        assertThat(employeeRead, notNullValue());
        assertThat(employeeRead.getId(), notNullValue());

        Optional<EmployeeEntity> employee = employeeRepository.findOneByCompanyIdAndId(company.getId(), employeeRead.getId());
        assertEmployeeSame(employee.get(), employeeRead);
    }

    @Test
    public void shouldGetStatusCode201OnCreate() {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());

        EmployeeWrite employeeWrite = EmployeeWrite.builder()
                .firstName("mew")
                .lastName("max")
                .email("new@test.de")
                .dateOfBirth(LocalDate.now())
                .female(false)
                .build();

        // when
        ResponseEntity<EmployeeRead> response = new RestTemplate().exchange(String.format("http://localhost:%d/api/company/%s/employee", randomServerPort, company.getId()),
                HttpMethod.POST,
                new HttpEntity<>(employeeWrite),
                EmployeeRead.class);

        // then
        assertThat(response, notNullValue());
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
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
        try {
            ResponseEntity<EmployeeRead> response = new RestTemplate().exchange(String.format("http://localhost:%d/api/company/%s/employee", randomServerPort, "unkown-id"),
                    HttpMethod.POST,
                    new HttpEntity<>(employeeWrite),
                    EmployeeRead.class);

            // then
        } catch (HttpClientErrorException httpClientError) {
            assertThat(httpClientError.getRawStatusCode(), is(HttpStatus.NOT_FOUND.value()));
        }
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
        try {
            EmployeeRead employeeRead = employeeResource.create(company.getId(), employeeWrite);

            // then
            AssertionErrors.fail("should not create invalid employee");
        } catch (BadRequestException ex) {
            ErrorResponse errorResponse = ex.getErrorResponse();
            assertThat(errorResponse, notNullValue());
            assertThat(errorResponse.getFields(), hasKey("email"));
            assertThat(errorResponse.getFirstFieldValue("email"), not(emptyString()));
        }

    }

    @Test
    public void shouldUpdateEmployee() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());
        EmployeeEntity employee = createDefaultEmployee();
        employee.setCompany(company);
        employee = employeeRepository.save(employee);

        EmployeeWrite employeeWrite = EmployeeWrite.builder()
                .firstName("max")
                .lastName("Müller")
                .email("max@test.de")
                .dateOfBirth(LocalDate.now())
                .female(false)
                .build();


        // when
        EmployeeRead employeeRead = employeeResource.update(company.getId(), employee.getId(), employeeWrite);

        // then
        assertThat(employeeRead, notNullValue());
        assertThat(employeeRead.getId(), is(employee.getId()));

        assertThat(employeeRead.getFirstName(), is(employeeWrite.getFirstName()));
        assertThat(employeeRead.getLastName(), is(employeeWrite.getLastName()));
        assertThat(employeeRead.getEmail(), is(employeeWrite.getEmail()));

        employee = employeeRepository.findById(employeeRead.getId()).get();
        assertEmployeeSame(employee, employeeRead);
    }

    @Test
    public void shouldDeleteCompany() throws Exception {
        // given
        CompanyEntity company = companyRepository.save(createDefaultCompany());
        EmployeeEntity employee = createDefaultEmployee();
        employee.setCompany(company);
        employee = employeeRepository.save(employee);

        // when
        employeeResource.delete(company.getId(), employee.getId());

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
        assertThat(data.getCompany().getId(), is(entity.getCompany().getId()));
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
                .company(createDefaultCompany())
                .dateOfBirth(LocalDate.now().minusYears(5))
                .female(true)
                .build();
    }


}
