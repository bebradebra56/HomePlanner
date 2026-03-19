package com.homeplane.plmeas.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interior_rooms")
data class InteriorRoom(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val type: String = "Other",
    val style: String = "",
    val width: Float = 0f,
    val length: Float = 0f,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val area: Float get() = width * length
}
