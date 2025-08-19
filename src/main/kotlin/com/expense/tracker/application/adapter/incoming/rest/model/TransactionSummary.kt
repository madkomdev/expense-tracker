package com.expense.tracker.application.adapter.incoming.rest.model

import java.math.BigDecimal

data class TransactionSummary(
    val totalExpenses: BigDecimal,
    val totalIncome: BigDecimal,
    val netAmount: BigDecimal,
    val transactionCount: Long,
    val expenseCount: Long,
    val incomeCount: Long,
    val categoryBreakdown: List<CategorySummary>
)

data class CategorySummary(
    val categoryId: String,
    val categoryName: String,
    val categoryColor: String,
    val totalAmount: BigDecimal,
    val transactionCount: Long,
    val type: String
) 