package io.github.helmy2.mudawama.core.domain.session

/**
 * Simple domain models for authentication session.
 */
data class User(
    val id: String,
    val email: String,
    val fullName: String,
)