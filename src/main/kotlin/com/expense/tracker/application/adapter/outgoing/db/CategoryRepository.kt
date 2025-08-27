package com.expense.tracker.application.adapter.outgoing.db

import com.expense.tracker.domain.db.Category

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository: CoroutineCrudRepository<Category, String> {

    fun findAllByOrderBySortOrderAscNameAsc(): Flow<Category>

    @Query("""
        SELECT * FROM categories 
        WHERE type = :type
        ORDER BY sort_order ASC, name ASC
    """)
    fun findByTypeOrderBySortOrderAscNameAsc(type: String): Flow<Category>

    fun findByIsActiveTrueOrderBySortOrderAscNameAsc(): Flow<Category>

    @Query("""
        SELECT * FROM categories 
        WHERE type = :type 
        AND is_active = true
        ORDER BY sort_order ASC, name ASC
    """)
    fun findByTypeAndIsActiveTrueOrderBySortOrderAscNameAsc(type: String): Flow<Category>
    
    suspend fun existsByName(name: String): Boolean
    
    suspend fun existsByNameAndIdNot(name: String, id: String): Boolean
} 