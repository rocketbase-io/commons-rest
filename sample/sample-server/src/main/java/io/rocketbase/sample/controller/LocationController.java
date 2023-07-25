package io.rocketbase.sample.controller;

import io.hypersistence.tsid.TSID;
import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.exception.NotFoundException;
import io.rocketbase.sample.converter.LocationConverter;
import io.rocketbase.sample.dto.localtion.LocationRead;
import io.rocketbase.sample.model.LocationEntity;
import io.rocketbase.sample.repository.jpa.LocationRepository;
import io.rocketbase.sample.resource.LocationApi;
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
public class LocationController implements LocationApi {


    private final LocationRepository repository;
    private final LocationConverter converter;

    @Override
    public PageableResult<LocationRead> findAll(Pageable pageable) {
        Page<LocationEntity> entities = repository.findAll(pageable);
        return PageableResult.contentPage(converter.fromEntities(entities.getContent()), entities);
    }

    @Override
    public LocationRead getById(TSID id) {
        return repository.findById(id.toLong()).map(converter::fromEntity).orElseThrow(NotFoundException::new);
    }
}
