package com.expense.tracker.application.adapter.incoming.rest.mappers

import com.expense.tracker.application.adapter.incoming.rest.model.UserProfile
import com.expense.tracker.application.domain.db.User

fun User.toUserProfile() = UserProfile(
    id = this.id.toString(),
    name = "${this.firstName} ${this.lastName}",
    email = this.email,
    phone = this.phone,
    address = this.address
)