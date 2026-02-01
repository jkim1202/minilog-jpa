package com.asdf.minilog.config; // 패키지명은 본인 프로젝트에 맞게 수정

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiDocumentationConfig {
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
            .components(new Components()
                    .addSecuritySchemes("bearer-key",
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("bearer-key"))
            .info(new Info()
                    .title("MiniLog API")
                    .description("MiniLog 프로젝트 API 명세서")
                    .version("1.0.0"));
  }
}