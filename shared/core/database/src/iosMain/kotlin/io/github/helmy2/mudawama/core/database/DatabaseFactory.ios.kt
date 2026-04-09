package io.github.helmy2.mudawama.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

fun getDatabaseBuilder(): RoomDatabase.Builder<MudawamaDatabase> {
    val docDir = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).firstOrNull() as? String
        ?: throw IllegalStateException("Cannot resolve NSDocumentDirectory on iOS")
    val dbFilePath = "$docDir/mudawama.db"
    return Room.databaseBuilder<MudawamaDatabase>(
        name = dbFilePath
    ).setDriver(BundledSQLiteDriver())
}

