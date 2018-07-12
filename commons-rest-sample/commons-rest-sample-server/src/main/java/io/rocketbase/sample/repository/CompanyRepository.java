package io.rocketbase.sample.repository;

import io.rocketbase.sample.model.CompanyEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CompanyRepository extends MongoRepository<CompanyEntity, String> {

}
