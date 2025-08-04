package com.expense.tracker.application.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class UserManagementController {

    //User Management:
    //GET    /api/users/profile     - Get user profile
    //PUT    /api/users/profile     - Update user profile
    //DELETE /api/users/profile     - Delete user account

    @PostMapping("/api/users/register")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun registerUser(): ResponseEntity<String> {
        // Logic to create a user profile


        return ResponseEntity.ok("User profile created")
    }

    @PutMapping("/api/users/{id}/profile")
    @ResponseStatus(HttpStatus.OK)
    suspend fun updateUserProfile(@PathVariable id : String): ResponseEntity<String> {
        // Logic to update user profile

        return ResponseEntity.ok("User profile updated")
    }

    @DeleteMapping("/api/users/{id}/delete")
    @ResponseStatus(HttpStatus.OK)
    suspend fun deleteUserAccount(@PathVariable id: String): ResponseEntity<String> {
        // Logic to delete user account

        return ResponseEntity.ok("User account deleted")
    }

    @GetMapping("/api/users/{id}/profile")
    @ResponseStatus(HttpStatus.OK)
    suspend fun getUserProfile(@PathVariable id: String): ResponseEntity<String> {
        // Logic to get user profile

        return ResponseEntity.ok("User profile retrieved")
    }
}