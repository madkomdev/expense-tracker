package com.expense.tracker.application.service.mappers

import com.expense.tracker.application.adapter.incoming.rest.model.User
import com.expense.tracker.application.adapter.incoming.rest.model.UserProfile
import com.expense.tracker.application.adapter.incoming.rest.model.Category
import com.expense.tracker.application.adapter.incoming.rest.model.CategoryResponse
import com.expense.tracker.application.adapter.incoming.rest.model.Transaction
import com.expense.tracker.application.adapter.incoming.rest.model.TransactionResponse
import com.expense.tracker.domain.db.User as DBUser
import com.expense.tracker.domain.db.Category as DBCategory
import com.expense.tracker.domain.db.Transaction as DBTransaction
import com.expense.tracker.domain.db.TransactionType
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
    passwordHash = hashedPassword
)

fun DBUser.toUser() = User(
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = this.username,
    password = null // Never return password in User model
)

fun DBUser.toUserProfile() = UserProfile(
    id = this.id?.toString() ?: "unknown",
    name = "${this.firstName} ${this.lastName}",
    email = this.email,
    phone = this.phone,
    address = this.address
)

fun UserProfile.toUser() = User(
    firstName = this.name.split(" ").firstOrNull() ?: "",
    lastName = this.name.split(" ").drop(1).joinToString(" "),
    email = this.email,
    phone = this.phone,
    address = this.address,
    username = null,
    password = null
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
    type = TransactionType.valueOf(this.type.uppercase()),
    isActive = this.isActive,
    sortOrder = this.sortOrder
)

fun DBCategory.toCategoryResponse() = CategoryResponse(
    id = this.id?.toString() ?: "unknown",
    name = this.name,
    color = this.color,
    icon = this.icon,
    type = this.type.name,
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
    transactionDate = LocalDate.parse(this.transactionDate),
    type = TransactionType.valueOf(this.type.uppercase())
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
    type = this.type.name,
    createdAt = this.createdAt?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) ?: ""
)
