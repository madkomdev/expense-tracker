package com.expense.tracker.application.adapter.incoming.rest.model

import java.math.BigDecimal

data class TransactionUpdateRequest(
    val categoryId: String? = null,
    val amount: BigDecimal? = null,
    val description: String? = null,
    val transactionDate: String? = null,
    val type: String? = null
) 