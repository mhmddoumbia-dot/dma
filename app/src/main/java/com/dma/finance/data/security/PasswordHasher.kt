package com.dma.finance.data.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

/**
 * Hache les mots de passe avec PBKDF2-HMAC-SHA256 (100 000 itérations) et un sel
 * aléatoire par utilisateur. Aucun mot de passe n'est jamais conservé en clair.
 */
class PasswordHasher @Inject constructor() {

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }

    fun hash(password: String, saltHex: String): String {
        val salt = saltHex.hexToBytes()
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hashBytes = factory.generateSecret(spec).encoded
        return hashBytes.joinToString(separator = "") { "%02x".format(it) }
    }

    fun verify(password: String, saltHex: String, expectedHashHex: String): Boolean {
        val actualHash = hash(password, saltHex)
        return constantTimeEquals(actualHash, expectedHashHex)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

    private fun String.hexToBytes(): ByteArray =
        ByteArray(length / 2) { i -> ((this[i * 2].digitToInt(16) shl 4) + this[i * 2 + 1].digitToInt(16)).toByte() }

    private companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ITERATIONS = 100_000
        const val KEY_LENGTH_BITS = 256
    }
}
