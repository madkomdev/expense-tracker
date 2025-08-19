package com.expense.tracker.application.adapter.incoming.rest.model

data class Category(
    val name: String,
    val color: String = "#6B7280",
    val icon: String = "folder",
    val type: String, // "EXPENSE" or "INCOME"
    val isActive: Boolean = true,
    val sortOrder: Int = 0
) 