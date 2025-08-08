package com.expense.tracker.application.service

import com.expense.tracker.application.adapter.incoming.rest.model.User
import com.expense.tracker.application.adapter.outgoing.db.UserRepository
import com.expense.tracker.application.service.mappers.toDBUser
import com.expense.tracker.application.service.mappers.toUser
import com.expense.tracker.application.domain.db.User as DBUser
import org.springframework.stereotype.Service

@Service
class UserManagementService(
    private val userRepository: UserRepository,
) {

    suspend fun createUser(user: User): DBUser {
        val newUser = user.toDBUser()
        userRepository.save(newUser)
        return newUser
    }

    suspend fun getUserById(id: String): User? {
        return userRepository.findById(id)?.toUser()
    }
}