package com.homeplane.plmeas.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val apartmentType: String = "",
    val area: Float = 0f,
    val startDate: String = "",
    val budget: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
