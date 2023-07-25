package io.rocketbase.sample.repository.jpa;

import io.rocketbase.sample.model.LocationEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends ListCrudRepository<LocationEntity, Long>, PagingAndSortingRepository<LocationEntity, Long> {

}
