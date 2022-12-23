package io.rocketbase.sample.repository.jpa;

import io.rocketbase.sample.model.CustomerEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends CustomJpaRepository<CustomerEntity, Long> {

}
