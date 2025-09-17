package com.expense.tracker.application.service


import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class PasswordServiceTest {

    @Test
    fun `should hash password and verify it correctly`() {
        // Arrange
        val passwordEncoder = BCryptPasswordEncoder()
        val passwordService = PasswordService(passwordEncoder)
        val plainPassword = "password"

        // Act
        val hashedPassword = passwordService.hashPassword(plainPassword)
        println(hashedPassword)
        // Assert
        assertNotNull(hashedPassword)
        assertNotEquals(plainPassword, hashedPassword)

        assertTrue(passwordService.verifyPassword(plainPassword, hashedPassword))
        assertFalse(passwordService.verifyPassword("WrongPassword", hashedPassword))
    }
}
