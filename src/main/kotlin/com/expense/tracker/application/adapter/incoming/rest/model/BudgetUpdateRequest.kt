package com.expense.tracker.application.adapter.incoming.rest.model

import java.math.BigDecimal

data class BudgetUpdateRequest(
    val name: String? = null,
    val amount: BigDecimal? = null,
    val startDate: String? = null,
    val endDate: String? = null
)
