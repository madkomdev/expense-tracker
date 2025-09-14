package com.expense.tracker.application.adapter.incoming.rest

import com.expense.tracker.application.service.JwtService
import com.nimbusds.jose.jwk.JWKSet
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class JwksController(
    private val jwtService: JwtService
) {

    @GetMapping("/.well-known/jwks.json", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getJwks(): ResponseEntity<Map<String, Any>> {
        val rsaKey = jwtService.getRSAPublicKey()
        val jwkSet = JWKSet(rsaKey)
        
        return ResponseEntity.ok(jwkSet.toJSONObject(true))
    }
}
