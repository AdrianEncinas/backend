package com.assetstrack.backend.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AssetTrack API")
                        .description("API RESTful para el sistema de gestión de inversiones y portafolio. " +
                                "Proporciona funcionalidades para autenticación, gestión de posiciones, " +
                                "búsqueda de acciones, seguimiento de listas de observación y análisis de portafolio.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("AssetTrack Team")
                                .url("https://assetstrack.com")
                                .email("soporte@assetstrack.com"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentación completa y repositorio")
                        .url("https://github.com/assetstrack/backend"))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Servidor local de desarrollo"))
                .addServersItem(new Server()
                        .url("https://api.assetstrack.com")
                        .description("Servidor de producción"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
