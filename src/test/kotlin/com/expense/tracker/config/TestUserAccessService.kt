package com.expense.tracker.config

import com.expense.tracker.application.service.UserAccessService
import org.springframework.stereotype.Component
import org.springframework.context.annotation.Primary
import org.springframework.security.core.Authentication

@Component
@Primary
class TestUserAccessService : UserAccessService() {
    
    override fun canAccessUserData(targetUserId: String, authentication: Authentication?): Boolean {
        return true // Allow all access in tests
    }
    
    override fun hasAdminRole(authentication: Authentication?): Boolean {
        return true // Allow all admin operations in tests
    }
    
    override fun getCurrentUserId(authentication: Authentication?): String? {
        return "test-user-id"
    }
    
    override fun hasRole(role: String, authentication: Authentication?): Boolean {
        return true // Allow all roles in tests
    }
}
