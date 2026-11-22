package com.jjcr11.rss.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.jjcr11.rss.data.local.dao.FeedDao
import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.entity.FeedEntity
import com.jjcr11.rss.data.local.entity.SourceEntity

@Database(
    entities = [SourceEntity::class, FeedEntity::class],
    version = 2,
    exportSchema = false,
)
@ConstructedBy(RssDatabaseConstructor::class)
abstract class RssDatabase : RoomDatabase() {
    abstract fun sourceDao(): SourceDao
    abstract fun feedDao(): FeedDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "KotlinNoActualForExpect")
expect object RssDatabaseConstructor : RoomDatabaseConstructor<RssDatabase> {
    override fun initialize(): RssDatabase
}
