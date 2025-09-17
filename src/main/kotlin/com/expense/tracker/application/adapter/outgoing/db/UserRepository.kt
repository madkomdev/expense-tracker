package com.expense.tracker.application.adapter.outgoing.db

import com.expense.tracker.domain.db.User
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: CoroutineCrudRepository<User, String> {

    suspend fun findByUsername(username: String): User?
    
    suspend fun findByEmail(email: String): User?

    suspend fun existsByUsername(username: String): Boolean
    
    suspend fun existsByEmail(email: String): Boolean
}