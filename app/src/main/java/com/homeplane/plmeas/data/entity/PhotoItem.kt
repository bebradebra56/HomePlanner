package com.homeplane.plmeas.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_items")
data class PhotoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val roomId: Long = -1L,
    val uri: String,
    val description: String = "",
    val category: String = "Design Ideas",
    val createdAt: Long = System.currentTimeMillis()
)
