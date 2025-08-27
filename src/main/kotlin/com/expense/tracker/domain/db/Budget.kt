package com.expense.tracker.domain.db

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Table("budgets")
data class Budget @PersistenceCreator constructor(
    @Id val id: UUID?,
    @Column("user_id") val userId: UUID,
    @Column("category_id") val categoryId: UUID?, // Nullable for overall budgets
    @Column("name") val name: String,
    @Column("amount") val amount: BigDecimal, // Allocated amount
    @Column("period") val period: String, // WEEKLY, MONTHLY, QUARTERLY, YEARLY
    @Column("start_date") val startDate: LocalDate,
    @Column("end_date") val endDate: LocalDate,
    @Column("created_at") val createdAt: LocalDateTime? = null,
    @Column("updated_at") val updatedAt: LocalDateTime? = null
) {
    // Secondary constructor for creating new entities
    constructor(
        userId: UUID,
        categoryId: UUID?,
        name: String,
        amount: BigDecimal,
        period: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) : this(null, userId, categoryId, name, amount, period, startDate, endDate)
}

// Period constants for type safety
object BudgetPeriod {
    const val WEEKLY = "WEEKLY"
    const val MONTHLY = "MONTHLY" 
    const val QUARTERLY = "QUARTERLY"
    const val YEARLY = "YEARLY"
    
    val ALL_PERIODS = setOf(WEEKLY, MONTHLY, QUARTERLY, YEARLY)
    
    fun isValid(period: String): Boolean = period in ALL_PERIODS
}
