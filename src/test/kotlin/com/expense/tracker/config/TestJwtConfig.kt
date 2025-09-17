package com.expense.tracker.config

import com.expense.tracker.application.service.JwtKeyManager
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestJwtConfig {

    @Bean
    @Primary
    fun testJwtKeyManager(): JwtKeyManager {
        // Create JwtKeyManager with empty keys - it will auto-generate development keys
        return JwtKeyManager("", "", "test-key", "test")
    }
}
