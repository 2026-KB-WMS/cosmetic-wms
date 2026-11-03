package com.kb.cosmetic_wms.global.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String HEADQUARTERS = "HEADQUARTERS";
    public static final String WAREHOUSE_MANAGER = "WAREHOUSE_MANAGER";
    public static final String FRANCHISE_MANAGER = "FRANCHISE_MANAGER";

    private static final String ROLE_CLAIM = "role";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(ROLE_CLAIM);
        authorities.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/members/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/reissue",
                                "/api/v1/auth/logout").permitAll()

                        // 마스터 데이터: 조회는 로그인한 모두, 변경은 본사
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products/**",
                                "/api/v1/categories/**",
                                "/api/v1/product-types/**",
                                "/api/v1/stores/**")
                        .hasAnyRole(HEADQUARTERS, WAREHOUSE_MANAGER, FRANCHISE_MANAGER)
                        .requestMatchers(
                                "/api/v1/products/**",
                                "/api/v1/categories/**",
                                "/api/v1/product-types/**",
                                "/api/v1/stores/**")
                        .hasRole(HEADQUARTERS)

                        // 협력사·창고: 조회는 본사와 창고 관리자, 변경은 본사
                        .requestMatchers(HttpMethod.GET, "/api/v1/partners/**", "/api/v1/storages/**")
                        .hasAnyRole(HEADQUARTERS, WAREHOUSE_MANAGER)
                        .requestMatchers("/api/v1/partners/**", "/api/v1/storages/**")
                        .hasRole(HEADQUARTERS)

                        // 입출고·재고·로트·검사: 본사와 창고 관리자
                        .requestMatchers(
                                "/api/v1/lots/**",
                                "/api/v1/inbounds/**",
                                "/api/v1/quality-inspections/**",
                                "/api/v1/inventories/**",
                                "/api/v1/outbounds/**")
                        .hasAnyRole(HEADQUARTERS, WAREHOUSE_MANAGER)

                        // 발주: 준비·출하는 창고, 그 외는 가맹점
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/orders/*/prepare", "/api/v1/orders/*/ship")
                        .hasAnyRole(HEADQUARTERS, WAREHOUSE_MANAGER)
                        .requestMatchers("/api/v1/orders/**")
                        .hasAnyRole(HEADQUARTERS, FRANCHISE_MANAGER)

                        // 운영 관리(실패 이벤트 재처리 등): 본사만
                        .requestMatchers("/api/v1/admin/**").hasRole(HEADQUARTERS)

                        .anyRequest().denyAll()
                );

        return http.build();
    }
}
