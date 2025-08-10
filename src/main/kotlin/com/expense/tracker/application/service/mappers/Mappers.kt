package com.expense.tracker.application.service.mappers

import com.expense.tracker.application.adapter.incoming.rest.model.User
import com.expense.tracker.application.adapter.incoming.rest.model.UserProfile
import java.util.UUID


fun User.toDBUser(hashedPassword: String? = null) = com.expense.tracker.application.domain.db.User(
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = this.username,
    passwordHash = hashedPassword
)

fun com.expense.tracker.application.domain.db.User.toUser() = User(
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = this.username,
    password = null // Never return password in User model
)

fun com.expense.tracker.application.domain.db.User.toUserProfile() = UserProfile(
    id = this.id?.toString() ?: "unknown",
    name = "${this.firstName} ${this.lastName}",
    email = this.email,
    phone = this.phone,
    address = this.address
)

fun UserProfile.toUser() = User(
    firstName = this.name.split(" ").firstOrNull() ?: "",
    lastName = this.name.split(" ").drop(1).joinToString(" "),
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = null,
    password = null
)

fun UserProfile.toDBUser(existingUser: com.expense.tracker.application.domain.db.User) = existingUser.copy(
    firstName = this.name.split(" ").firstOrNull() ?: existingUser.firstName,
    lastName = this.name.split(" ").drop(1).joinToString(" ").takeIf { it.isNotEmpty() } ?: existingUser.lastName,
    email = this.email,
    phone = this.phone,
    address = this.address
)
