package com.expense.tracker.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@TestConfiguration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class TestSecurityConfig {

    @Bean
    @Primary
    fun testSecurityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            csrf { disable() }
            cors { disable() }
            httpBasic { disable() }
            formLogin { disable() }
            logout { disable() }
            
            authorizeExchange {
                authorize(anyExchange, permitAll)
            }
        }
    }

    @Bean
    @Primary
    fun testPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
