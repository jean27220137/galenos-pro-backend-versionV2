package com.galenospro.gateway.config;

import com.galenospro.gateway.filter.JwtAuthenticationFilter;
import com.galenospro.gateway.filter.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayRoutingConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final RateLimitingFilter rateLimitingFilter;

    @Value("${gateway.services.auth}")
    private String authServiceUrl;

    @Value("${gateway.services.farmacia}")
    private String farmaciaServiceUrl;

    @Value("${gateway.services.almacen}")
    private String almacenServiceUrl;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // Rutas públicas de auth (sin filtro JWT)
                .route("auth-public", r -> r
                        .path("/api/auth/login", "/api/auth/register", "/api/auth/validate")
                        .filters(f -> f.filter(rateLimitingFilter))
                        .uri(authServiceUrl))

                // Rutas protegidas de auth (con filtro JWT)
                .route("auth-private", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .filter(jwtFilter)
                                .filter(rateLimitingFilter))
                        .uri(authServiceUrl))

                // Farmacia (protegida)
                .route("farmacia", r -> r
                        .path("/api/farmacia/**")
                        .filters(f -> f
                                .filter(jwtFilter)
                                .filter(rateLimitingFilter))
                        .uri(farmaciaServiceUrl))

                // Almacén (protegida)
                .route("almacen", r -> r
                        .path("/api/almacen/**")
                        .filters(f -> f
                                .filter(jwtFilter)
                                .filter(rateLimitingFilter))
                        .uri(almacenServiceUrl))

                .build();
    }
}
