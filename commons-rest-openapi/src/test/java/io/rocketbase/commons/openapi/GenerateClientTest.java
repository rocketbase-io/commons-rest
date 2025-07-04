package io.rocketbase.commons.openapi;


import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.parser.RestApplicationParser;
import cz.habarta.typescript.generator.parser.SourceType;
import cz.habarta.typescript.generator.spring.SpringApplicationParser;
import io.rocketbase.commons.openapi.sample.SampleApplication;
import io.rocketbase.commons.openapi.sample.resource.ActivityController;
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
import java.util.Collections;
import java.util.List;

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

    private void generateTypeScriptTypes() {
        final Settings settings = new Settings();
        settings.outputKind = TypeScriptOutputKind.global;
        settings.outputFileType = TypeScriptFileType.implementationFile;
        settings.jsonLibrary = JsonLibrary.jackson2;
        settings.noFileComment = true;
        settings.noTslintDisable = true;
        settings.noEslintDisable = true;
        settings.newline = "\n";
        settings.customTypeMappings = Collections.singletonMap("java.util.Map<K, V>", "Map<K, V>");
        settings.mapDate = DateMapping.asString;
        settings.classLoader = Thread.currentThread().getContextClassLoader();
        settings.optionalAnnotations = List.of(jakarta.annotation.Nullable.class, org.springframework.lang.Nullable.class);
        settings.generateSpringApplicationClient = true;


        final Input.Parameters parameters = new Input.Parameters();
        parameters.classNamePatterns = List.of("io.rocketbase.commons.openapi.sample.dto.**");

        TypeScriptGenerator typeScriptGenerator = new TypeScriptGenerator(settings);
        String result = typeScriptGenerator.generateTypeScript(Input.from(ActivityController.class));//Input.from(parameters));

        SpringApplicationParser springApplicationParser = new SpringApplicationParser(settings, typeScriptGenerator.getCommonTypeProcessor());

        RestApplicationParser.Result jaxrsResult = springApplicationParser.tryParse(new SourceType<>(ActivityController.class));
    }
}
