package com.expense.tracker.infrastructure.context

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class ApiSecurityContext {

    @Bean
    fun apiSecurityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            csrf { disable() }
            logout { disable() }
            formLogin { disable() }

            headers {
                contentSecurityPolicy {
                    policyDirectives = "default-src 'self';"
                }
                frameOptions {
                    mode = XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN
                }
            }

            authorizeExchange {
                // Public endpoints
                authorize("/.well-known/jwks.json", permitAll)
                authorize("/api-docs/**", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/actuator/health", permitAll)
                
                // Authentication endpoints (public)
                authorize("/api/users/register", permitAll)
                authorize("/api/users/login", permitAll)
                
                // Admin-only endpoints
                authorize("/api/users", hasRole("ADMIN"))
                authorize("/api/categories", hasRole("ADMIN"))
                authorize("/api/categories/*", hasRole("ADMIN"))
                authorize("/api/categories/*", hasRole("ADMIN"))
                
                // Authenticated user endpoints
                authorize("/api/categories", hasAnyRole("ADMIN", "USER"))
                authorize("/api/users/*/profile", hasAnyRole("ADMIN", "USER"))
                authorize("/api/users/*/budgets/**", hasAnyRole("ADMIN", "USER"))
                authorize("/api/users/*/change-password", hasAnyRole("ADMIN", "USER"))
                authorize("/api/users/*/delete", hasAnyRole("ADMIN", "USER"))
                authorize("/api/transactions/**", hasAnyRole("ADMIN", "USER"))
                
                // Admin access to actuator endpoints
                authorize("/actuator/health", permitAll)
                authorize("/actuator/info", permitAll)
                authorize("/actuator/**", hasRole("ADMIN"))
                
                // All other requests must be authenticated
                authorize(anyExchange, authenticated)
            }

            oauth2ResourceServer {
                jwt { 
                    jwtAuthenticationConverter = jwtAuthenticationConverter()
                }
            }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun jwtAuthenticationConverter(): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        val jwtConverter = JwtAuthenticationConverter()
        
        // Extract authorities from the "authorities" claim in JWT
        val authoritiesConverter = JwtGrantedAuthoritiesConverter()
        authoritiesConverter.setAuthoritiesClaimName("authorities")
        authoritiesConverter.setAuthorityPrefix("")
        
        jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter)
        
        return ReactiveJwtAuthenticationConverterAdapter(jwtConverter)
    }
}

