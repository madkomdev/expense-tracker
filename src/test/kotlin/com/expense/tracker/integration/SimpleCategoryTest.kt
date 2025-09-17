package com.expense.tracker.integration

import com.expense.tracker.config.TestJwtConfig
import com.expense.tracker.config.TestSecurityConfig
import com.expense.tracker.config.TestUserAccessService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import org.springframework.beans.factory.annotation.Autowired

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig::class, TestUserAccessService::class, TestJwtConfig::class)
@AutoConfigureWebTestClient
@Testcontainers
class SimpleCategoryTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

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
            
            // JWT config
            registry.add("app.jwt.private-key") { "" }
            registry.add("app.jwt.public-key") { "" }
        }
    }

    @Test
    fun `should get categories without authentication error`() {
        webTestClient.get()
            .uri("/api/categories")
            .exchange()
            .expectStatus().isOk
    }
}
