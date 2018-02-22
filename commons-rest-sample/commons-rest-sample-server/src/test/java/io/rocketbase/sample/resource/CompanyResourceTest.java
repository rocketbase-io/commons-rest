package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.BadRequestException;
import io.rocketbase.sample.dto.data.CompanyData;
import io.rocketbase.sample.dto.edit.CompanyEdit;
import io.rocketbase.sample.model.Company;
import io.rocketbase.sample.repository.CompanyRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.AssertionErrors;

import javax.annotation.Resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
public class CompanyResourceTest {

    @Resource
    private CompanyResource companyResource;

    @Resource
    private CompanyRepository companyRepository;

    @Resource
    private TestRestTemplate testRestTemplate;

    @Before
    public void setup() throws Exception {
        companyResource.setRestTemplate(testRestTemplate.getRestTemplate());
        companyRepository.deleteAll();
    }

    @After
    public void cleanup() throws Exception {
        companyRepository.deleteAll();
    }

    @Test
    public void shouldGetCompany() throws Exception {
        // given
        Company company = companyRepository.save(createDefaultCompany());

        // when
        CompanyData data = companyResource.getById(company.getId());

        // then
        assertCompanySame(company, data);
    }

    @Test
    public void shouldNotGetUnknownCompany() throws Exception {
        // given

        // when
        CompanyData data = companyResource.getById("notexisting");

        // then
        assertThat(data, nullValue());
    }

    @Test
    public void shouldFindAllCompanys() throws Exception {
        // given
        Company Company = companyRepository.save(createDefaultCompany());

        // when
        PageableResult<CompanyData> result = companyResource.find(0, 10);

        // then
        assertThat(result, notNullValue());
        assertThat(result.getTotalElements(), is(1L));
        assertThat(result.getPage(), is(0));
        assertThat(result.getTotalPages(), is(1));
        assertThat(result.getPageSize(), is(10));
        assertThat(result.getContent(), hasSize(1));
        assertCompanySame(Company,
                result.getContent()
                        .get(0));
    }

    @Test
    public void shouldFindAllCompanysAndExecute() throws Exception {
        // given
        Company Company = companyRepository.save(createDefaultCompany());

        Object mock = Mockito.mock(Object.class);

        // when
        companyResource.findAllAndExecute(companyData -> {
            mock.hashCode();
        }, 1);

        // then
        verify(mock).hashCode();
    }

    @Test
    public void shouldCreateCompany() throws Exception {
        // given
        CompanyEdit companyEdit = CompanyEdit.builder()
                .name("testcompany")
                .email("test@company.org")
                .url("https://company.org")
                .build();

        // when
        CompanyData companyData = companyResource.create(companyEdit);

        // then
        assertThat(companyData, notNullValue());
        assertThat(companyData.getId(), notNullValue());

        Company Company = companyRepository.findOne(companyData.getId());
        assertCompanySame(Company, companyData);
    }

    @Test
    public void shouldNotCreateInvalidCompany() throws Exception {
        // given
        CompanyEdit companyEdit = CompanyEdit.builder()
                .name("testcompany")
                .url("https://company.org")
                .build();

        // when
        try {
            CompanyData companyData = companyResource.create(companyEdit);

            // then
            AssertionErrors.fail("should not create invalid company");
        } catch (BadRequestException ex) {
            ErrorResponse errorResponse = ex.getErrorResponse();
            assertThat(errorResponse, notNullValue());
            assertThat(errorResponse.getFields(), hasEntry(is("email"), not(isEmptyString())));
        }

    }

    @Test
    public void shouldUpdateCompany() throws Exception {
        // given
        Company company = companyRepository.save(createDefaultCompany());
        CompanyEdit companyEdit = CompanyEdit.builder()
                .name("testcompany all new")
                .email("test@company2.org")
                .url("https://company2.org")
                .build();


        // when
        CompanyData companyData = companyResource.update(company.getId(), companyEdit);

        // then
        assertThat(companyData, notNullValue());
        assertThat(companyData.getId(), is(company.getId()));

        assertThat(companyData.getName(), is(companyEdit.getName()));
        assertThat(companyData.getEmail(), is(companyEdit.getEmail()));
        assertThat(companyData.getUrl(), is(companyEdit.getUrl()));

        company = companyRepository.findOne(company.getId());
        assertCompanySame(company, companyData);
    }

    @Test
    public void shouldDeleteCompany() throws Exception {
        // given
        Company company = companyRepository.save(createDefaultCompany());

        // when
        companyResource.delete(company.getId());

        // then
        assertThat(companyRepository.findOne(company.getId()), nullValue());
    }


    private void assertCompanySame(Company company, CompanyData data) {
        assertThat(data, notNullValue());
        assertThat(data.getId(), is(company.getId()));
        assertThat(data.getName(), is(company.getName()));
        assertThat(data.getEmail(), is(company.getEmail()));
        assertThat(data.getUrl(), is(company.getUrl()));
    }


    private Company createDefaultCompany() {
        return Company.builder()
                .name("testcompany")
                .email("test@company.org")
                .url("https://company.org")
                .build();
    }


}
