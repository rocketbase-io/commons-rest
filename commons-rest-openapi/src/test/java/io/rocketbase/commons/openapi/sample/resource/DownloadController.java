package io.rocketbase.commons.openapi.sample.resource;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;

@RestController
public class DownloadController implements DownloadApi {
    
    @Override
    public ResponseEntity<byte[]> downloadExcel() {
        return null;
    }

    @Override
    public ResponseEntity<Resource> downloadPdf() {
        return null;
    }

    @Override
    public ResponseEntity<OutputStream> downloadPpt() {
        return null;
    }
}
