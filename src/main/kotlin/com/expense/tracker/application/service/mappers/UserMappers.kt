package com.expense.tracker.application.service.mappers

import com.expense.tracker.application.adapter.incoming.rest.model.User
import com.expense.tracker.application.adapter.incoming.rest.model.UserProfile
import com.expense.tracker.domain.db.User as DBUser
import com.expense.tracker.domain.db.UserRole

// User registration mapper - always assigns USER role for security
fun User.toDBUser(hashedPassword: String? = null, role: String = UserRole.USER) = DBUser(
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = this.username,
    passwordHash = hashedPassword,
    role = role
)

fun DBUser.toUser() = User(
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = this.username,
    password = null // Never return password in User model
)

fun DBUser.toUserProfile() = UserProfile(
    id = this.id?.toString() ?: "unknown",
    name = "${this.firstName} ${this.lastName}",
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = this.username,
    role = this.role
)

fun UserProfile.toUser() = User(
    firstName = this.name.split(" ").firstOrNull() ?: "",
    lastName = this.name.split(" ").drop(1).joinToString(" "),
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = this.username,
    password = null
)

fun UserProfile.toDBUser(existingUser: DBUser) = existingUser.copy(
    firstName = this.name.split(" ").firstOrNull() ?: existingUser.firstName,
    lastName = this.name.split(" ").drop(1).joinToString(" ").takeIf { it.isNotEmpty() } ?: existingUser.lastName,
    email = this.email,
    phone = this.phone,
    address = this.address
)
