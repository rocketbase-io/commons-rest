package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.BadRequestException;
import io.rocketbase.commons.resource.BasicResponseErrorHandler;
import io.rocketbase.sample.dto.data.EmployeeData;
import io.rocketbase.sample.dto.edit.EmployeeEdit;
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
        EmployeeData data = employeeResource.getById(company.getId(), employee.getId());

        // then
        assertEmployeeSame(employee, data);
    }

    @Test
    public void shouldNotGetUnknownEmployee() throws Exception {
        // given
        Company company = companyRepository.save(createDefaultCompany());
        Employee employee = createDefaultEmployee();
        employee.setCompany(company);
        employee = employeeRepository.save(employee);

        // when
        EmployeeData data = employeeResource.getById(company.getId(), "notexisting");
        EmployeeData missMatch = employeeResource.getById("notexisting", employee.getId());

        // then
        assertThat(data, nullValue());
        assertThat(missMatch, nullValue());
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
        PageableResult<EmployeeData> result = employeeResource.find(company.getId(), 0, 10);

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

        EmployeeEdit employeeEdit = EmployeeEdit.builder()
                .firstName("max")
                .lastName("Müller")
                .email("max@test.de")
                .dateOfBirth(LocalDate.now())
                .female(false)
                .build();

        // when
        EmployeeData employeeData = employeeResource.create(company.getId(), employeeEdit);

        // then
        assertThat(employeeData, notNullValue());
        assertThat(employeeData.getId(), notNullValue());

        Employee employee = employeeRepository.findOneByCompanyIdAndId(company.getId(), employeeData.getId());
        assertEmployeeSame(employee, employeeData);
    }

    @Test
    public void shouldNotCreateInvalidEmployee() throws Exception {
        // given
        Company company = companyRepository.save(createDefaultCompany());

        EmployeeEdit employeeEdit = EmployeeEdit.builder()
                .firstName("max")
                .lastName("Müller")
                .email("wrongEmail")
                .female(false)
                .build();

        // when
        try {
            EmployeeData employeeData = employeeResource.create(company.getId(), employeeEdit);

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

        EmployeeEdit employeeEdit = EmployeeEdit.builder()
                .firstName("max")
                .lastName("Müller")
                .email("max@test.de")
                .dateOfBirth(LocalDate.now())
                .female(false)
                .build();


        // when
        EmployeeData employeeData = employeeResource.update(company.getId(), employee.getId(), employeeEdit);

        // then
        assertThat(employeeData, notNullValue());
        assertThat(employeeData.getId(), is(employee.getId()));

        assertThat(employeeData.getFirstName(), is(employeeEdit.getFirstName()));
        assertThat(employeeData.getLastName(), is(employeeEdit.getLastName()));
        assertThat(employeeData.getEmail(), is(employeeEdit.getEmail()));

        employee = employeeRepository.findOne(employeeData.getId());
        assertEmployeeSame(employee, employeeData);
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
        assertThat(employeeRepository.findOne(employee.getId()), nullValue());
    }


    private void assertEmployeeSame(Employee entity, EmployeeData data) {
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
