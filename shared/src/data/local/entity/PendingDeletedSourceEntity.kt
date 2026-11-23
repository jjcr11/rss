package com.jjcr11.rss.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_deleted_sources")
data class PendingDeletedSourceEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
)
