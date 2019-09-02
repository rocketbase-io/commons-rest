package io.rocketbase.sample.controller;

import io.rocketbase.commons.controller.AbstractCrudObfuscatedController;
import io.rocketbase.sample.converter.CustomerConverter;
import io.rocketbase.sample.dto.customer.CustomerRead;
import io.rocketbase.sample.dto.customer.CustomerWrite;
import io.rocketbase.sample.model.CustomerEntity;
import io.rocketbase.sample.repository.jpa.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/customer")
public class CustomerController extends AbstractCrudObfuscatedController<CustomerEntity, CustomerRead, CustomerWrite, CustomerConverter> {

    @Autowired
    public CustomerController(CustomerRepository repository, CustomerConverter converter) {
        super(repository, converter);
    }

    @Override
    protected CustomerRepository getRepository() {
        return (CustomerRepository) super.getRepository();
    }

    @Override
    protected Sort getDefaultSort() {
        return Sort.by("id");
    }

}
