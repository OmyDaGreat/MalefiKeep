package xyz.malefic.malefikeep.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date

object JwtUtils {
    private val secret = System.getenv("JWT_SECRET") ?: "dev-secret-please-change-in-production"
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)
    private const val ISSUER = "malefikeep"
    private const val EXPIRY_MS = 24 * 60 * 60 * 1000L // 24 hours

    fun generateToken(userId: String, username: String): String =
        JWT
            .create()
            .withIssuer(ISSUER)
            .withSubject(userId)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRY_MS))
            .sign(algorithm)

    fun verifyToken(token: String): DecodedJWT? =
        runCatching {
            JWT.require(algorithm).withIssuer(ISSUER).build().verify(token)
        }.getOrNull()

    fun extractUserId(token: String): String? = verifyToken(token)?.subject

    fun extractUsername(token: String): String? = verifyToken(token)?.getClaim("username")?.asString()
}
