package com.expense.tracker.application.adapter.outgoing.db

import com.expense.tracker.domain.db.Transaction
import com.expense.tracker.domain.db.TransactionType
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
    
    // Find by user and type
    fun findByUserIdAndTypeOrderByTransactionDateDescCreatedAtDesc(
        userId: UUID, 
        type: TransactionType
    ): Flow<Transaction>
    
    // Find by user and date range
    fun findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Transaction>
    
    // Recent transactions
    fun findTop10ByUserIdOrderByCreatedAtDesc(userId: UUID): Flow<Transaction>
    
    // Summary queries
    @Query("""
        SELECT SUM(amount) 
        FROM transactions 
        WHERE user_id = :userId AND type = :type
    """)
    suspend fun sumAmountByUserIdAndType(userId: UUID, type: TransactionType): BigDecimal?
    
    @Query("""
        SELECT COUNT(*) 
        FROM transactions 
        WHERE user_id = :userId AND type = :type
    """)
    suspend fun countByUserIdAndType(userId: UUID, type: TransactionType): Long
    
    // Complex search query
    @Query("""
        SELECT * FROM transactions 
        WHERE user_id = :userId
        AND (:categoryId IS NULL OR category_id = :categoryId)
        AND (:type IS NULL OR type = :type)
        AND (:description IS NULL OR LOWER(description) LIKE LOWER(CONCAT('%', :description, '%')))
        AND (:minAmount IS NULL OR amount >= :minAmount)
        AND (:maxAmount IS NULL OR amount <= :maxAmount)
        AND (:startDate IS NULL OR transaction_date >= :startDate)
        AND (:endDate IS NULL OR transaction_date <= :endDate)
        ORDER BY transaction_date DESC, created_at DESC
    """)
    fun searchTransactions(
        userId: UUID,
        categoryId: UUID?,
        type: TransactionType?,
        description: String?,
        minAmount: BigDecimal?,
        maxAmount: BigDecimal?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Flow<Transaction>
    
    // Check if user owns transaction
    suspend fun existsByIdAndUserId(id: String, userId: UUID): Boolean
} 