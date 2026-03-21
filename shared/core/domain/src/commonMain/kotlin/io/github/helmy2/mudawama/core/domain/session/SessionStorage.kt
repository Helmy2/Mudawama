package io.github.helmy2.mudawama.core.domain.session

/**
 * Abstraction for persisting a user session defined in the domain module so it can be used across
 * layers without creating additional module dependencies.
 */
interface SessionStorage {
    suspend fun get(): AuthInfo?
    suspend fun set(info: AuthInfo?)
}

/** Simple encryptor abstraction — platform-specific implementations may live in platform modules. */
interface Encryptor {
    fun encrypt(plain: String): String
    fun decrypt(encrypted: String): String
}

