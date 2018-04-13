package io.rocketbase.sample.resource;

import io.rocketbase.commons.dto.ErrorResponse;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.BadRequestException;
import io.rocketbase.commons.request.PageableRequest;
import io.rocketbase.commons.resource.BasicResponseErrorHandler;
import io.rocketbase.sample.dto.company.CompanyRead;
import io.rocketbase.sample.dto.company.CompanyWrite;
import io.rocketbase.sample.model.Company;
import io.rocketbase.sample.repository.CompanyRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.AssertionErrors;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Arrays;

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
        RestTemplate restTemplate = testRestTemplate.getRestTemplate();
        restTemplate.setErrorHandler(new BasicResponseErrorHandler());
        companyResource.setRestTemplate(restTemplate);
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
        CompanyRead data = companyResource.getById(company.getId());

        // then
        assertCompanySame(company, data);
    }

    @Test
    public void shouldNotGetUnknownCompany() throws Exception {
        // given

        // when
        CompanyRead data = companyResource.getById("notexisting");

        // then
        assertThat(data, nullValue());
    }

    @Test
    public void shouldFindAllCompanys() throws Exception {
        // given
        Company Company = companyRepository.save(createDefaultCompany());

        // when
        PageableResult<CompanyRead> result = companyResource.find(0, 10);

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
    public void shouldExecuteAllCompanys() throws Exception {
        // given
        Company Company = companyRepository.save(createDefaultCompany());

        Object mock = Mockito.mock(Object.class);

        // when
        companyResource.executeAll(companyData -> {
            mock.hashCode();
        }, 1);

        // then
        verify(mock).hashCode();
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
        CompanyRead companyRead = companyResource.create(companyWrite);

        // then
        assertThat(companyRead, notNullValue());
        assertThat(companyRead.getId(), notNullValue());

        Company Company = companyRepository.findById(companyRead.getId()).get();
        assertCompanySame(Company, companyRead);
    }

    @Test
    public void shouldNotCreateInvalidCompany() throws Exception {
        // given
        CompanyWrite companyWrite = CompanyWrite.builder()
                .name("testcompany")
                .url("https://company.org")
                .build();

        // when
        try {
            CompanyRead companyRead = companyResource.create(companyWrite);

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
        CompanyWrite companyWrite = CompanyWrite.builder()
                .name("testcompany all new")
                .email("test@company2.org")
                .url("https://company2.org")
                .build();


        // when
        CompanyRead companyRead = companyResource.update(company.getId(), companyWrite);

        // then
        assertThat(companyRead, notNullValue());
        assertThat(companyRead.getId(), is(company.getId()));

        assertThat(companyRead.getName(), is(companyWrite.getName()));
        assertThat(companyRead.getEmail(), is(companyWrite.getEmail()));
        assertThat(companyRead.getUrl(), is(companyWrite.getUrl()));

        company = companyRepository.findById(company.getId()).get();
        assertCompanySame(company, companyRead);
    }

    @Test
    public void shouldDeleteCompany() throws Exception {
        // given
        Company company = companyRepository.save(createDefaultCompany());

        // when
        companyResource.delete(company.getId());

        // then
        assertThat(companyRepository.findById(company.getId()).isPresent(), equalTo(false));
    }

    @Test
    public void shouldSortAsc() {
        // given
        Company aCompany = Company.builder()
                .name("a-company")
                .email("a@company.org")
                .url("https://a-company.org")
                .build();
        companyRepository.saveAll(Arrays.asList(createDefaultCompany(), aCompany));


        // when
        PageableResult<CompanyRead> result = companyResource.find(PageableRequest.builder()
                .sort(new Sort(Sort.Direction.ASC, "name"))
                .build());

        // then
        assertThat(result, notNullValue());
        assertThat(result.getTotalElements(), is(2L));
        assertThat(result.getPage(), is(0));
        assertThat(result.getTotalPages(), is(1));
        assertThat(result.getContent(), hasSize(2));
        assertCompanySameWithoutId(aCompany,
                result.getContent()
                        .get(0));
    }

    @Test
    public void shouldSortDesc() {
        // given
        Company aCompany = Company.builder()
                .name("a-company")
                .email("a@company.org")
                .url("https://a-company.org")
                .build();
        companyRepository.saveAll(Arrays.asList(createDefaultCompany(), aCompany));


        // when
        PageableResult<CompanyRead> result = companyResource.find(PageableRequest.builder()
                .sort(new Sort(Sort.Direction.DESC, "name"))
                .build());

        // then
        assertThat(result, notNullValue());
        assertThat(result.getTotalElements(), is(2L));
        assertThat(result.getPage(), is(0));
        assertThat(result.getTotalPages(), is(1));
        assertThat(result.getContent(), hasSize(2));
        assertCompanySameWithoutId(createDefaultCompany(),
                result.getContent()
                        .get(0));
    }


    private void assertCompanySame(Company company, CompanyRead data) {
        assertCompanySameWithoutId(company, data);
        assertThat(data.getId(), is(company.getId()));
    }

    private void assertCompanySameWithoutId(Company company, CompanyRead data) {
        assertThat(data, notNullValue());
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
