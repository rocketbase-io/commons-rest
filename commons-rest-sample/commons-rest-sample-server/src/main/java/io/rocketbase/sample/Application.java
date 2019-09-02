package io.rocketbase.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "io.rocketbase.sample.repository.mongo")
@EnableJpaRepositories(basePackages = "io.rocketbase.sample.repository.jpa")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
