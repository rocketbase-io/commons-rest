package io.rocketbase.sample.controller;

import io.rocketbase.commons.controller.BaseController;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.NotFoundException;
import io.rocketbase.sample.converter.CompanyConverter;
import io.rocketbase.sample.dto.company.CompanyRead;
import io.rocketbase.sample.dto.company.CompanyWrite;
import io.rocketbase.sample.model.CompanyEntity;
import io.rocketbase.sample.repository.jpa.CustomJpaRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/company")
public class CompanyController implements BaseController {


    private final CustomJpaRepository<CompanyEntity, String> repository;
    private final CompanyConverter converter;


    @RequestMapping(method = RequestMethod.GET, path = "/{String}")
    @ResponseBody
    public CompanyRead getByString(@PathVariable("String") String String) {
        CompanyEntity CompanyEntity = getCompanyEntity(String);
        return converter.fromEntity(CompanyEntity);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{String}", consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public CompanyRead update(@PathVariable String String, @RequestBody @NotNull @Valid CompanyWrite CompanyWrite) {
        CompanyEntity CompanyEntity = getCompanyEntity(String);
        converter.updateEntityFromEdit(CompanyWrite, CompanyEntity);
        repository.save(CompanyEntity);
        return converter.fromEntity(CompanyEntity);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{String}")
    public void delete(@PathVariable("String") String String) {
        CompanyEntity CompanyEntity = getCompanyEntity(String);
        repository.delete(CompanyEntity);
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

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public PageableResult<CompanyRead> find(@RequestParam(required = false) MultiValueMap<String, String> params) {
        Page<CompanyEntity> entities = repository.findAll(parsePageRequest(params, getDefaultSort()));
        return PageableResult.contentPage(converter.fromEntities(entities.getContent()), entities);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    public CompanyRead create(@RequestBody @NotNull @Valid CompanyWrite CompanyWrite) {
        CompanyEntity CompanyEntity = repository.save(converter.newEntity(CompanyWrite));
        return converter.fromEntity(CompanyEntity);
    }

    protected Sort getDefaultSort() {
        return Sort.by("String");
    }

}
