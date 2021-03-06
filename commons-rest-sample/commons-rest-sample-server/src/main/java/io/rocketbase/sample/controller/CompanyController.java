package io.rocketbase.sample.controller;

import io.rocketbase.commons.controller.AbstractCrudController;
import io.rocketbase.sample.converter.CompanyConverter;
import io.rocketbase.sample.dto.company.CompanyRead;
import io.rocketbase.sample.dto.company.CompanyWrite;
import io.rocketbase.sample.model.CompanyEntity;
import io.rocketbase.sample.repository.mongo.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/company")
public class CompanyController extends AbstractCrudController<CompanyEntity, CompanyRead, CompanyWrite, String, CompanyConverter> {

    @Autowired
    public CompanyController(CompanyRepository repository, CompanyConverter converter) {
        super(repository, converter);
    }

    @Override
    protected CompanyRepository getRepository() {
        return (CompanyRepository) super.getRepository();
    }

    @Override
    protected Sort getDefaultSort() {
        return Sort.by("id");
    }

}
