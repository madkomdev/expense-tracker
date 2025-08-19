package com.expense.tracker.application.adapter.incoming.rest

import com.expense.tracker.application.adapter.incoming.rest.model.Category
import com.expense.tracker.application.adapter.incoming.rest.model.CategoryResponse
import com.expense.tracker.application.adapter.incoming.rest.model.CategoryUpdateRequest
import com.expense.tracker.application.service.CategoryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    suspend fun getAllCategories(): ResponseEntity<List<CategoryResponse>> {
        val categories = categoryService.getAllCategories()
        return ResponseEntity.ok(categories)
    }

    @GetMapping("/active")
    suspend fun getActiveCategories(): ResponseEntity<List<CategoryResponse>> {
        val categories = categoryService.getActiveCategories()
        return ResponseEntity.ok(categories)
    }

    @GetMapping("/by-type/{type}")
    suspend fun getCategoriesByType(@PathVariable type: String): ResponseEntity<List<CategoryResponse>> {
        return try {
            val categories = categoryService.getCategoriesByType(type)
            ResponseEntity.ok(categories)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/{id}")
    suspend fun getCategoryById(@PathVariable id: String): ResponseEntity<CategoryResponse> {
        val category = categoryService.getCategoryById(id)
        return if (category != null) {
            ResponseEntity.ok(category)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    suspend fun createCategory(@RequestBody category: Category): ResponseEntity<CategoryResponse> {
        return try {
            val createdCategory = categoryService.createCategory(category)
            ResponseEntity.status(HttpStatus.CREATED).body(createdCategory)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{id}")
    suspend fun updateCategory(
        @PathVariable id: String,
        @RequestBody updateRequest: CategoryUpdateRequest
    ): ResponseEntity<CategoryResponse> {
        return try {
            val updatedCategory = categoryService.updateCategory(id, updateRequest)
            if (updatedCategory != null) {
                ResponseEntity.ok(updatedCategory)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/{id}")
    suspend fun deleteCategory(@PathVariable id: String): ResponseEntity<String> {
        val deleted = categoryService.deleteCategory(id)
        return if (deleted) {
            ResponseEntity.ok("Category deleted successfully")
        } else {
            ResponseEntity.notFound().build()
        }
    }
} 