package io.github.helmy2.mudawama.core.data.session


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.helmy2.mudawama.core.domain.session.AuthInfo
import io.github.helmy2.mudawama.core.domain.session.Encryptor
import io.github.helmy2.mudawama.core.domain.session.SessionStorage
import io.github.helmy2.mudawama.core.domain.session.User
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


const val DATA_STORE_FILE_NAME = "session.preferences_pb"

class DataStoreSessionStorage(
    private val dataStore: DataStore<Preferences>,
    private val encryptor: Encryptor,
) : SessionStorage {

    private val key = stringPreferencesKey("auth_info")

    override suspend fun get(): AuthInfo? {
        val prefs = dataStore.data.firstOrNull() ?: return null
        val encrypted = prefs[key] ?: return null

        return try {
            val json = encryptor.decrypt(encrypted)
            Json.decodeFromString<AuthInfoDto>(json).toAuthInfo()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun set(info: AuthInfo?) {
        dataStore.edit { prefs ->
            if (info == null) {
                prefs.remove(key)
            } else {
                val dto = AuthInfoDto.from(info)
                val json = Json.encodeToString(dto)
                prefs[key] = encryptor.encrypt(json)
            }
        }
    }
}

@Serializable
private data class AuthInfoDto(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String,
    val fullName: String,
) {
    fun toAuthInfo() = AuthInfo(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = User(id = userId, email = email, fullName = fullName),
    )

    companion object {
        fun from(info: AuthInfo) = AuthInfoDto(
            accessToken = info.accessToken,
            refreshToken = info.refreshToken,
            userId = info.user.id,
            email = info.user.email,
            fullName = info.user.fullName,
        )
    }
}