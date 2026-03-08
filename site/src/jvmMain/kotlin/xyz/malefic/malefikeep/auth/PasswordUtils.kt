package xyz.malefic.malefikeep.auth

import org.mindrot.jbcrypt.BCrypt

object PasswordUtils {
    private const val ROUNDS = 12

    fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(ROUNDS))

    fun verify(
        password: String,
        hash: String,
    ): Boolean = BCrypt.checkpw(password, hash)
}
