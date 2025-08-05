package com.expense.tracker.infrastructure.context


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter

@Configuration
@EnableWebFluxSecurity
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
                authorize("/.well-known/jwks.json", permitAll)
                authorize("/api-docs**", permitAll)
                authorize("/actuator/**", permitAll)
                authorize("/api/**", permitAll)
            }

            oauth2ResourceServer {
                jwt { }
            }
        }
    }
}

