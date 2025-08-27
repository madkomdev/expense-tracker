package com.expense.tracker.application.adapter.outgoing.db

import com.expense.tracker.domain.db.Budget
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
interface BudgetRepository : CoroutineCrudRepository<Budget, String> {
    
    // Basic user budget queries
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID): Flow<Budget>
    fun findByUserIdAndCategoryIdOrderByCreatedAtDesc(userId: UUID, categoryId: UUID): Flow<Budget>
    
    // Date range queries for active budgets
    @Query("""
        SELECT * FROM budgets 
        WHERE user_id = :userId 
        AND start_date <= :currentDate 
        AND end_date >= :currentDate
        ORDER BY created_at DESC
    """)
    fun findActiveBudgetsByUserId(userId: UUID, currentDate: LocalDate): Flow<Budget>
    
    // Check ownership for security
    suspend fun existsByIdAndUserId(id: String, userId: UUID): Boolean
    
    // Check for overlapping budgets
    suspend fun existsByUserIdAndCategoryIdAndPeriodAndStartDate(
        userId: UUID, categoryId: UUID?, period: String, startDate: LocalDate
    ): Boolean
}
