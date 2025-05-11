package com.mogacko.mogacko.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 문서화 구성 클래스
 * API 문서 생성 및 그룹화를 설정합니다.
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 기본 정보를 설정합니다.
     *
     * @return OpenAPI 구성 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mogacko API")
                        .description("모각코 스터디 그룹 관리 애플리케이션 API 문서")
                        .version("v1.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 헤더에 입력하세요. 예: Bearer {token}")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * 공개 API 그룹을 구성합니다.
     * 인증이 필요하지 않은 API를 그룹화합니다.
     *
     * @return GroupedOpenApi 구성 객체
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/auth/**", "/api/public/**")
                .displayName("🔓 Public APIs")
                .build();
    }

    /**
     * 사용자 관련 API 그룹을 구성합니다.
     *
     * @return GroupedOpenApi 구성 객체
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("users")
                .pathsToMatch("/api/users/**")
                .displayName("👤 User APIs")
                .build();
    }

    /**
     * 스터디 그룹 관련 API 그룹을 구성합니다.
     *
     * @return GroupedOpenApi 구성 객체
     */
    @Bean
    public GroupedOpenApi groupApi() {
        return GroupedOpenApi.builder()
                .group("groups")
                .pathsToMatch("/api/groups/**")
                .displayName("👥 Study Group APIs")
                .build();
    }
}