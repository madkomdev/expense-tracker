package com.expense.tracker.domain.db

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Table("transactions")
data class Transaction @PersistenceCreator constructor(
    @Id val id: UUID?,
    @Column("user_id") val userId: UUID,
    @Column("category_id") val categoryId: UUID,
    @Column("amount") val amount: BigDecimal,
    @Column("description") val description: String,
    @Column("transaction_date") val transactionDate: LocalDate,
    @Column("type") val type: String,
    @Column("created_at") val createdAt: LocalDateTime? = null,
    @Column("updated_at") val updatedAt: LocalDateTime? = null
) {
    // Secondary constructor for creating new entities
    constructor(
        userId: UUID,
        categoryId: UUID,
        amount: BigDecimal,
        description: String,
        transactionDate: LocalDate = LocalDate.now(),
        type: String
    ) : this(null, userId, categoryId, amount, description, transactionDate, type)
} 