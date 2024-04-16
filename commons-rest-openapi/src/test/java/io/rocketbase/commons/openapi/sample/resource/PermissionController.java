package io.rocketbase.commons.openapi.sample.resource;

import io.rocketbase.commons.openapi.sample.dto.PermissionCmd;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PermissionController implements PermissionApi {
    @Override
    public ResponseEntity<Void> setPermission(PermissionCmd cmd) {
        return null;
    }
}
