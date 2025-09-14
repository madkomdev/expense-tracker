package com.expense.tracker.infrastructure.context

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter

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
                authorize("/actuator/**", hasRole("ADMIN"))
                
                // All other requests must be authenticated
                authorize(anyExchange, authenticated)
            }

            oauth2ResourceServer {
                jwt { 
                    // JWT will be validated against the JWKS endpoint
                }
            }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}

