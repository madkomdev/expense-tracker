package com.expense.tracker.application.service

import com.expense.tracker.application.adapter.incoming.rest.model.User
import com.expense.tracker.application.adapter.incoming.rest.model.UserProfile
import com.expense.tracker.application.adapter.outgoing.db.UserRepository
import com.expense.tracker.application.service.mappers.toDBUser
import com.expense.tracker.application.service.mappers.toUserProfile
import com.expense.tracker.domain.db.UserRole
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class UserManagementService(
    private val userRepository: UserRepository,
    private val passwordService: PasswordService
) {

    suspend fun createUser(user: User): UserProfile {
        // Check for existing username or email
        if (user.username != null && userRepository.existsByUsername(user.username)) {
            throw IllegalArgumentException("Username '${user.username}' already exists")
        }
        if (userRepository.existsByEmail(user.email)) {
            throw IllegalArgumentException("Email '${user.email}' already exists")
        }
        
        val hashedPassword = user.password?.let { passwordService.hashPassword(it) }
        val newUser = user.toDBUser(hashedPassword, UserRole.USER)
        val savedUser = userRepository.save(newUser)
        return savedUser.toUserProfile()
    }

    suspend fun getUserById(id: String): UserProfile? {
        return userRepository.findById(id)?.toUserProfile()
    }

    suspend fun updateUser(id: String, user: User): UserProfile? {
        val existingUser = userRepository.findById(id) ?: return null
        val hashedPassword = user.password?.let { passwordService.hashPassword(it) }
            ?: existingUser.passwordHash // Keep existing password if not provided
        
        val updatedUser = existingUser.copy(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            phone = user.phone,
            address = user.address,
            username = user.username,
            passwordHash = hashedPassword
        )
        val savedUser = userRepository.save(updatedUser)
        return savedUser.toUserProfile()
    }

    suspend fun deleteUser(id: String): Boolean {
        return if (userRepository.existsById(id)) {
            userRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    suspend fun getAllUsers(): List<UserProfile> {
        return userRepository.findAll().toList().map { it.toUserProfile() }
    }

    suspend fun authenticateUser(username: String, password: String): UserProfile? {
        val user = userRepository.findByUsername(username) 
            ?: userRepository.findByEmail(username) // Allow login with email
        
        return if (user?.passwordHash != null && passwordService.verifyPassword(password, user.passwordHash)) {
            user.toUserProfile()
        } else {
            null
        }
    }

    suspend fun changePassword(userId: String, currentPassword: String, newPassword: String): Boolean {
        val user = userRepository.findById(userId) ?: return false
        
        if (user.passwordHash == null || !passwordService.verifyPassword(currentPassword, user.passwordHash)) {
            return false
        }
        
        val newHashedPassword = passwordService.hashPassword(newPassword)
        val updatedUser = user.copy(passwordHash = newHashedPassword)
        userRepository.save(updatedUser)
        
        return true
    }

    // Admin-only role management methods
    suspend fun promoteUserToAdmin(adminUserId: String, targetUserId: String): UserProfile? {
        // Verify admin requesting the promotion
        val admin = userRepository.findById(adminUserId) 
        if (admin?.role != UserRole.ADMIN) {
            throw SecurityException("Only admins can promote users")
        }
        
        val targetUser = userRepository.findById(targetUserId) ?: return null
        val promotedUser = targetUser.copy(role = UserRole.ADMIN)
        val savedUser = userRepository.save(promotedUser)
        return savedUser.toUserProfile()
    }

    suspend fun demoteUserFromAdmin(adminUserId: String, targetUserId: String): UserProfile? {
        // Verify admin requesting the demotion
        val admin = userRepository.findById(adminUserId)
        if (admin?.role != UserRole.ADMIN) {
            throw SecurityException("Only admins can demote users")
        }
        
        // Prevent admin from demoting themselves
        if (adminUserId == targetUserId) {
            throw IllegalArgumentException("Admin cannot demote themselves")
        }
        
        val targetUser = userRepository.findById(targetUserId) ?: return null
        val demotedUser = targetUser.copy(role = UserRole.USER)
        val savedUser = userRepository.save(demotedUser)
        return savedUser.toUserProfile()
    }

}