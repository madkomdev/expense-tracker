package com.expense.tracker.application.adapter.incoming.rest.model

import java.math.BigDecimal

data class TransactionSearchRequest(
    val categoryId: String? = null,
    val type: String? = null, // Still kept for backwards compatibility - will filter by category type
    val description: String? = null,
    val minAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val page: Int = 0,
    val size: Int = 20
) 