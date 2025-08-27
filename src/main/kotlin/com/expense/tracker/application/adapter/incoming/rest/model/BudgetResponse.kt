package com.expense.tracker.application.adapter.incoming.rest.model

import java.math.BigDecimal

data class BudgetResponse(
    val id: String,
    val userId: String,
    val categoryId: String?,
    val categoryName: String?, // Resolved from category
    val categoryColor: String?, // For UI display
    val categoryIcon: String?,  // For UI display
    val name: String,
    val amount: BigDecimal,
    val spentAmount: BigDecimal, // Calculated from transactions
    val remainingAmount: BigDecimal, // amount - spentAmount
    val period: String,
    val startDate: String,
    val endDate: String,
    val createdAt: String
)
