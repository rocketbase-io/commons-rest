package io.rocketbase.sample.initializer;

import com.devskiller.jfairy.Fairy;
import com.devskiller.jfairy.producer.company.Company;
import com.devskiller.jfairy.producer.person.Person;
import com.devskiller.jfairy.producer.text.TextProducer;
import io.rocketbase.sample.model.CompanyEntity;
import io.rocketbase.sample.model.CustomerEntity;
import io.rocketbase.sample.model.EmployeeEntity;
import io.rocketbase.sample.model.LocationEntity;
import io.rocketbase.sample.repository.jpa.CustomerRepository;
import io.rocketbase.sample.repository.jpa.LocationRepository;
import io.rocketbase.sample.repository.mongo.CompanyRepository;
import io.rocketbase.sample.repository.mongo.EmployeeRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    @Resource
    private LocationRepository locationRepository;

    private AtomicInteger companyCounter = new AtomicInteger(0);

    private Map<String, CompanyEntity> companyCache = new HashMap<>();

    @PostConstruct
    public void postConstruct() {
        Fairy fairy = Fairy.create(Locale.GERMAN);
        if (companyRepository.count() == 0) {
            List<CompanyEntity> companyList = new ArrayList<>();
            for (int count = 0; count < 100; count++) {
                Company company = fairy.company();
                companyList.add(CompanyEntity.builder()
                        .name(company.getName())
                        .email(company.getEmail())
                        .url(company.getUrl())
                        .build());
            }
            companyRepository.saveAll(companyList);

            List<EmployeeEntity> personList = new ArrayList<>();
            for (int count = 0; count < 1000; count++) {
                Person person = fairy.person();
                personList.add(EmployeeEntity.builder()
                        .firstName(person.getFirstName())
                        .lastName(person.getLastName())
                        .dateOfBirth(person.getDateOfBirth())
                        .female(person.getSex().equals(Person.Sex.FEMALE))
                        .email(person.getEmail())
                        .companyId(companyList.get(ThreadLocalRandom.current().nextInt(0, companyList.size())).getId())
                        .build());
            }
            personRepository.saveAll(personList);
            log.info("initialized {} persons and {} companies", personList.size(), companyCounter.get());
        }
        if (customerRepository.count() == 0) {
            List<CustomerEntity> customerList = new ArrayList<>();
            for (int count = 0; count < 100; count++) {
                Person person = fairy.person();
                customerList.add(CustomerEntity.builder()
                        .name(fairy.person().getFullName())
                        .build());
            }
            customerRepository.saveAll(customerList);
            log.info("initialized {} customers", customerList.size());
        }

        if (locationRepository.count() == 0) {
            List<LocationEntity> locationList = new ArrayList<>();
            for (int count = 0; count < 50; count++) {
                TextProducer textProducer = fairy.textProducer();
                locationList.add(LocationEntity.builder()
                        .name(textProducer.word())
                        .city(textProducer.latinWord())
                        .street(textProducer.sentence(3))
                        .country("de")
                        .build());
            }
            locationRepository.saveAll(locationList);
            log.info("initialized {} locations", locationList.size());
        }
    }
}
