package io.rocketbase.commons.controller;


import io.rocketbase.commons.openapi.OpenApiClientCreatorService;
import io.rocketbase.commons.openapi.OpenApiController;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@Hidden
public class CodeGeneratorController {

    @Resource
    private OpenApiClientCreatorService openApiClientCreatorService;

    @Value("${generator.baseurl:/api}")
    private String baseUrl;

    @Value("${generator.groupname:/ModuleApi}")
    private String groupName;

    @GetMapping(
            value = {"/generator/"},
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE
    )
    @SneakyThrows
    public ResponseEntity<List<OpenApiController>> buildReactQueryHooks(HttpServletRequest request) {
        return ResponseEntity.ok(openApiClientCreatorService.getControllers(request));
    }

    @GetMapping(value = {"/generator/typescript-client/{filename}"})
    @SneakyThrows
    public void buildTypescriptClient(HttpServletRequest request, HttpServletResponse response, @PathVariable(value = "filename", required = false) Optional<String> filename) {
        openApiClientCreatorService.getTypescriptClients(request, response, baseUrl, groupName, filename.orElse("client.zip"));
    }


}
