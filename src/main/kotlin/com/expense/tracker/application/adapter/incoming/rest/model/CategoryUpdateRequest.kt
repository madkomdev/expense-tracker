package com.expense.tracker.application.adapter.incoming.rest.model

data class CategoryUpdateRequest(
    val name: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val isActive: Boolean? = null,
    val sortOrder: Int? = null
) 