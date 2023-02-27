package io.rocketbase.commons.openapi.sample.resource;

import io.rocketbase.commons.generator.QueryHook;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.OutputStream;

@SecurityRequirement(name = "OAuth2", scopes = {"user"})
@Tag(name = "download", description = "showrooms are used mainly by vendors to present their products.")
@RequestMapping(path = "/download", produces = MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)
public interface DownloadApi {

    @QueryHook(value = "downloadExcel", cacheKeys = "download,excel", staleTime = 0)
    @GetMapping(path = "/excel", consumes = MimeTypeUtils.ALL_VALUE)
    ResponseEntity<byte[]> downloadExcel();

    @QueryHook(value = "downloadPdf", cacheKeys = "download,pdf", staleTime = 0)
    @GetMapping(path = "/pdf", consumes = MimeTypeUtils.ALL_VALUE)
    ResponseEntity<Resource> downloadPdf();

    @QueryHook(value = "downloadPpt", cacheKeys = "download,ppt", staleTime = 0)
    @GetMapping(path = "/ppt", consumes = MimeTypeUtils.ALL_VALUE)
    ResponseEntity<OutputStream> downloadPpt();
}
