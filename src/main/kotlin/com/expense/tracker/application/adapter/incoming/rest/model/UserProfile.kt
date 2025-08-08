package com.expense.tracker.application.adapter.incoming.rest.model

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val address: String? = null
)