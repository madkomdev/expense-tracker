package com.expense.tracker.application.service

import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.JWSAlgorithm
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import jakarta.annotation.PostConstruct

@Service
class JwtKeyManager(
    @Value("\${app.jwt.private-key:}") private val privateKeyBase64: String,
    @Value("\${app.jwt.public-key:}") private val publicKeyBase64: String,
    @Value("\${app.jwt.key-id:expense-tracker-key}") private val keyId: String,
    @Value("\${spring.profiles.active:local}") private val activeProfile: String
) {
    
    private val logger = KotlinLogging.logger {}
    
    lateinit var rsaPrivateKey: RSAPrivateKey
    lateinit var rsaPublicKey: RSAPublicKey
    
    @PostConstruct
    fun initializeKeys() {
        logger.info("Initializing JWT keys for profile: $activeProfile")
        
        try {
            if (privateKeyBase64.isNotEmpty() && publicKeyBase64.isNotEmpty()) {
                logger.info("Loading JWT keys from environment variables")
                rsaPrivateKey = loadPrivateKeyFromBase64(privateKeyBase64)
                rsaPublicKey = loadPublicKeyFromBase64(publicKeyBase64)
                logger.info("Successfully loaded JWT keys from environment")
            } else {
                if (activeProfile == "prod") {
                    throw IllegalStateException(
                        "JWT keys must be configured for production environment. " +
                        "Please set app.jwt.private-key and app.jwt.public-key"
                    )
                }
                logger.warn("No JWT keys configured, generating runtime keys for development")
                val keyPair = generateDevelopmentKeys()
                rsaPrivateKey = keyPair.private as RSAPrivateKey
                rsaPublicKey = keyPair.public as RSAPublicKey
                
                // Log the generated keys for development use
                logDevelopmentKeys(keyPair)
            }
        } catch (e: Exception) {
            logger.error("Failed to initialize JWT keys", e)
            throw IllegalStateException("JWT key initialization failed", e)
        }
    }
    
    private fun loadPrivateKeyFromBase64(base64Key: String): RSAPrivateKey {
        return try {
            val keyBytes = Base64.getDecoder().decode(base64Key)
            val spec = PKCS8EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePrivate(spec) as RSAPrivateKey
        } catch (e: Exception) {
            logger.error("Failed to parse private key from Base64", e)
            throw IllegalArgumentException("Invalid private key format", e)
        }
    }
    
    private fun loadPublicKeyFromBase64(base64Key: String): RSAPublicKey {
        return try {
            val keyBytes = Base64.getDecoder().decode(base64Key)
            val spec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePublic(spec) as RSAPublicKey
        } catch (e: Exception) {
            logger.error("Failed to parse public key from Base64", e)
            throw IllegalArgumentException("Invalid public key format", e)
        }
    }
    
    private fun generateDevelopmentKeys(): java.security.KeyPair {
        val keyGenerator = KeyPairGenerator.getInstance("RSA")
        keyGenerator.initialize(2048)
        return keyGenerator.generateKeyPair()
    }
    
    private fun logDevelopmentKeys(keyPair: java.security.KeyPair) {
        if (activeProfile != "prod") {
            try {
                val privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.private.encoded)
                val publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.public.encoded)
                
                logger.info("=== DEVELOPMENT JWT KEYS ===")
                logger.info("Private Key (Base64): $privateKeyBase64")
                logger.info("Public Key (Base64): $publicKeyBase64")
                logger.info("Key ID: $keyId")
                logger.info("============================")
            } catch (e: Exception) {
                logger.warn("Failed to log development keys", e)
            }
        }
    }
    
    /**
     * Get RSA key for JWKS endpoint (public key only)
     */
    fun getRSAPublicKey(): RSAKey {
        return RSAKey.Builder(rsaPublicKey)
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .keyID(keyId)
            .build()
    }
    
    /**
     * Get complete RSA key with private key for signing
     */
    fun getRSAKey(): RSAKey {
        return RSAKey.Builder(rsaPublicKey)
            .privateKey(rsaPrivateKey)
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .keyID(keyId)
            .build()
    }
}
