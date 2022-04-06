package io.rocketbase.commons.controller;


import io.rocketbase.commons.config.OpenApiGeneratorProperties;
import io.rocketbase.commons.openapi.OpenApiClientCreatorService;
import io.rocketbase.commons.openapi.model.OpenApiController;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@Hidden
@RequiredArgsConstructor
@RestController
public class CodeGeneratorController {

    private final OpenApiGeneratorProperties openApiGeneratorProperties;
    private final OpenApiClientCreatorService openApiClientCreatorService;

    @GetMapping(value = {"/generator/"}, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @SneakyThrows
    public ResponseEntity<List<OpenApiController>> buildReactQueryHooks(HttpServletRequest request) {
        return ResponseEntity.ok(openApiClientCreatorService.getControllers(request));
    }

    @GetMapping(value = {"/generator/typescript-client/{filename}"})
    @SneakyThrows
    public void buildTypescriptClient(HttpServletRequest request, HttpServletResponse response, @PathVariable(value = "filename", required = false) Optional<String> filename) {
        openApiClientCreatorService.getTypescriptClients(request, response,
                openApiGeneratorProperties.getBaseUrl(),
                openApiGeneratorProperties.getGroupName(),
                filename.orElse("client.zip"));
    }


}