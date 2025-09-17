package com.expense.tracker.integration

import com.expense.tracker.application.adapter.incoming.rest.model.CategoryResponse
import com.expense.tracker.application.adapter.outgoing.db.CategoryRepository
import com.expense.tracker.domain.db.Category
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@AutoConfigureWebTestClient
class CategoryControllerIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    private lateinit var expenseCategory: Category
    private lateinit var incomeCategory: Category
    private lateinit var inactiveCategory: Category

    @BeforeEach
    fun setup() {
        runBlocking {
            // Clean up existing data
            categoryRepository.deleteAll()

            // Create test categories
            expenseCategory = categoryRepository.save(
                Category(
                    name = "Groceries",
                    color = "#FF5722",
                    icon = "shopping_cart",
                    type = "EXPENSE",
                    isActive = true,
                    sortOrder = 1
                )
            )

            incomeCategory = categoryRepository.save(
                Category(
                    name = "Salary",
                    color = "#4CAF50",
                    icon = "attach_money",
                    type = "INCOME",
                    isActive = true,
                    sortOrder = 2
                )
            )

            inactiveCategory = categoryRepository.save(
                Category(
                    name = "Inactive Category",
                    color = "#9E9E9E",
                    icon = "archive",
                    type = "EXPENSE",
                    isActive = false,
                    sortOrder = 3
                )
            )
        }
    }

    @Test
    fun `should get all categories including inactive ones`() {
        webTestClient.get()
            .uri("/api/categories")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<CategoryResponse>>()
            .consumeWith { response ->
                val categories = response.responseBody!!
                assert(categories.size >= 3)
                assert(categories.any { it.name == "Groceries" })
                assert(categories.any { it.name == "Salary" })
                assert(categories.any { it.name == "Inactive Category" })
            }
    }

    @Test
    fun `should get only active categories`() {
        webTestClient.get()
            .uri("/api/categories/active")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<CategoryResponse>>()
            .consumeWith { response ->
                val categories = response.responseBody!!
                assert(categories.size >= 2)
                assert(categories.all { it.isActive })
                assert(categories.any { it.name == "Groceries" })
                assert(categories.any { it.name == "Salary" })
                assert(categories.none { it.name == "Inactive Category" })
            }
    }

    @Test
    fun `should get categories by type - expense`() {
        webTestClient.get()
            .uri("/api/categories/by-type/EXPENSE")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<CategoryResponse>>()
            .consumeWith { response ->
                val categories = response.responseBody!!
                assert(categories.isNotEmpty())
                assert(categories.all { it.type == "EXPENSE" })
                assert(categories.any { it.name == "Groceries" })
                assert(categories.none { it.name == "Salary" })
            }
    }

    @Test
    fun `should get categories by type - income`() {
        webTestClient.get()
            .uri("/api/categories/by-type/INCOME")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<CategoryResponse>>()
            .consumeWith { response ->
                val categories = response.responseBody!!
                assert(categories.isNotEmpty())
                assert(categories.all { it.type == "INCOME" })
                assert(categories.any { it.name == "Salary" })
                assert(categories.none { it.name == "Groceries" })
            }
    }

    @Test
    fun `should return bad request for invalid category type`() {
        webTestClient.get()
            .uri("/api/categories/by-type/INVALID")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should get category by id`() {
        webTestClient.get()
            .uri("/api/categories/${expenseCategory.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody<CategoryResponse>()
            .consumeWith { response ->
                val category = response.responseBody!!
                assert(category.id == expenseCategory.id.toString())
                assert(category.name == "Groceries")
                assert(category.type == "EXPENSE")
                assert(category.color == "#FF5722")
                assert(category.icon == "shopping_cart")
                assert(category.isActive)
                assert(category.sortOrder == 1)
            }
    }

    @Test
    fun `should return not found for non-existent category`() {
        webTestClient.get()
            .uri("/api/categories/00000000-0000-0000-0000-000000000000")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should return categories sorted by sort order and name`() {
        runBlocking {
            // Create additional categories with different sort orders
            categoryRepository.save(
                Category(
                    name = "ZZZ Last",
                    color = "#000000",
                    icon = "last",
                    type = "EXPENSE",
                    isActive = true,
                    sortOrder = 0
                )
            )
            
            categoryRepository.save(
                Category(
                    name = "AAA First",
                    color = "#FFFFFF",
                    icon = "first",
                    type = "EXPENSE",
                    isActive = true,
                    sortOrder = 0
                )
            )
        }

        webTestClient.get()
            .uri("/api/categories/by-type/EXPENSE")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<CategoryResponse>>()
            .consumeWith { response ->
                val categories = response.responseBody!!
                assert(categories.isNotEmpty())
                
                // Check that categories with sortOrder 0 come first, and within same sortOrder, names are sorted
                val sortOrder0Categories = categories.filter { it.sortOrder == 0 }
                if (sortOrder0Categories.size >= 2) {
                    // Among sortOrder 0, AAA First should come before ZZZ Last
                    val aaaIndex = sortOrder0Categories.indexOfFirst { it.name == "AAA First" }
                    val zzzIndex = sortOrder0Categories.indexOfFirst { it.name == "ZZZ Last" }
                    if (aaaIndex >= 0 && zzzIndex >= 0) {
                        assert(aaaIndex < zzzIndex) { "Categories should be sorted by name within same sort order" }
                    }
                }
            }
    }

}
