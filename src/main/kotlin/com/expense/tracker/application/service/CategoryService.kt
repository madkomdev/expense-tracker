package com.expense.tracker.application.service

import com.expense.tracker.application.adapter.incoming.rest.model.Category
import com.expense.tracker.application.adapter.incoming.rest.model.CategoryResponse
import com.expense.tracker.application.adapter.incoming.rest.model.CategoryUpdateRequest
import com.expense.tracker.application.adapter.outgoing.db.CategoryRepository
import com.expense.tracker.application.service.mappers.toDBCategory
import com.expense.tracker.application.service.mappers.toCategoryResponse
import com.expense.tracker.domain.model.CategoryType

import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    suspend fun createCategory(category: Category): CategoryResponse {
        if (categoryRepository.existsByName(category.name)) {
            throw IllegalArgumentException("Category with name '${category.name}' already exists")
        }
        val newCategory = category.toDBCategory()
        val savedCategory = categoryRepository.save(newCategory)
        return savedCategory.toCategoryResponse()
    }

    suspend fun getAllCategories(): List<CategoryResponse> {
        return categoryRepository.findAllByOrderBySortOrderAscNameAsc()
            .toList()
            .map { it.toCategoryResponse() }
    }

    suspend fun getActiveCategories(): List<CategoryResponse> {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc()
            .toList()
            .map { it.toCategoryResponse() }
    }

    suspend fun getCategoriesByType(type: String): List<CategoryResponse> {
        val transactionType = CategoryType.fromString(type).name
        return categoryRepository.findByTypeAndIsActiveTrueOrderBySortOrderAscNameAsc(transactionType)
            .toList()
            .map { it.toCategoryResponse() }
    }

    suspend fun getCategoryById(id: String): CategoryResponse? {
        return categoryRepository.findById(id)?.toCategoryResponse()
    }

    suspend fun updateCategory(id: String, updateRequest: CategoryUpdateRequest): CategoryResponse? {
        val existingCategory = categoryRepository.findById(id) ?: return null
        
        // Check for name conflicts if name is being updated
        if (updateRequest.name != null && updateRequest.name != existingCategory.name) {
            if (categoryRepository.existsByNameAndIdNot(updateRequest.name, id)) {
                throw IllegalArgumentException("Category with name '${updateRequest.name}' already exists")
            }
        }
        
        val updatedCategory = existingCategory.copy(
            name = updateRequest.name ?: existingCategory.name,
            color = updateRequest.color ?: existingCategory.color,
            icon = updateRequest.icon ?: existingCategory.icon,
            isActive = updateRequest.isActive ?: existingCategory.isActive,
            sortOrder = updateRequest.sortOrder ?: existingCategory.sortOrder
        )
        
        val savedCategory = categoryRepository.save(updatedCategory)
        return savedCategory.toCategoryResponse()
    }

    suspend fun deleteCategory(id: String): Boolean {
        return if (categoryRepository.existsById(id)) {
            // Note: Should check if category is used in transactions before deleting
            categoryRepository.deleteById(id)
            true
        } else {
            false
        }
    }
} 