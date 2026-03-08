package xyz.malefic.malefikeep.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.security.SecureRandom
import java.util.Date

object JwtUtils {
    private val secret = System.getenv("JWT_SECRET") ?: "dev-secret-please-change-in-production"
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)
    private const val ISSUER = "malefikeep"
    private const val ACCESS_TOKEN_EXPIRY_MS = 3 * 24 * 60 * 60 * 1000L // 3 days
    val REFRESH_TOKEN_EXPIRY_MS = 30 * 24 * 60 * 60 * 1000L // 30 days

    fun generateToken(userId: String, username: String): String =
        JWT
            .create()
            .withIssuer(ISSUER)
            .withSubject(userId)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY_MS))
            .sign(algorithm)

    fun generateRefreshToken(): String {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyToken(token: String): DecodedJWT? =
        runCatching {
            JWT.require(algorithm).withIssuer(ISSUER).build().verify(token)
        }.getOrNull()

    fun extractUserId(token: String): String? = verifyToken(token)?.subject

    fun extractUsername(token: String): String? = verifyToken(token)?.getClaim("username")?.asString()
}
