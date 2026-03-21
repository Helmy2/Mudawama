package io.github.helmy2.mudawama.core.data.networking

import io.github.helmy2.mudawama.core.domain.session.SessionStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val sessionStorage: SessionStorage,
    private val baseUrl: String,
) {
    fun create(): HttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = false
                    },
                )
            }

            install(Logging) {
                level = LogLevel.ALL
                sanitizeHeader { header -> header == io.ktor.http.HttpHeaders.Authorization }
            }

            defaultRequest {
                url(baseUrl)
                contentType(ContentType.Application.Json)
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val authInfo = sessionStorage.get()
                        authInfo?.let {
                            BearerTokens(it.accessToken, it.refreshToken)
                        }
                    }
                    refreshTokens {
                        val authInfo = sessionStorage.get()
                        authInfo?.let {
                            BearerTokens(it.accessToken, it.refreshToken)
                        }
                    }
                }
            }
        }
}
