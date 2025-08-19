package com.expense.tracker.application.adapter.outgoing.db

import com.expense.tracker.domain.db.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: CoroutineCrudRepository<User, String> {

    suspend fun findByUsername(username: String): User?
}