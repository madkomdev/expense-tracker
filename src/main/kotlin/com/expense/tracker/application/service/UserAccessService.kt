package com.expense.tracker.application.service

import com.expense.tracker.domain.db.UserRole
import mu.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class UserAccessService {
    
    private val logger = KotlinLogging.logger {}

    /**
     * Check if the authenticated user can access data for the given user ID
     */
    fun canAccessUserData(targetUserId: String, authentication: Authentication?): Boolean {
        if (authentication == null || !authentication.isAuthenticated) {
            logger.debug("Authentication is null or not authenticated")
            return false
        }

        return try {
            val jwtToken = authentication as JwtAuthenticationToken
            val currentUserId = jwtToken.token.getClaimAsString("user_id")
            val userRole = jwtToken.token.getClaimAsString("role")

            // Admin can access any user's data
            if (userRole == UserRole.ADMIN) {
                logger.debug("Admin user accessing data for user: $targetUserId")
                return true
            }

            // User can only access their own data
            val canAccess = currentUserId == targetUserId
            if (!canAccess) {
                logger.warn("User $currentUserId attempted to access data for user $targetUserId")
            }
            
            canAccess
        } catch (e: Exception) {
            logger.error("Error checking user access permissions", e)
            false
        }
    }

    /**
     * Check if the authenticated user is the owner of the resource or is an admin
     */
    fun isOwnerOrAdmin(resourceUserId: String, authentication: Authentication?): Boolean {
        return canAccessUserData(resourceUserId, authentication)
    }

    /**
     * Check if the authenticated user has admin role
     */
    fun hasAdminRole(authentication: Authentication?): Boolean {
        if (authentication == null || !authentication.isAuthenticated) {
            return false
        }

        return try {
            val jwtToken = authentication as JwtAuthenticationToken
            val userRole = jwtToken.token.getClaimAsString("role")
            userRole == UserRole.ADMIN
        } catch (e: Exception) {
            logger.error("Error checking admin role", e)
            false
        }
    }

    /**
     * Check if the authenticated user has the specified role
     */
    fun hasRole(role: String, authentication: Authentication?): Boolean {
        if (authentication == null || !authentication.isAuthenticated) {
            return false
        }

        return try {
            val jwtToken = authentication as JwtAuthenticationToken
            val userRole = jwtToken.token.getClaimAsString("role")
            userRole == role
        } catch (e: Exception) {
            logger.error("Error checking user role", e)
            false
        }
    }

    /**
     * Get the current user ID from authentication
     */
    fun getCurrentUserId(authentication: Authentication?): String? {
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }

        return try {
            val jwtToken = authentication as JwtAuthenticationToken
            jwtToken.token.getClaimAsString("user_id")
        } catch (e: Exception) {
            logger.error("Error getting current user ID", e)
            null
        }
    }

    /**
     * Get the current user role from authentication
     */
    fun getCurrentUserRole(authentication: Authentication?): String? {
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }

        return try {
            val jwtToken = authentication as JwtAuthenticationToken
            jwtToken.token.getClaimAsString("role")
        } catch (e: Exception) {
            logger.error("Error getting current user role", e)
            null
        }
    }
}
