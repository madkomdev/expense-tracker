package com.expense.tracker.application.adapter.incoming.rest.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)