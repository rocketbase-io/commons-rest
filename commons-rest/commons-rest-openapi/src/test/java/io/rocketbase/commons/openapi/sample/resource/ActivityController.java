package io.rocketbase.commons.openapi.sample.resource;

import io.rocketbase.commons.dto.PageableResult;
import io.rocketbase.commons.openapi.sample.dto.Activity;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class ActivityController implements ActivityApi {
    @Override
    public ResponseEntity<PageableResult<Activity>> loadActivities(Pageable pageable, Optional<String> query, Optional<String> activityType) {
        return null;
    }

    @Override
    public ResponseEntity<Activity> findById(String id) {
        return null;
    }
}
