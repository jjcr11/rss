package com.jjcr11.rss.data.local.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

fun getRoomDatabase(
    builder: RoomDatabase.Builder<RssDatabase>,
): RssDatabase {
    return builder
        .setDriver(driver = BundledSQLiteDriver())
        .fallbackToDestructiveMigration(dropAllTables = true)
        .setQueryCoroutineContext(context = Dispatchers.IO)
        .build()
}
