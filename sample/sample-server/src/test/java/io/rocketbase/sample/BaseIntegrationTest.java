package io.rocketbase.sample;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Locale;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@Testcontainers
public abstract class BaseIntegrationTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Container
    protected static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4"));

    @DynamicPropertySource
    static void initTestContainerProperties(DynamicPropertyRegistry registry) {
        log.info("Setting spring.data.mongodb.uri = {}", mongoDBContainer.getReplicaSetUrl());
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    public void setup() {
        LocaleContextHolder.setLocale(Locale.GERMAN);
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
        RestAssuredMockMvc.enableLoggingOfRequestAndResponseIfValidationFails();
    }

}
