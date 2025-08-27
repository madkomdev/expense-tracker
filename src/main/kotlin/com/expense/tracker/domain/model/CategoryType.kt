package com.expense.tracker.domain.model

enum class CategoryType {
    EXPENSE,
    INCOME;

    companion object {
        fun fromString(type: String): CategoryType {
            return entries.find { it.name.equals(type, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid category type: $type")
        }

        fun isValid(type: String): Boolean {
            return entries.any { it.name.equals(type, ignoreCase = true) }
        }

    }
}