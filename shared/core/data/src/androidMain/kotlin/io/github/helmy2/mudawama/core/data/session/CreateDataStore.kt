package io.github.helmy2.mudawama.core.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

fun createDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath.toPath() }
    )
}