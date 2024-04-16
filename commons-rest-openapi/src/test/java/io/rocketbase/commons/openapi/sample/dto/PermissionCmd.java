package io.rocketbase.commons.openapi.sample.dto;

import io.hypersistence.tsid.TSID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PermissionCmd {

    @NotNull
    protected TSID objectId;

    @Size(min = 1)
    protected Set<TSID> identityIds;

    @NotNull
    protected Short permissionId;
}
