package io.github.helmy2.mudawama.core.data.session

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.Aead
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import io.github.helmy2.mudawama.core.domain.session.Encryptor

class TinkEncryptor(
    context: Context,
) : Encryptor {
    private val aead: Aead

    init {
        AeadConfig.register()
        val keysetManager =
            AndroidKeysetManager
                .Builder()
                .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
                .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()
        aead = keysetManager.keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    override fun encrypt(plain: String): String {
        val encryptedData = aead.encrypt(plain.toByteArray(), ASSOCIATED_DATA)
        return Base64.encodeToString(encryptedData, Base64.NO_WRAP)
    }

    override fun decrypt(encrypted: String): String {
        val decoded = Base64.decode(encrypted, Base64.NO_WRAP)
        return String(aead.decrypt(decoded, ASSOCIATED_DATA))
    }

    private companion object {
        const val KEYSET_NAME = "mudawama_keyset"
        const val PREF_FILE_NAME = "mudawama_keyset_prefs"
        const val MASTER_KEY_URI = "android-keystore://mudawama_master_key"
        val ASSOCIATED_DATA = "mudawama_session".toByteArray()
    }
}
