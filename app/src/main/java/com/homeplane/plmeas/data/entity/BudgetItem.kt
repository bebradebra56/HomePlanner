package com.homeplane.plmeas.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_items")
data class BudgetItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val category: String = "Other",
    val amount: Double = 0.0,
    val date: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
