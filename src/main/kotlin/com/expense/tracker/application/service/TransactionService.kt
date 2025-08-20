package com.expense.tracker.application.service

import com.expense.tracker.application.adapter.incoming.rest.model.*
import com.expense.tracker.application.adapter.outgoing.db.CategoryRepository
import com.expense.tracker.application.adapter.outgoing.db.TransactionRepository
import com.expense.tracker.application.service.mappers.toDBTransaction
import com.expense.tracker.application.service.mappers.toTransactionResponse
import com.expense.tracker.domain.db.TransactionType
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {

    suspend fun createTransaction(userId: UUID, transaction: Transaction): TransactionResponse {
        // Validate category exists
        val category = categoryRepository.findById(transaction.categoryId)
            ?: throw IllegalArgumentException("Category not found")
        
        // Validate type matches category type
        if (category.type.name != transaction.type) {
            throw IllegalArgumentException("Transaction type must match category type")
        }
        
        val newTransaction = transaction.toDBTransaction(userId)
        val savedTransaction = transactionRepository.save(newTransaction)
        return savedTransaction.toTransactionResponse(category)
    }

    suspend fun getUserTransactions(userId: UUID, page: Int = 0, size: Int = 20): List<TransactionResponse> {
        return transactionRepository.findByUserIdOrderByTransactionDateDescCreatedAtDesc(userId)
            .toList()
            .drop(page * size)
            .take(size)
            .map { transaction ->
                val category = categoryRepository.findById(transaction.categoryId.toString())!!
                transaction.toTransactionResponse(category)
            }
    }

    suspend fun getTransactionById(userId: UUID, transactionId: String): TransactionResponse? {
        val transaction = transactionRepository.findById(transactionId) ?: return null
        
        // Ensure user owns this transaction
        if (transaction.userId != userId) {
            throw SecurityException("User does not own this transaction")
        }
        
        val category = categoryRepository.findById(transaction.categoryId.toString())!!
        return transaction.toTransactionResponse(category)
    }

    suspend fun getTransactionsByCategory(userId: UUID, categoryId: String): List<TransactionResponse> {
        val categoryUUID = UUID.fromString(categoryId)
        val category = categoryRepository.findById(categoryId)
            ?: throw IllegalArgumentException("Category not found")
        
        return transactionRepository.findByUserIdAndCategoryIdOrderByTransactionDateDescCreatedAtDesc(userId, categoryUUID)
            .toList()
            .map { it.toTransactionResponse(category) }
    }

    suspend fun getTransactionsByType(userId: UUID, type: String): List<TransactionResponse> {
        val transactionType = TransactionType.valueOf(type.uppercase())
        return transactionRepository.findByUserIdAndTypeOrderByTransactionDateDescCreatedAtDesc(userId, transactionType.name)
            .toList()
            .map { transaction ->
                val category = categoryRepository.findById(transaction.categoryId.toString())!!
                transaction.toTransactionResponse(category)
            }
    }

    suspend fun getTransactionsByDateRange(
        userId: UUID, 
        startDate: String, 
        endDate: String
    ): List<TransactionResponse> {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        
        return transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
            userId, start, end
        )
            .toList()
            .map { transaction ->
                val category = categoryRepository.findById(transaction.categoryId.toString())!!
                transaction.toTransactionResponse(category)
            }
    }

    suspend fun getRecentTransactions(userId: UUID): List<TransactionResponse> {
        return transactionRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
            .toList()
            .map { transaction ->
                val category = categoryRepository.findById(transaction.categoryId.toString())!!
                transaction.toTransactionResponse(category)
            }
    }

    suspend fun searchTransactions(userId: UUID, searchRequest: TransactionSearchRequest): List<TransactionResponse> {
        val categoryId = searchRequest.categoryId?.let { UUID.fromString(it) }
        val type = searchRequest.type?.let { TransactionType.valueOf(it.uppercase()) }
        val startDate = searchRequest.startDate?.let { LocalDate.parse(it) }
        val endDate = searchRequest.endDate?.let { LocalDate.parse(it) }
        
        return transactionRepository.searchTransactions(
            userId = userId,
            categoryId = categoryId,
            type = type,
            description = searchRequest.description,
            minAmount = searchRequest.minAmount,
            maxAmount = searchRequest.maxAmount,
            startDate = startDate,
            endDate = endDate
        )
            .toList()
            .drop(searchRequest.page * searchRequest.size)
            .take(searchRequest.size)
            .map { transaction ->
                val category = categoryRepository.findById(transaction.categoryId.toString())!!
                transaction.toTransactionResponse(category)
            }
    }

    suspend fun getTransactionSummary(userId: UUID): TransactionSummary {
        val totalExpenses = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.EXPENSE) ?: BigDecimal.ZERO
        val totalIncome = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.INCOME) ?: BigDecimal.ZERO
        val expenseCount = transactionRepository.countByUserIdAndType(userId, TransactionType.EXPENSE)
        val incomeCount = transactionRepository.countByUserIdAndType(userId, TransactionType.INCOME)
        
        // TODO: Implement category breakdown query
        val categoryBreakdown = emptyList<CategorySummary>()
        
        return TransactionSummary(
            totalExpenses = totalExpenses,
            totalIncome = totalIncome,
            netAmount = totalIncome.subtract(totalExpenses),
            transactionCount = expenseCount + incomeCount,
            expenseCount = expenseCount,
            incomeCount = incomeCount,
            categoryBreakdown = categoryBreakdown
        )
    }

    suspend fun updateTransaction(userId: UUID, transactionId: String, updateRequest: TransactionUpdateRequest): TransactionResponse? {
        val existingTransaction = transactionRepository.findById(transactionId) ?: return null
        
        // Ensure user owns this transaction
        if (existingTransaction.userId != userId) {
            throw SecurityException("User does not own this transaction")
        }
        
        // Validate category if being updated
        val category = if (updateRequest.categoryId != null) {
            categoryRepository.findById(updateRequest.categoryId)
                ?: throw IllegalArgumentException("Category not found")
        } else {
            categoryRepository.findById(existingTransaction.categoryId.toString())!!
        }
        
        val updatedTransaction = existingTransaction.copy(
            categoryId = updateRequest.categoryId?.let { UUID.fromString(it) } ?: existingTransaction.categoryId,
            amount = updateRequest.amount ?: existingTransaction.amount,
            description = updateRequest.description ?: existingTransaction.description,
            transactionDate = updateRequest.transactionDate?.let { LocalDate.parse(it) } ?: existingTransaction.transactionDate,
            type = updateRequest.type?.let { TransactionType.valueOf(it.uppercase()).name } ?: existingTransaction.type
        )
        
        val savedTransaction = transactionRepository.save(updatedTransaction)
        return savedTransaction.toTransactionResponse(category)
    }

    suspend fun deleteTransaction(userId: UUID, transactionId: String): Boolean {
        return if (transactionRepository.existsByIdAndUserId(transactionId, userId)) {
            transactionRepository.deleteById(transactionId)
            true
        } else {
            false
        }
    }
} 