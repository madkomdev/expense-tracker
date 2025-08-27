package com.expense.tracker.application.service

import com.expense.tracker.application.adapter.incoming.rest.model.*
import com.expense.tracker.application.adapter.outgoing.db.BudgetRepository
import com.expense.tracker.application.adapter.outgoing.db.CategoryRepository
import com.expense.tracker.application.adapter.outgoing.db.TransactionRepository
import com.expense.tracker.application.service.mappers.toDBBudget
import com.expense.tracker.application.service.mappers.toBudgetResponse
import com.expense.tracker.domain.db.Budget as DBBudget
import com.expense.tracker.domain.db.BudgetPeriod
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
class BudgetService(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) {

    suspend fun createBudget(userId: UUID, budget: Budget): BudgetResponse {
        // Validate period
        if (!BudgetPeriod.isValid(budget.period)) {
            throw IllegalArgumentException("Invalid budget period: ${budget.period}")
        }
        
        // Validate dates
        val startDate = LocalDate.parse(budget.startDate)
        val endDate = LocalDate.parse(budget.endDate)
        if (endDate <= startDate) {
            throw IllegalArgumentException("End date must be after start date")
        }
        
        // Validate category exists if provided
        val category = if (budget.categoryId != null) {
            categoryRepository.findById(budget.categoryId)
                ?: throw IllegalArgumentException("Category not found")
        } else null
        
        // Check for overlapping budgets
        val categoryUUID = budget.categoryId?.let { UUID.fromString(it) }
        val budgetExists = budgetRepository.existsByUserIdAndCategoryIdAndPeriodAndStartDate(
            userId, categoryUUID, budget.period, startDate
        )
        if (budgetExists) {
            throw IllegalArgumentException("Budget already exists for this category and period")
        }
        
        val newBudget = budget.toDBBudget(userId)
        val savedBudget = budgetRepository.save(newBudget)
        
        // Calculate spent amount
        val spentAmount = calculateSpentAmount(savedBudget)
        
        return savedBudget.toBudgetResponse(category, spentAmount)
    }

    suspend fun getUserBudgets(userId: UUID): List<BudgetResponse> {
        return budgetRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .toList()
            .map { budget ->
                val category = if (budget.categoryId != null) {
                    categoryRepository.findById(budget.categoryId.toString())
                } else null
                val spentAmount = calculateSpentAmount(budget)
                budget.toBudgetResponse(category, spentAmount)
            }
    }

    suspend fun getBudgetById(userId: UUID, budgetId: String): BudgetResponse? {
        val budget = budgetRepository.findById(budgetId) ?: return null
        
        // Ensure user owns this budget
        if (budget.userId != userId) {
            throw SecurityException("User does not own this budget")
        }
        
        val category = if (budget.categoryId != null) {
            categoryRepository.findById(budget.categoryId.toString())
        } else null
        
        val spentAmount = calculateSpentAmount(budget)
        return budget.toBudgetResponse(category, spentAmount)
    }

    suspend fun updateBudget(userId: UUID, budgetId: String, updateRequest: BudgetUpdateRequest): BudgetResponse? {
        val existingBudget = budgetRepository.findById(budgetId) ?: return null
        
        // Ensure user owns this budget
        if (existingBudget.userId != userId) {
            throw SecurityException("User does not own this budget")
        }
        
        // Validate dates if being updated
        val startDate = updateRequest.startDate?.let { LocalDate.parse(it) } ?: existingBudget.startDate
        val endDate = updateRequest.endDate?.let { LocalDate.parse(it) } ?: existingBudget.endDate
        
        if (endDate <= startDate) {
            throw IllegalArgumentException("End date must be after start date")
        }
        
        val updatedBudget = existingBudget.copy(
            name = updateRequest.name ?: existingBudget.name,
            amount = updateRequest.amount ?: existingBudget.amount,
            startDate = startDate,
            endDate = endDate
        )
        
        val savedBudget = budgetRepository.save(updatedBudget)
        
        val category = if (savedBudget.categoryId != null) {
            categoryRepository.findById(savedBudget.categoryId.toString())
        } else null
        
        val spentAmount = calculateSpentAmount(savedBudget)
        return savedBudget.toBudgetResponse(category, spentAmount)
    }

    suspend fun deleteBudget(userId: UUID, budgetId: String): Boolean {
        return if (budgetRepository.existsByIdAndUserId(budgetId, userId)) {
            budgetRepository.deleteById(budgetId)
            true
        } else {
            false
        }
    }

    suspend fun getBudgetSummary(userId: UUID): BudgetSummary {
        val budgets = budgetRepository.findByUserIdOrderByCreatedAtDesc(userId).toList()
        
        var totalAllocated = BigDecimal.ZERO
        var totalSpent = BigDecimal.ZERO
        
        val categoryMap = mutableMapOf<String?, CategoryBudgetSummary>()
        
        for (budget in budgets) {
            val spentAmount = calculateSpentAmount(budget)
            totalAllocated = totalAllocated.add(budget.amount)
            totalSpent = totalSpent.add(spentAmount)
            
            val categoryId = budget.categoryId?.toString()
            val categoryName = if (budget.categoryId != null) {
                categoryRepository.findById(budget.categoryId.toString())?.name
            } else "Overall Budget"
            
            val existingSummary = categoryMap[categoryId]
            if (existingSummary != null) {
                categoryMap[categoryId] = existingSummary.copy(
                    totalAllocated = existingSummary.totalAllocated.add(budget.amount),
                    totalSpent = existingSummary.totalSpent.add(spentAmount),
                    totalRemaining = existingSummary.totalRemaining.add(budget.amount).subtract(spentAmount),
                    budgetCount = existingSummary.budgetCount + 1
                )
            } else {
                categoryMap[categoryId] = CategoryBudgetSummary(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    totalAllocated = budget.amount,
                    totalSpent = spentAmount,
                    totalRemaining = budget.amount.subtract(spentAmount),
                    budgetCount = 1
                )
            }
        }
        
        return BudgetSummary(
            totalBudgets = budgets.size,
            totalAllocated = totalAllocated,
            totalSpent = totalSpent,
            totalRemaining = totalAllocated.subtract(totalSpent),
            budgetsByCategory = categoryMap.values.toList()
        )
    }

    suspend fun getBudgetsByCategory(userId: UUID, categoryId: String): List<BudgetResponse> {
        val categoryUUID = UUID.fromString(categoryId)
        val category = categoryRepository.findById(categoryId)
            ?: throw IllegalArgumentException("Category not found")
        
        return budgetRepository.findByUserIdAndCategoryIdOrderByCreatedAtDesc(userId, categoryUUID)
            .toList()
            .map { budget ->
                val spentAmount = calculateSpentAmount(budget)
                budget.toBudgetResponse(category, spentAmount)
            }
    }
    
    // Helper method to calculate spent amount for a budget
    private suspend fun calculateSpentAmount(budget: DBBudget): BigDecimal {
        return transactionRepository.calculateSpentAmountForBudget(
            userId = budget.userId,
            categoryId = budget.categoryId,
            startDate = budget.startDate,
            endDate = budget.endDate
        )
    }
}
