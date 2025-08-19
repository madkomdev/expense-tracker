package com.expense.tracker.application.adapter.incoming.rest.model

data class CategoryResponse(
    val id: String,
    val name: String,
    val color: String,
    val icon: String,
    val type: String,
    val isActive: Boolean,
    val sortOrder: Int,
    val createdAt: String
) 