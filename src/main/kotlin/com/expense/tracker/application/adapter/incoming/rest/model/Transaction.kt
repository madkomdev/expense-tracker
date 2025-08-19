package com.expense.tracker.application.adapter.incoming.rest.model

import java.math.BigDecimal

data class Transaction(
    val categoryId: String,
    val amount: BigDecimal,
    val description: String,
    val transactionDate: String, // Format: "YYYY-MM-DD"
    val type: String // "EXPENSE" or "INCOME"
) 