package com.cotato.backend.common.config;

import com.cotato.backend.common.jwt.JwtAuthenticationFilter;
import com.cotato.backend.oauth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuthService oAuthService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)

            .authorizeHttpRequests(authorize -> authorize
                // 공개 엔드포인트
                .requestMatchers("/", "/h2-console/**", "/login/**", "/oauth2/**", "/callback.html").permitAll()

                // Swagger 경로 허용
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                // 인증 API
                .requestMatchers("/api/auth/**").permitAll()

                // 사용자 API (인증 필요)
                .requestMatchers("/api/users/**").authenticated()

                .anyRequest().authenticated()
            )

            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            )

            .oauth2Login(oauth2Login -> oauth2Login
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                    .userService(oAuthService))
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:8080"
            // 배포시 도메인 추가
        ));

        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}