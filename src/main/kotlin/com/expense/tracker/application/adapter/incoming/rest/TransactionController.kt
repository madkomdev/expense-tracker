package com.expense.tracker.application.adapter.incoming.rest

import com.expense.tracker.application.adapter.incoming.rest.model.*
import com.expense.tracker.application.service.TransactionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/users/{userId}/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping
    suspend fun createTransaction(
        @PathVariable userId: String,
        @RequestBody transaction: Transaction
    ): ResponseEntity<Any> {
        return try {
            val userUUID = UUID.fromString(userId)
            val createdTransaction = transactionService.createTransaction(userUUID, transaction)
            ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to create transaction"))
        }
    }

    @GetMapping
    suspend fun getUserTransactions(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<TransactionResponse>> {
        val userUUID = UUID.fromString(userId)
        val transactions = transactionService.getUserTransactions(userUUID, page, size)
        return ResponseEntity.ok(transactions)
    }

    @GetMapping("/{id}")
    suspend fun getTransactionById(
        @PathVariable userId: String,
        @PathVariable id: String
    ): ResponseEntity<Any> {
        return try {
            val userUUID = UUID.fromString(userId)
            val transaction = transactionService.getTransactionById(userUUID, id)
            if (transaction != null) {
                ResponseEntity.ok(transaction)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/category/{categoryId}")
    suspend fun getTransactionsByCategory(
        @PathVariable userId: String,
        @PathVariable categoryId: String
    ): ResponseEntity<Any> {
        return try {
            val userUUID = UUID.fromString(userId)
            val transactions = transactionService.getTransactionsByCategory(userUUID, categoryId)
            ResponseEntity.ok(transactions)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/type/{type}")
    suspend fun getTransactionsByType(
        @PathVariable userId: String,
        @PathVariable type: String
    ): ResponseEntity<Any> {
        return try {
            val userUUID = UUID.fromString(userId)
            val transactions = transactionService.getTransactionsByType(userUUID, type)
            ResponseEntity.ok(transactions)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/date-range")
    suspend fun getTransactionsByDateRange(
        @PathVariable userId: String,
        @RequestParam startDate: String,
        @RequestParam endDate: String
    ): ResponseEntity<Any> {
        return try {
            val userUUID = UUID.fromString(userId)
            val transactions = transactionService.getTransactionsByDateRange(userUUID, startDate, endDate)
            ResponseEntity.ok(transactions)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/recent")
    suspend fun getRecentTransactions(
        @PathVariable userId: String
    ): ResponseEntity<List<TransactionResponse>> {
        val userUUID = UUID.fromString(userId)
        val transactions = transactionService.getRecentTransactions(userUUID)
        return ResponseEntity.ok(transactions)
    }

    @PostMapping("/search")
    suspend fun searchTransactions(
        @PathVariable userId: String,
        @RequestBody searchRequest: TransactionSearchRequest
    ): ResponseEntity<List<TransactionResponse>> {
        val userUUID = UUID.fromString(userId)
        val transactions = transactionService.searchTransactions(userUUID, searchRequest)
        return ResponseEntity.ok(transactions)
    }

    @GetMapping("/summary")
    suspend fun getTransactionSummary(
        @PathVariable userId: String
    ): ResponseEntity<TransactionSummary> {
        val userUUID = UUID.fromString(userId)
        val summary = transactionService.getTransactionSummary(userUUID)
        return ResponseEntity.ok(summary)
    }

    @PutMapping("/{id}")
    suspend fun updateTransaction(
        @PathVariable userId: String,
        @PathVariable id: String,
        @RequestBody updateRequest: TransactionUpdateRequest
    ): ResponseEntity<Any> {
        return try {
            val userUUID = UUID.fromString(userId)
            val updatedTransaction = transactionService.updateTransaction(userUUID, id, updateRequest)
            if (updatedTransaction != null) {
                ResponseEntity.ok(updatedTransaction)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    suspend fun deleteTransaction(
        @PathVariable userId: String,
        @PathVariable id: String
    ): ResponseEntity<Any> {
        val userUUID = UUID.fromString(userId)
        val deleted = transactionService.deleteTransaction(userUUID, id)
        return if (deleted) {
            ResponseEntity.ok(mapOf("message" to "Transaction deleted successfully"))
        } else {
            ResponseEntity.notFound().build()
        }
    }
} 