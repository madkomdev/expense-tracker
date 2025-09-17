package com.expense.tracker.integration

import com.expense.tracker.application.adapter.incoming.rest.model.LoginRequest
import com.expense.tracker.application.adapter.incoming.rest.model.User
import com.expense.tracker.application.adapter.incoming.rest.model.UserProfile
import com.expense.tracker.application.adapter.outgoing.db.UserRepository
import com.expense.tracker.application.service.JwtService
import com.expense.tracker.domain.db.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import com.expense.tracker.domain.db.User as DBUser

@AutoConfigureWebTestClient
class UserManagementControllerIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        runBlocking {
            // Clean up existing data
            userRepository.deleteAll()
        }
    }

    @Test
    fun `should register new user successfully`() {
        val newUser = User(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "+1234567890",
            address = "123 Main St",
            username = "johndoe",
            password = "SecurePassword123!"
        )

        webTestClient.post()
            .uri("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(newUser)
            .exchange()
            .expectStatus().isOk
            .expectBody<UserProfile>()
            .consumeWith { response ->
                val userProfile = response.responseBody!!
                assert(userProfile.name == "John Doe")
                assert(userProfile.email == "john.doe@example.com")
                assert(userProfile.phone == "+1234567890")
                assert(userProfile.address == "123 Main St")
                assert(userProfile.username == "johndoe")
                assert(userProfile.role == UserRole.USER)
            }
    }


    @Test
    fun `should login user with correct credentials`() {
        // First create a user
        val testUser = runBlocking {
            userRepository.save(
                DBUser(
                    firstName = "Login",
                    lastName = "Test",
                    email = "login@example.com",
                    username = "logintest",
                    passwordHash = "\$2a\$10\$efrZyZS5Js.dvOyKLta4sOnjMfk7vcJ0Gd1460HPNKLXSlMt22ibi", // "password" encoded
                    role = UserRole.USER
                )
            )
        }

        val loginRequest = LoginRequest(
            username = "logintest",
            password = "password"
        )

        webTestClient.post()
            .uri("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody<JwtService.LoginResponse>()
            .consumeWith { response ->
                val loginResponse = response.responseBody!!
                assert(loginResponse.token.isNotEmpty())
                assert(loginResponse.expiresIn == 3600L)
                assert(loginResponse.user.email == "login@example.com")
                assert(loginResponse.user.username == "logintest")
                assert(loginResponse.user.role == UserRole.USER)
            }
    }


    @Test
    fun `should reject login with incorrect password`() {
        // Create a user
        runBlocking {
            userRepository.save(
                DBUser(
                    firstName = "Wrong",
                    lastName = "Password",
                    email = "wrong@example.com",
                    username = "wrongpass",
                    passwordHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.", // "password" encoded
                    role = UserRole.USER
                )
            )
        }

        val loginRequest = LoginRequest(
            username = "wrongpass",
            password = "wrongpassword"
        )

        webTestClient.post()
            .uri("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should reject login with non-existent user`() {
        val loginRequest = LoginRequest(
            username = "nonexistent",
            password = "password"
        )

        webTestClient.post()
            .uri("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isUnauthorized
    }

}
