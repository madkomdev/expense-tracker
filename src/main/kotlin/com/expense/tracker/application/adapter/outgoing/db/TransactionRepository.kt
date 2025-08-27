package com.expense.tracker.application.adapter.outgoing.db

import com.expense.tracker.domain.db.Transaction

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Repository
interface TransactionRepository: CoroutineCrudRepository<Transaction, String> {
    
    // Find by user
    fun findByUserIdOrderByTransactionDateDescCreatedAtDesc(userId: UUID): Flow<Transaction>
    
    fun findByUserIdOrderByTransactionDateDescCreatedAtDesc(userId: UUID, pageable: Pageable): Flow<Transaction>
    
    // Find by user and category
    fun findByUserIdAndCategoryIdOrderByTransactionDateDescCreatedAtDesc(
        userId: UUID, 
        categoryId: UUID
    ): Flow<Transaction>

    @Query("""
        SELECT t.* FROM transactions t
        JOIN categories c ON t.category_id = c.id
        WHERE t.user_id = :userId
        AND c.type = :type
        ORDER BY t.transaction_date DESC, t.created_at DESC
    """)
    fun findByUserIdAndCategoryTypeOrderByTransactionDateDescCreatedAtDesc(
        userId: UUID, 
        type: String
    ): Flow<Transaction>
    
    // Find by user and date range
    fun findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Transaction>
    
    // Recent transactions
    fun findTop10ByUserIdOrderByCreatedAtDesc(userId: UUID): Flow<Transaction>
    
    // Summary queries using category type
    @Query("""
        SELECT SUM(t.amount) 
        FROM transactions t
        JOIN categories c ON t.category_id = c.id
        WHERE t.user_id = :userId AND c.type = :type
    """)
    suspend fun sumAmountByUserIdAndCategoryType(userId: UUID, type: String): BigDecimal?
    
    @Query("""
        SELECT COUNT(*) 
        FROM transactions t
        JOIN categories c ON t.category_id = c.id
        WHERE t.user_id = :userId AND c.type = :type
    """)
    suspend fun countByUserIdAndCategoryType(userId: UUID, type: String): Long
    
    // Complex search query using category type
    @Query("""
        SELECT t.* FROM transactions t
        JOIN categories c ON t.category_id = c.id
        WHERE t.user_id = :userId
        AND (:categoryId IS NULL OR t.category_id = :categoryId)
        AND (:type IS NULL OR c.type = :type)
        AND (:description IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')))
        AND (:minAmount IS NULL OR t.amount >= :minAmount)
        AND (:maxAmount IS NULL OR t.amount <= :maxAmount)
        AND (:startDate IS NULL OR t.transaction_date >= :startDate)
        AND (:endDate IS NULL OR t.transaction_date <= :endDate)
        ORDER BY t.transaction_date DESC, t.created_at DESC
    """)
    fun searchTransactions(
        userId: UUID,
        categoryId: UUID?,
        type: String?,
        description: String?,
        minAmount: BigDecimal?,
        maxAmount: BigDecimal?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Flow<Transaction>
    
    // Check if user owns transaction
    suspend fun existsByIdAndUserId(id: String, userId: UUID): Boolean
} 