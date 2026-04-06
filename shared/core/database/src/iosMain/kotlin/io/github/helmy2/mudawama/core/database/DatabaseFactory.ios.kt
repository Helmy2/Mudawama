package io.github.helmy2.mudawama.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder(): RoomDatabase.Builder<MudawamaDatabase> {
    val dbFilePath = NSHomeDirectory() + "/mudawama.db"
    return Room.databaseBuilder<MudawamaDatabase>(
        name = dbFilePath
    ).setDriver(BundledSQLiteDriver())
}

