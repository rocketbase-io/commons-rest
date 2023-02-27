package io.rocketbase.sample.repository.mongo;

import io.rocketbase.sample.model.CompanyEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CompanyRepository extends MongoRepository<CompanyEntity, String> {

}
