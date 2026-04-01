package io.github.helmy2.mudawama.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<MudawamaDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("mudawama.db")
    return Room.databaseBuilder<MudawamaDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

