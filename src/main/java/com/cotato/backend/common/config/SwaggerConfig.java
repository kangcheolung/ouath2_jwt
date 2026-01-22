package com.cotato.backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

	private final Environment env;

	@Bean
	public OpenAPI openAPI() {
		// 프로필별 서버 설정
		String profile = env.getActiveProfiles().length > 0 ? env.getActiveProfiles()[0] : "local";

		SecurityScheme accessTokenAuth = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER)
			.name("Authorization");

		SecurityRequirement securityRequirement = new SecurityRequirement()
			.addList("accessTokenAuth");

		Server server = new Server();
		if ("prod".equals(profile)) {
			server.setUrl("https://your-domain.com");  // 운영 서버 주소로 변경
			server.setDescription("운영 서버");
		} else {
			server.setUrl("http://localhost:8080");
			server.setDescription("로컬 서버");
		}

		return new OpenAPI()
			.info(new Info()
				.title("JWT Backend API")
				.description("OAuth2 + JWT 인증 시스템")
				.version("1.0.0"))
			.components(new Components()
				.addSecuritySchemes("accessTokenAuth", accessTokenAuth))
			.addSecurityItem(securityRequirement)
			.servers(List.of(server));
	}

	// Authorize 정보 유지 (새로고침해도 토큰 유지)
	@Bean
	@Primary
	public SwaggerUiConfigProperties swaggerUiConfigProperties(SwaggerUiConfigProperties props) {
		props.setPersistAuthorization(true);
		return props;
	}
}