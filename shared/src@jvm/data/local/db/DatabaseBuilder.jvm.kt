package com.jjcr11.rss.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<RssDatabase> {
    val dbFile = File("${System.getProperty("user.home")}/.rss/rss_database.db")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<RssDatabase>(
        name = dbFile.absolutePath,
    )
}
