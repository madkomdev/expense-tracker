package com.expense.tracker.application.adapter.incoming.rest

import com.expense.tracker.application.adapter.incoming.rest.model.*
import com.expense.tracker.application.service.BudgetService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/users/{userId}/budgets")
class BudgetController(
    private val budgetService: BudgetService
) {

    @PostMapping
    suspend fun createBudget(
        @PathVariable userId: String,
        @RequestBody budget: Budget
    ): ResponseEntity<Any> {
        return try {
            val userUUID = UUID.fromString(userId)
            val createdBudget = budgetService.createBudget(userUUID, budget)
            ResponseEntity.status(HttpStatus.CREATED).body(createdBudget)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to create budget"))
        }
    }

    @GetMapping
    suspend fun getUserBudgets(
        @PathVariable userId: String
    ): ResponseEntity<List<BudgetResponse>> {
        val userUUID = UUID.fromString(userId)
        val budgets = budgetService.getUserBudgets(userUUID)
        return ResponseEntity.ok(budgets)
    }

    @GetMapping("/{budgetId}")
    suspend fun getBudgetById(
        @PathVariable userId: String,
        @PathVariable budgetId: String
    ): ResponseEntity<Any> {
        return try {
            val userUUID = UUID.fromString(userId)
            val budget = budgetService.getBudgetById(userUUID, budgetId)
            if (budget != null) {
                ResponseEntity.ok(budget)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{budgetId}")
    suspend fun updateBudget(
        @PathVariable userId: String,
        @PathVariable budgetId: String,
        @RequestBody updateRequest: BudgetUpdateRequest
    ): ResponseEntity<Any> {
        return try {
            val userUUID = UUID.fromString(userId)
            val updatedBudget = budgetService.updateBudget(userUUID, budgetId, updateRequest)
            if (updatedBudget != null) {
                ResponseEntity.ok(updatedBudget)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{budgetId}")
    suspend fun deleteBudget(
        @PathVariable userId: String,
        @PathVariable budgetId: String
    ): ResponseEntity<Any> {
        val userUUID = UUID.fromString(userId)
        val deleted = budgetService.deleteBudget(userUUID, budgetId)
        return if (deleted) {
            ResponseEntity.ok(mapOf("message" to "Budget deleted successfully"))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/summary")
    suspend fun getBudgetSummary(
        @PathVariable userId: String
    ): ResponseEntity<BudgetSummary> {
        val userUUID = UUID.fromString(userId)
        val summary = budgetService.getBudgetSummary(userUUID)
        return ResponseEntity.ok(summary)
    }

    @GetMapping("/category/{categoryId}")
    suspend fun getBudgetsByCategory(
        @PathVariable userId: String,
        @PathVariable categoryId: String
    ): ResponseEntity<Any> {
        return try {
            val userUUID = UUID.fromString(userId)
            val budgets = budgetService.getBudgetsByCategory(userUUID, categoryId)
            ResponseEntity.ok(budgets)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
