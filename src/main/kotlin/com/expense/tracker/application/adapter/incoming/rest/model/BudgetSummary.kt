package com.expense.tracker.application.adapter.incoming.rest.model

import java.math.BigDecimal

data class BudgetSummary(
    val totalBudgets: Int,
    val totalAllocated: BigDecimal,
    val totalSpent: BigDecimal,
    val totalRemaining: BigDecimal,
    val budgetsByCategory: List<CategoryBudgetSummary>
)

data class CategoryBudgetSummary(
    val categoryId: String?,
    val categoryName: String?,
    val totalAllocated: BigDecimal,
    val totalSpent: BigDecimal,
    val totalRemaining: BigDecimal,
    val budgetCount: Int
)
