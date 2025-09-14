package com.expense.tracker.application.adapter.incoming.rest

import com.expense.tracker.application.adapter.incoming.rest.model.ChangePasswordRequest
import com.expense.tracker.application.adapter.incoming.rest.model.LoginRequest
import com.expense.tracker.application.adapter.incoming.rest.model.User
import com.expense.tracker.application.adapter.incoming.rest.model.UserProfile
import com.expense.tracker.application.service.UserManagementService
import com.expense.tracker.application.service.JwtService
import com.expense.tracker.application.service.UserAccessService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class UserManagementController(
    private val userManagementService: UserManagementService,
    private val jwtService: JwtService,
    private val userAccessService: UserAccessService
) {

    //User Management:
    //GET    /api/users                    - Get all users
    //GET    /api/users/{id}/profile       - Get user profile
    //POST   /api/users/register           - Register new user
    //POST   /api/users/login              - Authenticate user
    //PUT    /api/users/{id}/profile       - Update user profile
    //PUT    /api/users/{id}/change-password - Change user password
    //DELETE /api/users/{id}/delete        - Delete user account

    @PostMapping("/api/users/register")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun registerUser(@RequestBody user: User): ResponseEntity<UserProfile> {
        val userProfile = userManagementService.createUser(user)
        return ResponseEntity.ok(userProfile)
    }

    @PostMapping("/api/users/login")
    @ResponseStatus(HttpStatus.OK)
    suspend fun loginUser(@RequestBody loginRequest: LoginRequest): ResponseEntity<JwtService.LoginResponse> {
        val userProfile = userManagementService.authenticateUser(loginRequest.username, loginRequest.password)
        return if (userProfile != null) {
            val token = jwtService.generateToken(
                userId = userProfile.id,
                username = userProfile.username ?: userProfile.email,
                email = userProfile.email,
                role = userProfile.role
            )
            
            val loginResponse = JwtService.LoginResponse(
                token = token,
                expiresIn = 3600, // 1 hour
                user = JwtService.UserInfo(
                    id = userProfile.id,
                    username = userProfile.username ?: userProfile.email,
                    email = userProfile.email,
                    role = userProfile.role
                )
            )
            
            ResponseEntity.ok(loginResponse)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @GetMapping("/api/users")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@userAccessService.hasAdminRole(authentication)")
    suspend fun getAllUsers(): ResponseEntity<List<UserProfile>> {
        val userProfiles = userManagementService.getAllUsers()
        return ResponseEntity.ok(userProfiles)
    }

    @GetMapping("/api/users/{id}/profile")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@userAccessService.canAccessUserData(#id, authentication)")
    suspend fun getUserProfile(@PathVariable id: String): ResponseEntity<UserProfile> {
        val userProfile = userManagementService.getUserById(id)
        return if (userProfile != null) {
            ResponseEntity.ok(userProfile)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/api/users/{id}/profile")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@userAccessService.canAccessUserData(#id, authentication)")
    suspend fun updateUserProfile(@PathVariable id : String, @RequestBody user: User): ResponseEntity<UserProfile> {
        val userProfile = userManagementService.updateUser(id, user)
        return if (userProfile != null) {
            ResponseEntity.ok(userProfile)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/api/users/{id}/change-password")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@userAccessService.canAccessUserData(#id, authentication)")
    suspend fun changePassword(@PathVariable id: String, @RequestBody changePasswordRequest: ChangePasswordRequest): ResponseEntity<String> {
        val success = userManagementService.changePassword(id, changePasswordRequest.currentPassword, changePasswordRequest.newPassword)
        return if (success) {
            ResponseEntity.ok("Password changed successfully")
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to change password. Please check your current password.")
        }
    }

    @DeleteMapping("/api/users/{id}/delete")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@userAccessService.canAccessUserData(#id, authentication)")
    suspend fun deleteUserAccount(@PathVariable id: String): ResponseEntity<String> {
        val deleted = userManagementService.deleteUser(id)
        return if (deleted) {
            ResponseEntity.ok("User account deleted successfully")
        } else {
            ResponseEntity.notFound().build()
        }
    }
}