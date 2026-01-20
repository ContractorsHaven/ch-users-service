package org.binary.scripting.chusersservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Contractors Haven Users Service API")
                        .version("1.0.0")
                        .description("RESTful API for managing users in the ContractorsHaven platform. " +
                                "This service provides CRUD operations for user management with reactive endpoints.")
                        .contact(new Contact()
                                .name("ContractorsHaven Team")
                                .url("https://github.com/ContractorsHaven"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addServersItem(new Server()
                        .url("/")
                        .description("Default Server"));
    }
}
