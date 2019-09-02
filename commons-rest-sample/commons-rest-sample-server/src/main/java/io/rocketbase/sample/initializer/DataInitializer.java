package io.rocketbase.sample.initializer;

import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.person.Person;
import io.rocketbase.sample.model.CompanyEntity;
import io.rocketbase.sample.model.CustomerEntity;
import io.rocketbase.sample.model.EmployeeEntity;
import io.rocketbase.sample.repository.mongo.CompanyRepository;
import io.rocketbase.sample.repository.jpa.CustomerRepository;
import io.rocketbase.sample.repository.mongo.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class DataInitializer {

    @Resource
    private EmployeeRepository personRepository;

    @Resource
    private CompanyRepository companyRepository;

    @Resource
    private CustomerRepository customerRepository;

    private AtomicInteger companyCounter = new AtomicInteger(0);

    private Map<String, CompanyEntity> companyCache = new HashMap<>();

    @PostConstruct
    public void postConstruct() {
        Fairy fairy = Fairy.create(Locale.GERMAN);
        if (companyRepository.count() == 0) {
            List<CompanyEntity> companyList = new ArrayList<>();
            for (int count = 0; count < 100; count++) {
                io.codearte.jfairy.producer.company.Company company = fairy.company();
                companyList.add(CompanyEntity.builder()
                        .name(company.getName())
                        .email(company.getEmail())
                        .url(company.getUrl())
                        .build());
            }
            companyRepository.saveAll(companyList);

            List<EmployeeEntity> personList = new ArrayList<>();
            for (int count = 0; count < 1000; count++) {
                io.codearte.jfairy.producer.person.Person person = fairy.person();
                DateTime dateOfBirth = person.getDateOfBirth();
                personList.add(EmployeeEntity.builder()
                        .firstName(person.getFirstName())
                        .lastName(person.getLastName())
                        .dateOfBirth(LocalDate.of(dateOfBirth.getYear(), dateOfBirth.getMonthOfYear(), dateOfBirth.getDayOfMonth()))
                        .female(person.getSex().equals(Person.Sex.FEMALE))
                        .email(person.getEmail())
                        .company(companyList.get(ThreadLocalRandom.current().nextInt(0, companyList.size())))
                        .build());
            }
            personRepository.saveAll(personList);
            log.info("initialized {} persons and {} companies", personList.size(), companyCounter.get());
        }
        if (customerRepository.count() == 0) {
            List<CustomerEntity> customerList = new ArrayList<>();
            for (int count = 0; count < 100; count++) {
                io.codearte.jfairy.producer.person.Person person = fairy.person();
                customerList.add(CustomerEntity.builder()
                        .name(fairy.person().getFullName())
                        .build());
            }
            customerRepository.saveAll(customerList);
            log.info("initialized {} customers", customerList.size());
        }
    }
}
