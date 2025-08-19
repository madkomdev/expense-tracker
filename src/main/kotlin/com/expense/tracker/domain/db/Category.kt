package com.expense.tracker.domain.db

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("categories")
data class Category @PersistenceCreator constructor(
    @Id val id: UUID?,
    @Column("name") val name: String,
    @Column("color") val color: String,
    @Column("icon") val icon: String,
    @Column("type") val type: TransactionType,
    @Column("is_active") val isActive: Boolean = true,
    @Column("sort_order") val sortOrder: Int = 0,
    @Column("created_at") val createdAt: LocalDateTime? = null,
    @Column("updated_at") val updatedAt: LocalDateTime? = null
) {
    // Secondary constructor for creating new entities
    constructor(
        name: String,
        color: String = "#6B7280",
        icon: String = "folder",
        type: TransactionType,
        isActive: Boolean = true,
        sortOrder: Int = 0
    ) : this(null, name, color, icon, type, isActive, sortOrder)
}

enum class TransactionType {
    EXPENSE, INCOME
} 