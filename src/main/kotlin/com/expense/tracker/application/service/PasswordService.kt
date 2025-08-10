package com.expense.tracker.application.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class PasswordService(
    private val passwordEncoder: PasswordEncoder
) {

    fun hashPassword(plainPassword: String): String {
        return passwordEncoder.encode(plainPassword)
    }

    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return passwordEncoder.matches(plainPassword, hashedPassword)
    }
} 