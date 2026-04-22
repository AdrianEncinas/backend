package com.assetstrack.backend.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AssetTrack API")
                        .description("Documentación de la API para el sistema de gestión de inversiones AssetTrack.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("AssetTrack Team")
                                .email("soporte@assetstrack.com")
                                .url("https://assetstrack.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org"))
                )
                .externalDocs(new ExternalDocumentation()
                        .description("Repositorio Backend")
                        .url("https://github.com/assetstrack/backend"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
