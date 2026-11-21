package com.jjcr11.rss.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<RssDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("rss_database.db")
    return Room.databaseBuilder<RssDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
