package com.homeplane.plmeas.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "furniture_items")
data class FurnitureItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val roomId: Long = -1L,
    val name: String,
    val category: String = "Other",
    val width: Float = 0f,
    val height: Float = 0f,
    val depth: Float = 0f,
    val price: Double = 0.0,
    val store: String = "",
    val notes: String = "",
    val posX: Float = 20f,
    val posY: Float = 20f,
    val createdAt: Long = System.currentTimeMillis()
)
