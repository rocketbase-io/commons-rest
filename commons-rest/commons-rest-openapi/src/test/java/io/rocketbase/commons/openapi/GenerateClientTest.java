package io.rocketbase.commons.openapi;


import io.rocketbase.commons.openapi.sample.SampleApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.net.URL;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SampleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GenerateClientTest {

    @Value("http://localhost:${local.server.port}")
    protected String baseUrl;

    @Test
    public void downloadZip() throws Exception {
        File destination = new File("target/test.zip");
        FileUtils.copyURLToFile(new URL(baseUrl + "/generator/typescript-client/test.zip"), destination);
        log.info("downloaded: {}", destination.getAbsolutePath());
    }
}
