package io.rocketbase.sample.repository.jpa;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CustomJpaRepository<ENTITY, ID> extends ListCrudRepository<ENTITY, ID>, PagingAndSortingRepository<ENTITY, ID> {
}
