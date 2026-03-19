package com.homeplane.plmeas.data.dao

import androidx.room.*
import com.homeplane.plmeas.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ── Projects ──────────────────────────────────────────────────────────────
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    // ── Rooms ─────────────────────────────────────────────────────────────────
    @Query("SELECT * FROM interior_rooms WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getRoomsByProject(projectId: Long): Flow<List<InteriorRoom>>

    @Query("SELECT * FROM interior_rooms WHERE id = :id")
    suspend fun getRoomById(id: Long): InteriorRoom?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: InteriorRoom): Long

    @Update
    suspend fun updateRoom(room: InteriorRoom)

    @Delete
    suspend fun deleteRoom(room: InteriorRoom)

    @Query("SELECT COUNT(*) FROM interior_rooms WHERE projectId = :projectId")
    suspend fun getRoomCountForProject(projectId: Long): Int

    // ── Furniture ─────────────────────────────────────────────────────────────
    @Query("SELECT * FROM furniture_items WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getFurnitureByProject(projectId: Long): Flow<List<FurnitureItem>>

    @Query("SELECT * FROM furniture_items WHERE roomId = :roomId ORDER BY createdAt DESC")
    fun getFurnitureByRoom(roomId: Long): Flow<List<FurnitureItem>>

    @Query("SELECT * FROM furniture_items WHERE projectId = :projectId ORDER BY createdAt DESC LIMIT 5")
    fun getRecentFurniture(projectId: Long): Flow<List<FurnitureItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFurniture(item: FurnitureItem): Long

    @Update
    suspend fun updateFurniture(item: FurnitureItem)

    @Delete
    suspend fun deleteFurniture(item: FurnitureItem)

    @Query("UPDATE furniture_items SET posX = :posX, posY = :posY WHERE id = :id")
    suspend fun updateFurniturePosition(id: Long, posX: Float, posY: Float)

    @Query("SELECT COUNT(*) FROM furniture_items WHERE roomId = :roomId")
    suspend fun getFurnitureCountForRoom(roomId: Long): Int

    // ── Shopping ──────────────────────────────────────────────────────────────
    @Query("SELECT * FROM shopping_items WHERE projectId = :projectId ORDER BY purchased ASC, createdAt DESC")
    fun getShoppingByProject(projectId: Long): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem): Long

    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItem)

    @Query("UPDATE shopping_items SET purchased = :purchased WHERE id = :id")
    suspend fun toggleShoppingPurchased(id: Long, purchased: Boolean)

    // ── Measurements ──────────────────────────────────────────────────────────
    @Query("SELECT * FROM measurements WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getMeasurementsByProject(projectId: Long): Flow<List<Measurement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(item: Measurement): Long

    @Update
    suspend fun updateMeasurement(item: Measurement)

    @Delete
    suspend fun deleteMeasurement(item: Measurement)

    // ── Notes ─────────────────────────────────────────────────────────────────
    @Query("SELECT * FROM notes WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getNotesByProject(projectId: Long): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // ── Budget ────────────────────────────────────────────────────────────────
    @Query("SELECT * FROM budget_items WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getBudgetByProject(projectId: Long): Flow<List<BudgetItem>>

    @Query("SELECT SUM(amount) FROM budget_items WHERE projectId = :projectId")
    fun getTotalSpent(projectId: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetItem(item: BudgetItem): Long

    @Update
    suspend fun updateBudgetItem(item: BudgetItem)

    @Delete
    suspend fun deleteBudgetItem(item: BudgetItem)

    // ── Ideas ─────────────────────────────────────────────────────────────────
    @Query("SELECT * FROM ideas WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getIdeasByProject(projectId: Long): Flow<List<Idea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: Idea): Long

    @Update
    suspend fun updateIdea(idea: Idea)

    @Delete
    suspend fun deleteIdea(idea: Idea)

    // ── Backup / Export ───────────────────────────────────────────────────────
    @Query("SELECT * FROM interior_rooms ORDER BY projectId, createdAt")
    suspend fun getAllRoomsForBackup(): List<InteriorRoom>

    @Query("SELECT * FROM furniture_items ORDER BY projectId, createdAt")
    suspend fun getAllFurnitureForBackup(): List<FurnitureItem>

    @Query("SELECT * FROM shopping_items ORDER BY projectId, createdAt")
    suspend fun getAllShoppingForBackup(): List<ShoppingItem>

    @Query("SELECT * FROM budget_items ORDER BY projectId, createdAt")
    suspend fun getAllBudgetForBackup(): List<BudgetItem>

    @Query("SELECT * FROM measurements ORDER BY projectId, createdAt")
    suspend fun getAllMeasurementsForBackup(): List<Measurement>

    @Query("SELECT * FROM notes ORDER BY projectId, createdAt")
    suspend fun getAllNotesForBackup(): List<Note>

    @Query("SELECT * FROM ideas ORDER BY projectId, createdAt")
    suspend fun getAllIdeasForBackup(): List<Idea>

    // ── Photos ────────────────────────────────────────────────────────────────
    @Query("SELECT * FROM photo_items WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getPhotosByProject(projectId: Long): Flow<List<PhotoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoItem): Long

    @Delete
    suspend fun deletePhoto(photo: PhotoItem)
}
