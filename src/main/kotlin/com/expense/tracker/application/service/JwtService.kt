package com.expense.tracker.application.service

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.*

@Service
class JwtService(
    private val jwtKeyManager: JwtKeyManager,
    @Value("\${app.jwt.expiration:3600}") private val jwtExpirationSeconds: Long = 3600,
    @Value("\${app.jwt.issuer:expense-tracker}") private val issuer: String = "expense-tracker"
) {
    
    private val logger = KotlinLogging.logger {}
    private val rsaPublicKey: RSAPublicKey by lazy { jwtKeyManager.rsaPublicKey }
    private val rsaPrivateKey: RSAPrivateKey by lazy { jwtKeyManager.rsaPrivateKey }
    private val signer: JWSSigner by lazy { RSASSASigner(rsaPrivateKey) }
    private val verifier: JWSVerifier by lazy { RSASSAVerifier(rsaPublicKey) }

    /**
     * Generate JWT token for authenticated user
     */
    fun generateToken(userId: String, username: String, email: String, role: String): String {
        val now = Instant.now()
        val expiration = now.plusSeconds(jwtExpirationSeconds)

        val claims = JWTClaimsSet.Builder()
            .issuer(issuer)
            .subject(userId)
            .audience("expense-tracker-api")
            .expirationTime(Date.from(expiration))
            .notBeforeTime(Date.from(now))
            .issueTime(Date.from(now))
            .jwtID(UUID.randomUUID().toString())
            .claim("user_id", userId)
            .claim("username", username)
            .claim("email", email)
            .claim("role", role)
            .claim("authorities", listOf("ROLE_$role"))
            .build()

        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(jwtKeyManager.getRSAKey().keyID)
                .build(),
            claims
        )

        try {
            signedJWT.sign(signer)
            return signedJWT.serialize()
        } catch (e: JOSEException) {
            logger.error("Failed to sign JWT token", e)
            throw RuntimeException("Failed to generate JWT token", e)
        }
    }

    /**
     * Validate and parse JWT token
     */
    fun validateToken(token: String): JWTClaimsSet? {
        return try {
            val signedJWT = SignedJWT.parse(token)
            
            if (!signedJWT.verify(verifier)) {
                logger.warn("JWT signature verification failed")
                return null
            }

            val claims = signedJWT.jwtClaimsSet
            val now = Date()

            // Check expiration
            if (claims.expirationTime?.before(now) == true) {
                logger.debug("JWT token has expired")
                return null
            }

            // Check not before
            if (claims.notBeforeTime?.after(now) == true) {
                logger.debug("JWT token not yet valid")
                return null
            }

            claims
        } catch (e: Exception) {
            logger.warn("Failed to parse or validate JWT token", e)
            null
        }
    }

    /**
     * Extract user ID from JWT token
     */
    fun getUserIdFromToken(token: String): String? {
        return validateToken(token)?.getStringClaim("user_id")
    }

    /**
     * Extract user role from JWT token
     */
    fun getUserRoleFromToken(token: String): String? {
        return validateToken(token)?.getStringClaim("role")
    }

    /**
     * Get RSA public key for JWKS endpoint
     */
    fun getRSAPublicKey(): RSAKey {
        return jwtKeyManager.getRSAPublicKey()
    }

    /**
     * Create login response with JWT token
     */
    data class LoginResponse(
        val token: String,
        val tokenType: String = "Bearer",
        val expiresIn: Long,
        val user: UserInfo
    )

    data class UserInfo(
        val id: String,
        val username: String,
        val email: String,
        val role: String
    )
}
