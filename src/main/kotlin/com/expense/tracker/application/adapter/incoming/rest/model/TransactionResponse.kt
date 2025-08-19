package com.expense.tracker.application.adapter.incoming.rest.model

import java.math.BigDecimal

data class TransactionResponse(
    val id: String,
    val userId: String,
    val categoryId: String,
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String,
    val amount: BigDecimal,
    val description: String,
    val transactionDate: String,
    val type: String,
    val createdAt: String
) 