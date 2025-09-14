package com.expense.tracker.application.service.mappers

import com.expense.tracker.application.adapter.incoming.rest.model.User
import com.expense.tracker.application.adapter.incoming.rest.model.UserProfile
import com.expense.tracker.application.adapter.incoming.rest.model.Category
import com.expense.tracker.application.adapter.incoming.rest.model.CategoryResponse
import com.expense.tracker.application.adapter.incoming.rest.model.Transaction
import com.expense.tracker.application.adapter.incoming.rest.model.TransactionResponse
import com.expense.tracker.application.adapter.incoming.rest.model.Budget
import com.expense.tracker.application.adapter.incoming.rest.model.BudgetResponse
import com.expense.tracker.domain.model.CategoryType
import com.expense.tracker.domain.db.User as DBUser
import com.expense.tracker.domain.db.Category as DBCategory
import com.expense.tracker.domain.db.Transaction as DBTransaction
import com.expense.tracker.domain.db.Budget as DBBudget

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID


fun User.toDBUser(hashedPassword: String? = null) = DBUser(
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = this.username,
    passwordHash = hashedPassword,
    role = this.role
)

fun DBUser.toUser() = User(
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = this.username,
    password = null, // Never return password in User model
    role = this.role
)

fun DBUser.toUserProfile() = UserProfile(
    id = this.id?.toString() ?: "unknown",
    name = "${this.firstName} ${this.lastName}",
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = this.username,
    role = this.role
)

fun UserProfile.toUser() = User(
    firstName = this.name.split(" ").firstOrNull() ?: "",
    lastName = this.name.split(" ").drop(1).joinToString(" "),
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = this.username,
    password = null,
    role = this.role
)

fun UserProfile.toDBUser(existingUser: DBUser) = existingUser.copy(
    firstName = this.name.split(" ").firstOrNull() ?: existingUser.firstName,
    lastName = this.name.split(" ").drop(1).joinToString(" ").takeIf { it.isNotEmpty() } ?: existingUser.lastName,
    email = this.email,
    phone = this.phone,
    address = this.address
)

// Category Mappers
fun Category.toDBCategory() = DBCategory(
    name = this.name,
    color = this.color,
    icon = this.icon,
    type = CategoryType.fromString(this.type).name,
    isActive = this.isActive,
    sortOrder = this.sortOrder
)

fun DBCategory.toCategoryResponse() = CategoryResponse(
    id = this.id?.toString() ?: "unknown",
    name = this.name,
    color = this.color,
    icon = this.icon,
    type = CategoryType.fromString(this.type).name,
    isActive = this.isActive,
    sortOrder = this.sortOrder,
    createdAt = this.createdAt?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) ?: ""
)

// Transaction Mappers  
fun Transaction.toDBTransaction(userId: UUID) = DBTransaction(
    userId = userId,
    categoryId = UUID.fromString(this.categoryId),
    amount = this.amount,
    description = this.description,
    transactionDate = LocalDate.parse(this.transactionDate)
)

fun DBTransaction.toTransactionResponse(category: DBCategory) = TransactionResponse(
    id = this.id?.toString() ?: "unknown",
    userId = this.userId.toString(),
    categoryId = this.categoryId.toString(),
    categoryName = category.name,
    categoryColor = category.color,
    categoryIcon = category.icon,
    amount = this.amount,
    description = this.description,
    transactionDate = this.transactionDate.toString(),
    type = category.type, // Type is now inferred from the category
    createdAt = this.createdAt?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) ?: ""
)

// Budget Mappers
fun Budget.toDBBudget(userId: UUID) = DBBudget(
    userId = userId,
    categoryId = this.categoryId?.let { UUID.fromString(it) },
    name = this.name,
    amount = this.amount,
    period = this.period,
    startDate = LocalDate.parse(this.startDate),
    endDate = LocalDate.parse(this.endDate)
)

fun DBBudget.toBudgetResponse(
    category: DBCategory?,
    spentAmount: BigDecimal
) = BudgetResponse(
    id = this.id?.toString() ?: "unknown",
    userId = this.userId.toString(),
    categoryId = this.categoryId?.toString(),
    categoryName = category?.name,
    categoryColor = category?.color,
    categoryIcon = category?.icon,
    name = this.name,
    amount = this.amount,
    spentAmount = spentAmount,
    remainingAmount = this.amount.subtract(spentAmount),
    period = this.period,
    startDate = this.startDate.toString(),
    endDate = this.endDate.toString(),
    createdAt = this.createdAt?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) ?: ""
)
