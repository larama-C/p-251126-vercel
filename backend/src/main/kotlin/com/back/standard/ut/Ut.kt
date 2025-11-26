package com.back.standard.ut

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*

object Ut {


    object jwt {

        fun toString(secret: String, expireSeconds: Long, body: Map<String, Any>): String {
            val claimsBuilder = Jwts.claims()

            for ((key, value) in body) {
                claimsBuilder.add(key, value)
            }

            val claims = claimsBuilder.build()

            val issuedAt = Date()
            val expiration = Date(issuedAt.time + 1000L * expireSeconds)

            val secretKey: Key = Keys.hmacShaKeyFor(secret.toByteArray())

            val jwt = Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact()

            return jwt
        }

        fun isValid(jwt: String, secretPattern: String): Boolean {
            val secretKey = Keys.hmacShaKeyFor(secretPattern.toByteArray(StandardCharsets.UTF_8))

            return runCatching {
                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwt)
                true
            }.getOrElse { false }
        }

        fun payloadOrNull(jwt: String, secretPattern: String): Map<String, Any>? {
            val secretKey = Keys.hmacShaKeyFor(secretPattern.toByteArray(StandardCharsets.UTF_8))

            if (isValid(jwt, secretPattern)) {
                return Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwt)
                    .payload as Map<String, Any>
            }

            return null
        }
    }
}