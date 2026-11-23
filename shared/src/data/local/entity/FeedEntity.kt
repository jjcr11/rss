package com.jjcr11.rss.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "feeds",
    foreignKeys = [
        ForeignKey(
            entity = SourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["source_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["source_id"])],
)
data class FeedEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "source_id")
    val sourceId: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "content", defaultValue = "''")
    val content: String = "",
    @ColumnInfo(name = "is_read")
    val isRead: Boolean,
    @ColumnInfo(name = "is_saved")
    val isSaved: Boolean,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "is_pending_sync", defaultValue = "0")
    val isPendingSync: Boolean = false,
)
