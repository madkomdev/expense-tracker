package com.expense.tracker.integration

import com.expense.tracker.config.TestJwtConfig
import com.expense.tracker.config.TestSecurityConfig
import com.expense.tracker.config.TestUserAccessService
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig::class, TestUserAccessService::class, TestJwtConfig::class)
@Testcontainers
abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected var port: Int = 0

    companion object {
        @Container
        @JvmStatic
        val postgreSQLContainer = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")

        @BeforeAll
        @JvmStatic
        fun setupDatabase() {
            // Container will be started automatically by @Testcontainers
        }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            val jdbcUrl = postgreSQLContainer.jdbcUrl
            val r2dbcUrl = jdbcUrl.replace("jdbc:postgresql://", "r2dbc:postgresql://")
            
            registry.add("spring.r2dbc.url") { r2dbcUrl }
            registry.add("spring.r2dbc.username") { postgreSQLContainer.username }
            registry.add("spring.r2dbc.password") { postgreSQLContainer.password }
            
            registry.add("spring.flyway.url") { jdbcUrl }
            registry.add("spring.flyway.user") { postgreSQLContainer.username }
            registry.add("spring.flyway.password") { postgreSQLContainer.password }
            
            // Disable JWT validation for tests
            registry.add("app.jwt.private-key") { "" }
            registry.add("app.jwt.public-key") { "" }
        }
    }

    protected fun getBaseUrl(): String = "http://localhost:$port"
}
