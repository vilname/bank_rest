package com.example.bankcards.config;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;

import java.util.List;

@Configuration
public class Config {

    @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token. Example: 'eyJhbGciOiJIUzI1NiIs...'")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info().title("Документация").version(appVersion)
                        .description("Документация свагера")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }

    @Bean
    public OpenApiCustomizer authEndpointsWithoutLockCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            List<String> publicAuthPaths = List.of("/api/auth/login", "/api/auth/register");
            for (String path : publicAuthPaths) {
                var item = openApi.getPaths().get(path);
                if (item == null) {
                    continue;
                }
                if (item.getPost() != null) {
                    item.getPost().setSecurity(List.of());
                }
            }
        };
    }
}
