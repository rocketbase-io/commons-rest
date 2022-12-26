package io.rocketbase.sample.controller;

import io.rocketbase.commons.controller.BaseController;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.NotFoundException;
import io.rocketbase.sample.converter.CompanyConverter;
import io.rocketbase.sample.dto.company.CompanyRead;
import io.rocketbase.sample.dto.company.CompanyWrite;
import io.rocketbase.sample.model.CompanyEntity;
import io.rocketbase.sample.repository.mongo.CompanyRepository;
import io.rocketbase.sample.resource.CompanyApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api")
public class CompanyController implements BaseController, CompanyApi {


    private final CompanyRepository repository;
    private final CompanyConverter converter;


    public CompanyRead findById(String id) {
        CompanyEntity CompanyEntity = getCompanyEntity(id);
        return converter.fromEntity(CompanyEntity);
    }

    public CompanyRead update(String String, CompanyWrite CompanyWrite) {
        CompanyEntity CompanyEntity = getCompanyEntity(String);
        converter.updateEntityFromEdit(CompanyWrite, CompanyEntity);
        repository.save(CompanyEntity);
        return converter.fromEntity(CompanyEntity);
    }

    public void delete(String id) {
        CompanyEntity CompanyEntity = getCompanyEntity(id);
        repository.delete(CompanyEntity);
    }

    public PageableResult<CompanyRead> find(Pageable pageable) {
        Page<CompanyEntity> entities = repository.findAll(pageable);
        return PageableResult.contentPage(converter.fromEntities(entities.getContent()), entities);
    }

    public CompanyRead create(CompanyWrite write) {
        CompanyEntity CompanyEntity = repository.save(converter.newEntity(write));
        return converter.fromEntity(CompanyEntity);
    }

    /**
     * get by String or throw {@link NotFoundException}
     *
     * @param String unique Stringentifier
     * @return CompanyEntity
     */
    protected CompanyEntity getCompanyEntity(String String) {
        return repository.findById(String)
                .orElseThrow(() -> new NotFoundException());
    }

}
