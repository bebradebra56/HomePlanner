package com.homeplane.plmeas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.homeplane.plmeas.data.dao.AppDao
import com.homeplane.plmeas.data.entity.*

@Database(
    entities = [
        Project::class,
        InteriorRoom::class,
        FurnitureItem::class,
        ShoppingItem::class,
        Measurement::class,
        Note::class,
        BudgetItem::class,
        Idea::class,
        PhotoItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "home_planner.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
