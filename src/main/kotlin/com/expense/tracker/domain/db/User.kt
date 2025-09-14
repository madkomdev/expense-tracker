package com.expense.tracker.domain.db

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("users")
data class User @PersistenceCreator constructor(
    @Id val id: UUID?,
    @Column("first_name") val firstName: String,
    @Column("last_name") val lastName: String,
    @Column("email") val email: String,
    @Column("phone") val phone: String? = null,
    @Column("address") val address: String? = null,
    @Column("username") val username: String? = null,
    @Column("password_hash") val passwordHash: String? = null,
    @Column("role") val role: String = "USER"
) {
    // Secondary constructor for creating new entities
    constructor(
        firstName: String,
        lastName: String,
        email: String,
        phone: String? = null,
        address: String? = null,
        username: String? = null,
        passwordHash: String? = null,
        role: String = "USER"
    ) : this(null, firstName, lastName, email, phone, address, username, passwordHash, role)
}

// User role constants
object UserRole {
    const val ADMIN = "ADMIN"
    const val USER = "USER"
    
    val ALL_ROLES = setOf(ADMIN, USER)
    
    fun isValid(role: String): Boolean = role in ALL_ROLES
}
