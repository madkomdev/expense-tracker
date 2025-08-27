package com.expense.tracker.application.adapter.incoming.rest.model

import java.math.BigDecimal

data class Budget(
    val categoryId: String?, // Null for overall budget
    val name: String,
    val amount: BigDecimal,
    val period: String, // WEEKLY, MONTHLY, QUARTERLY, YEARLY
    val startDate: String, // YYYY-MM-DD format
    val endDate: String    // YYYY-MM-DD format
)
