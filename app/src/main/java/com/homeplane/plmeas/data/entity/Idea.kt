package com.homeplane.plmeas.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ideas")
data class Idea(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val description: String = "",
    val tags: String = "",
    val colorHex: String = "#E76F51",
    val createdAt: Long = System.currentTimeMillis()
)
