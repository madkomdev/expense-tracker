package com.expense.tracker.application.adapter.incoming.rest.mappers

import com.expense.tracker.application.adapter.incoming.rest.model.User
import com.expense.tracker.application.adapter.incoming.rest.model.UserProfile
import com.expense.tracker.domain.db.User as DBUser

fun DBUser.toUserProfile() = UserProfile(
    id = this.id?.toString() ?: "unknown",
    name = "${this.firstName} ${this.lastName}",
    email = this.email,
    phone = this.phone,
    address = this.address,
    role = this.role
)

fun User.toUserProfile(id: String) = UserProfile(
    id = id,
    name = "${this.firstName} ${this.lastName}",
    email = this.email,
    phone = this.phone,
    address = this.address,
    role = "USER"
)

fun UserProfile.toUser() = User(
    firstName = this.name.split(" ").firstOrNull() ?: "",
    lastName = this.name.split(" ").drop(1).joinToString(" "),
    email = this.email,
    phone = this.phone,
    address = this.address
)