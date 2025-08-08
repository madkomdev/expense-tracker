package com.expense.tracker.application.service.mappers

import com.expense.tracker.application.adapter.incoming.rest.model.User
import java.util.UUID


fun User.toDBUser() = com.expense.tracker.application.domain.db.User(
    id = UUID.randomUUID(),
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    phone = this.phone,
    address = this.address
)

fun com.expense.tracker.application.domain.db.User.toUser() = User(
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    phone = this.phone,
    address = this.address
)
