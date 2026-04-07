package com.devlog.devlog.global.config;

import com.devlog.devlog.global.filter.JwtAuthenticationFilter;
import com.devlog.devlog.global.provider.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private final JwtTokenProvider jwtTokenProvider;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                        .cors(Customizer.withDefaults())
                        .csrf(csrf -> csrf.disable())
                        .sessionManagement(session -> session
                                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(auth -> auth
                                        .requestMatchers("/auth/signin", "/auth/signup", "/auth/refresh", "/error").permitAll()
                                        .requestMatchers("/auth/signout", "/api/**").authenticated()
                                        .anyRequest().authenticated())
                        .exceptionHandling(exception -> exception
                                // 401 Unauthorized: 토큰 없이 접근 시 401 에러 반환 (프론트의 재발급 유도)
                                .authenticationEntryPoint((request, response, authException) -> {
                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                        response.setContentType("application/json;charset=UTF-8");
                                        response.getWriter().write("{\"status\":\"ERROR\", \"code\":\"AUTH-006\", \"message\":\"인증이 필요합니다.\"}");
                                })
                                // 403 Forbidden: 인증은 됐으나 해당 리소스에 권한이 없을 때
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                        response.setContentType("application/json;charset=UTF-8");
                                        response.getWriter().write("{\"status\":\"ERROR\", \"code\":\"AUTH-007\", \"message\":\"권한이 없습니다.\"}");
                                })
                        )
                        .addFilterBefore(
                                        new JwtAuthenticationFilter(jwtTokenProvider),
                                        UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        protected CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList("https://dev-log-frontend-eob7.vercel.app/", "http://localhost:5173"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
