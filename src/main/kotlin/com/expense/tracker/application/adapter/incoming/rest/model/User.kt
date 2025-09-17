package com.expense.tracker.application.adapter.incoming.rest.model

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val address: String? = null,
    val username: String? = null,
    val password: String? = null
) {
    override fun toString(): String {
        return "User(firstName='$firstName', lastName='$lastName', email='$email', phone=$phone, address=$address, username=$username)"
    }
}