package io.rocketbase.commons.openapi;


import io.rocketbase.commons.openapi.sample.SampleApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SampleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GenerateClientTest {

    @Value("http://localhost:${local.server.port}")
    protected String baseUrl;

    @Test
    public void downloadZip() throws Exception {
        File destination = new File("target/test.zip");
        download(new URL(baseUrl + "/generator/client/v5/test.zip"), destination);
        log.info("downloaded: {}", destination.getAbsolutePath());
    }

    @Test
    public void downloadOpenApiYaml() throws Exception {
        File destination = new File("target/api-docs.yaml");
        download(new URL(baseUrl + "/v3/api-docs.yaml"), destination);
        log.info("downloaded: {}", destination.getAbsolutePath());
    }

    private static void download(URL url, File file) throws IOException {
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }
}
