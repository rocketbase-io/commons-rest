package io.rocketbase.commons.openapi.sample;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("https://api.rocketbase.io/sample").description("Development Server"))
                .info(new Info().title("ProductSpace Dashboard API")
                        .version("1.0.0")
                        .description("Description")
                        .contact(new Contact().name("rocketbase.io").email("info@rocketbase.io").url("https://www.rocketbase.io")));
    }
}
