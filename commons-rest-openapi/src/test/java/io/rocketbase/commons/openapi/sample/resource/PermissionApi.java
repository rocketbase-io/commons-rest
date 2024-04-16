package io.rocketbase.commons.openapi.sample.resource;

import io.rocketbase.commons.generator.MutationHook;
import io.rocketbase.commons.openapi.sample.dto.PermissionCmd;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@SecurityRequirement(name = "OAuth2", scopes = {"user"})
@Tag(name = "permissionCommand", description = "create or update object's permissions")
public interface PermissionApi {

    @MutationHook(invalidateKeys = {"element,detail,${body.objectId}", "activities,${body.objectId}"})
    @Operation(description = "add/update permission for a set of identities")
    @PutMapping(value = {"/element/set-permission"}, consumes = MimeTypeUtils.APPLICATION_JSON_VALUE, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> setPermission(@Valid @NotNull @RequestBody PermissionCmd cmd);

}
