package com.expense.tracker.application.adapter.incoming.rest

import com.expense.tracker.application.adapter.incoming.rest.mappers.toUserProfile
import com.expense.tracker.application.adapter.incoming.rest.model.User
import com.expense.tracker.application.adapter.incoming.rest.model.UserProfile
import com.expense.tracker.application.service.UserManagementService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class UserManagementController( private val userManagementService: UserManagementService) {

    //User Management:
    //GET    /api/users/profile     - Get user profile
    //PUT    /api/users/profile     - Update user profile
    //DELETE /api/users/profile     - Delete user account

    @PostMapping("/api/users/register")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun registerUser(@RequestBody user: User): ResponseEntity<UserProfile> {

        val user = userManagementService.createUser(user)

        return ResponseEntity.ok(user.toUserProfile())
    }

    @PutMapping("/api/users/{id}/profile")
    @ResponseStatus(HttpStatus.OK)
    suspend fun updateUserProfile(@PathVariable id : String, @RequestBody user: User): ResponseEntity<UserProfile> {

        return ResponseEntity.ok(UserProfile(
            id = id,
            name = "${user.firstName} ${user.lastName}",
            email = user.email,
            phone = user.phone,
            address = user.address
        ))
    }

    @DeleteMapping("/api/users/{id}/delete")
    @ResponseStatus(HttpStatus.OK)
    suspend fun deleteUserAccount(@PathVariable id: String): ResponseEntity<String> {
        // Logic to delete user account

        return ResponseEntity.ok("User account deleted")
    }

    @GetMapping("/api/users/{id}/profile")
    @ResponseStatus(HttpStatus.OK)
    suspend fun getUserProfile(@PathVariable id: String): ResponseEntity<UserProfile> {
        // Logic to get user profile

        return ResponseEntity.ok(UserProfile(
            id = id,
            name = "", // This should be replaced with actual user data retrieval logic
            email = "",
            phone = "",
            address = ""
        ))
    }
}