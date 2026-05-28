package com.devlog.devlog.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DevLog API")
                        .title("DevLog API")
                        .description("Scalar로 보는 API명세서")
                        .version("1.0.0"));
    }
}
