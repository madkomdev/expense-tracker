package com.expense.tracker.application.adapter.outgoing.db

import com.expense.tracker.domain.db.Category
import com.expense.tracker.domain.db.TransactionType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository: CoroutineCrudRepository<Category, String> {
    
    fun findAllByOrderBySortOrderAscNameAsc(): Flow<Category>
    
    fun findByTypeOrderBySortOrderAscNameAsc(type: TransactionType): Flow<Category>
    
    fun findByIsActiveTrueOrderBySortOrderAscNameAsc(): Flow<Category>
    
    fun findByTypeAndIsActiveTrueOrderBySortOrderAscNameAsc(type: TransactionType): Flow<Category>
    
    suspend fun existsByName(name: String): Boolean
    
    suspend fun existsByNameAndIdNot(name: String, id: String): Boolean
} 