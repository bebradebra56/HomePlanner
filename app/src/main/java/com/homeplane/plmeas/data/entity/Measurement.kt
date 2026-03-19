package com.homeplane.plmeas.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val roomId: Long = -1L,
    val name: String,
    val value: Float = 0f,
    val unit: String = "m",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
