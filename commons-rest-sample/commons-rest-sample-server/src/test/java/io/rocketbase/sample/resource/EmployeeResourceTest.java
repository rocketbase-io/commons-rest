package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.BadRequestException;
import io.rocketbase.commons.resource.BasicResponseErrorHandler;
import io.rocketbase.sample.dto.employee.EmployeeRead;
import io.rocketbase.sample.dto.employee.EmployeeWrite;
import io.rocketbase.sample.model.Company;
import io.rocketbase.sample.model.Employee;
import io.rocketbase.sample.repository.CompanyRepository;
import io.rocketbase.sample.repository.EmployeeRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.AssertionErrors;
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

    @Resource
    private EmployeeResource employeeResource;

    @Resource
    private CompanyRepository companyRepository;

    @Resource
    private EmployeeRepository employeeRepository;

    @Resource
    private TestRestTemplate testRestTemplate;

    @Before
    public void setup() throws Exception {
        RestTemplate restTemplate = testRestTemplate.getRestTemplate();
        restTemplate.setErrorHandler(new BasicResponseErrorHandler());
        employeeResource.setRestTemplate(restTemplate);
        cleanup();
    }

    @After
    public void cleanup() throws Exception {
        companyRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    public void shouldGetEmployee() throws Exception {
        // given
        Company company = companyRepository.save(createDefaultCompany());
        Employee employee = createDefaultEmployee();
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
        Company company = companyRepository.save(createDefaultCompany());
        Employee employee = createDefaultEmployee();
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
        Company company = companyRepository.save(createDefaultCompany());
        Employee employee = createDefaultEmployee();
        employee.setCompany(company);
        employee = employeeRepository.save(employee);


        Company otherCompany = createDefaultCompany();
        otherCompany.setName("Other");
        Company other = companyRepository.save(otherCompany);
        Employee otherEmployee = createDefaultEmployee();
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
        Company company = companyRepository.save(createDefaultCompany());

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

        Employee employee = employeeRepository.findOneByCompanyIdAndId(company.getId(), employeeRead.getId());
        assertEmployeeSame(employee, employeeRead);
    }

    @Test
    public void shouldNotCreateInvalidEmployee() throws Exception {
        // given
        Company company = companyRepository.save(createDefaultCompany());

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
            assertThat(errorResponse.getFields(), hasEntry(is("email"), not(isEmptyString())));
        }

    }

    @Test
    public void shouldUpdateEmployee() throws Exception {
        // given
        Company company = companyRepository.save(createDefaultCompany());
        Employee employee = createDefaultEmployee();
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
        Company company = companyRepository.save(createDefaultCompany());
        Employee employee = createDefaultEmployee();
        employee.setCompany(company);
        employee = employeeRepository.save(employee);

        // when
        employeeResource.delete(company.getId(), employee.getId());

        // then
        assertThat(employeeRepository.findById(employee.getId()).isPresent(), equalTo(false));
    }


    private void assertEmployeeSame(Employee entity, EmployeeRead data) {
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


    private Company createDefaultCompany() {
        return Company.builder()
                .name("testcompany")
                .email("test@company.org")
                .url("https://company.org")
                .build();
    }

    private Employee createDefaultEmployee() {
        return Employee.builder()
                .firstName("firstname")
                .lastName("lastname")
                .email("test@test.de")
                .company(createDefaultCompany())
                .dateOfBirth(LocalDate.now().minusYears(5))
                .female(true)
                .build();
    }


}
