package io.github.helmy2.mudawama.core.domain.session

data class AuthInfo(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
)

