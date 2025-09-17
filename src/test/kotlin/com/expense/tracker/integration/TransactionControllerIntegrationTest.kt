package com.expense.tracker.integration

import com.expense.tracker.application.adapter.incoming.rest.model.*
import com.expense.tracker.application.adapter.outgoing.db.CategoryRepository
import com.expense.tracker.application.adapter.outgoing.db.TransactionRepository
import com.expense.tracker.application.adapter.outgoing.db.UserRepository
import com.expense.tracker.domain.db.Category
import com.expense.tracker.domain.db.Transaction
import com.expense.tracker.domain.db.User
import com.expense.tracker.domain.db.UserRole
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@AutoConfigureWebTestClient
class TransactionControllerIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private lateinit var testUser: User
    private lateinit var testCategory: Category

    @BeforeEach
    fun setup() {
        runBlocking {
            // Clean up existing data
            transactionRepository.deleteAll()
            categoryRepository.deleteAll()
            userRepository.deleteAll()

            // Create test user
            testUser = userRepository.save(
                User(
                    firstName = "Test",
                    lastName = "User",
                    email = "test@example.com",
                    username = "testuser",
                    passwordHash = "hashedpassword",
                    role = UserRole.USER
                )
            )

            // Create test category
            testCategory = categoryRepository.save(
                Category(
                    name = "Groceries",
                    color = "#FF5722",
                    icon = "shopping_cart",
                    type = "EXPENSE",
                    isActive = true,
                    sortOrder = 1
                )
            )
        }
    }

    @Test
    fun `should create transaction successfully`() {
        val transaction = com.expense.tracker.application.adapter.incoming.rest.model.Transaction(
            categoryId = testCategory.id.toString(),
            amount = BigDecimal("50.99"),
            description = "Weekly groceries",
            transactionDate = "2024-01-15"
        )

        webTestClient.post()
            .uri("/api/users/${testUser.id}/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(transaction)
            .exchange()
            .expectStatus().isCreated
            .expectBody<TransactionResponse>()
            .consumeWith { response ->
                val body = response.responseBody!!
                assert(body.amount == BigDecimal("50.99"))
                assert(body.description == "Weekly groceries")
                assert(body.categoryName == "Groceries")
                assert(body.type == "EXPENSE")
            }
    }

    @Test
    fun `should return bad request when category not found`() {
        val transaction = com.expense.tracker.application.adapter.incoming.rest.model.Transaction(
            categoryId = UUID.randomUUID().toString(),
            amount = BigDecimal("50.99"),
            description = "Invalid category",
            transactionDate = "2024-01-15"
        )

        webTestClient.post()
            .uri("/api/users/${testUser.id}/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(transaction)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should get user transactions with pagination`() {
        runBlocking {
            // Create multiple test transactions
            repeat(3) { index ->
                val transaction = Transaction(
                    userId = testUser.id!!,
                    categoryId = testCategory.id!!,
                    amount = BigDecimal("${10 + index * 5}.99"),
                    description = "Test transaction $index",
                    transactionDate = LocalDate.now().minusDays(index.toLong())
                )
                transactionRepository.save(transaction)
            }
        }

        webTestClient.get()
            .uri("/api/users/${testUser.id}/transactions?page=0&size=2")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<TransactionResponse>>()
            .consumeWith { response ->
                val transactions = response.responseBody!!
                assert(transactions.size == 2)
                assert(transactions.all { it.userId == testUser.id.toString() })
            }
    }

    @Test
    fun `should get transaction by id`() {
        val savedTransaction = runBlocking {
            transactionRepository.save(
                Transaction(
                    userId = testUser.id!!,
                    categoryId = testCategory.id!!,
                    amount = BigDecimal("25.50"),
                    description = "Test transaction",
                    transactionDate = LocalDate.now()
                )
            )
        }

        webTestClient.get()
            .uri("/api/users/${testUser.id}/transactions/${savedTransaction.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody<TransactionResponse>()
            .consumeWith { response ->
                val body = response.responseBody!!
                assert(body.id == savedTransaction.id.toString())
                assert(body.amount == BigDecimal("25.50"))
                assert(body.description == "Test transaction")
            }
    }

    @Test
    fun `should return not found for non-existent transaction`() {
        webTestClient.get()
            .uri("/api/users/${testUser.id}/transactions/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should get transactions by category`() {
        runBlocking {
            transactionRepository.save(
                Transaction(
                    userId = testUser.id!!,
                    categoryId = testCategory.id!!,
                    amount = BigDecimal("30.00"),
                    description = "Category test",
                    transactionDate = LocalDate.now()
                )
            )
        }

        webTestClient.get()
            .uri("/api/users/${testUser.id}/transactions/category/${testCategory.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<TransactionResponse>>()
            .consumeWith { response ->
                val transactions = response.responseBody!!
                assert(transactions.isNotEmpty())
                assert(transactions.all { it.categoryId == testCategory.id.toString() })
            }
    }

    @Test
    fun `should get transactions by type`() {
        runBlocking {
            transactionRepository.save(
                Transaction(
                    userId = testUser.id!!,
                    categoryId = testCategory.id!!,
                    amount = BigDecimal("40.00"),
                    description = "Type test",
                    transactionDate = LocalDate.now()
                )
            )
        }

        webTestClient.get()
            .uri("/api/users/${testUser.id}/transactions/type/EXPENSE")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<TransactionResponse>>()
            .consumeWith { response ->
                val transactions = response.responseBody!!
                assert(transactions.isNotEmpty())
                assert(transactions.all { it.type == "EXPENSE" })
            }
    }

    @Test
    fun `should get transactions by date range`() {
        runBlocking {
            transactionRepository.save(
                Transaction(
                    userId = testUser.id!!,
                    categoryId = testCategory.id!!,
                    amount = BigDecimal("35.00"),
                    description = "Date range test",
                    transactionDate = LocalDate.now()
                )
            )
        }

        val startDate = LocalDate.now().minusDays(1).toString()
        val endDate = LocalDate.now().plusDays(1).toString()

        webTestClient.get()
            .uri("/api/users/${testUser.id}/transactions/date-range?startDate=$startDate&endDate=$endDate")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<TransactionResponse>>()
            .consumeWith { response ->
                val transactions = response.responseBody!!
                assert(transactions.isNotEmpty())
            }
    }

    @Test
    fun `should get recent transactions`() {
        runBlocking {
            transactionRepository.save(
                Transaction(
                    userId = testUser.id!!,
                    categoryId = testCategory.id!!,
                    amount = BigDecimal("20.00"),
                    description = "Recent test",
                    transactionDate = LocalDate.now()
                )
            )
        }

        webTestClient.get()
            .uri("/api/users/${testUser.id}/transactions/recent")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<TransactionResponse>>()
            .consumeWith { response ->
                val transactions = response.responseBody!!
                assert(transactions.isNotEmpty())
            }
    }

    @Test
    fun `should search transactions`() {
        runBlocking {
            transactionRepository.save(
                Transaction(
                    userId = testUser.id!!,
                    categoryId = testCategory.id!!,
                    amount = BigDecimal("45.00"),
                    description = "Search test transaction",
                    transactionDate = LocalDate.now()
                )
            )
        }

        val searchRequest = TransactionSearchRequest(
            description = "Search",
            minAmount = BigDecimal("40.00"),
            maxAmount = BigDecimal("50.00")
        )

        webTestClient.post()
            .uri("/api/users/${testUser.id}/transactions/search")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(searchRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody<List<TransactionResponse>>()
            .consumeWith { response ->
                val transactions = response.responseBody!!
                assert(transactions.isNotEmpty())
            }
    }

    @Test
    fun `should get transaction summary`() {
        runBlocking {
            transactionRepository.save(
                Transaction(
                    userId = testUser.id!!,
                    categoryId = testCategory.id!!,
                    amount = BigDecimal("100.00"),
                    description = "Summary test",
                    transactionDate = LocalDate.now()
                )
            )
        }

        webTestClient.get()
            .uri("/api/users/${testUser.id}/transactions/summary")
            .exchange()
            .expectStatus().isOk
            .expectBody<TransactionSummary>()
            .consumeWith { response ->
                val summary = response.responseBody!!
                assert(summary.totalExpenses >= BigDecimal("100.00"))
                assert(summary.transactionCount >= 1)
            }
    }

    @Test
    fun `should update transaction`() {
        val savedTransaction = runBlocking {
            transactionRepository.save(
                Transaction(
                    userId = testUser.id!!,
                    categoryId = testCategory.id!!,
                    amount = BigDecimal("60.00"),
                    description = "Original description",
                    transactionDate = LocalDate.now()
                )
            )
        }

        val updateRequest = TransactionUpdateRequest(
            amount = BigDecimal("65.00"),
            description = "Updated description"
        )

        webTestClient.put()
            .uri("/api/users/${testUser.id}/transactions/${savedTransaction.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody<TransactionResponse>()
            .consumeWith { response ->
                val body = response.responseBody!!
                assert(body.amount == BigDecimal("65.00"))
                assert(body.description == "Updated description")
            }
    }

    @Test
    fun `should delete transaction`() {
        val savedTransaction = runBlocking {
            transactionRepository.save(
                Transaction(
                    userId = testUser.id!!,
                    categoryId = testCategory.id!!,
                    amount = BigDecimal("15.00"),
                    description = "To be deleted",
                    transactionDate = LocalDate.now()
                )
            )
        }

        webTestClient.delete()
            .uri("/api/users/${testUser.id}/transactions/${savedTransaction.id}")
            .exchange()
            .expectStatus().isOk

        // Verify transaction is deleted
        webTestClient.get()
            .uri("/api/users/${testUser.id}/transactions/${savedTransaction.id}")
            .exchange()
            .expectStatus().isNotFound
    }
}
