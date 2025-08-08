package com.expense.tracker.application.domain.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("users")
data class User(
    @Id val id: UUID,
    @Column("first_name") val firstName: String,
    @Column("last_name") val lastName: String,
    @Column("email") val email: String,
    @Column("phone") val phone: String? = null,
    @Column("address") val address: String? = null,
    @Column("username") val username: String? = null,
    @Column("password_hash") val passwordHash: String? = null
)
