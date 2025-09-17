package com.expense.tracker.integration

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class ContextLoadTest {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            val jdbcUrl = postgres.jdbcUrl
            val r2dbcUrl = jdbcUrl.replace("jdbc:postgresql://", "r2dbc:postgresql://")
            
            registry.add("spring.r2dbc.url") { r2dbcUrl }
            registry.add("spring.r2dbc.username") { postgres.username }
            registry.add("spring.r2dbc.password") { postgres.password }
            
            registry.add("spring.flyway.url") { jdbcUrl }
            registry.add("spring.flyway.user") { postgres.username }
            registry.add("spring.flyway.password") { postgres.password }
            
            // Provide empty JWT keys to prevent initialization errors
            registry.add("app.jwt.private-key") { "" }
            registry.add("app.jwt.public-key") { "" }
        }
    }

    @Test
    fun `context loads successfully`() {
        println("Context loading successful")
    }
}
