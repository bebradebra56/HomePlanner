package com.homeplane.plmeas.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val room: String = "",
    val category: String = "Other",
    val price: Double = 0.0,
    val store: String = "",
    val purchased: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
