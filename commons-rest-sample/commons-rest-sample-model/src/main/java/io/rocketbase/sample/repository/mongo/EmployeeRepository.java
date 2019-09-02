package io.rocketbase.sample.repository.mongo;

import io.rocketbase.sample.model.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EmployeeRepository extends MongoRepository<EmployeeEntity, String> {

    Optional<EmployeeEntity> findOneByCompanyIdAndId(String companyId, String id);

    Page<EmployeeEntity> findAllByCompanyId(String companyId, Pageable pageRequest);
}
