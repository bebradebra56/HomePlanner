package com.homeplane.plmeas.data

import com.homeplane.plmeas.data.dao.AppDao
import com.homeplane.plmeas.data.entity.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {

    // Projects
    fun getAllProjects(): Flow<List<Project>> = dao.getAllProjects()
    suspend fun getProjectById(id: Long) = dao.getProjectById(id)
    suspend fun insertProject(project: Project) = dao.insertProject(project)
    suspend fun updateProject(project: Project) = dao.updateProject(project)
    suspend fun deleteProject(project: Project) = dao.deleteProject(project)

    // Rooms
    fun getRoomsByProject(projectId: Long) = dao.getRoomsByProject(projectId)
    suspend fun getRoomById(id: Long) = dao.getRoomById(id)
    suspend fun insertRoom(room: InteriorRoom) = dao.insertRoom(room)
    suspend fun updateRoom(room: InteriorRoom) = dao.updateRoom(room)
    suspend fun deleteRoom(room: InteriorRoom) = dao.deleteRoom(room)
    suspend fun getRoomCountForProject(projectId: Long) = dao.getRoomCountForProject(projectId)

    // Furniture
    fun getFurnitureByProject(projectId: Long) = dao.getFurnitureByProject(projectId)
    fun getFurnitureByRoom(roomId: Long) = dao.getFurnitureByRoom(roomId)
    fun getRecentFurniture(projectId: Long) = dao.getRecentFurniture(projectId)
    suspend fun insertFurniture(item: FurnitureItem) = dao.insertFurniture(item)
    suspend fun updateFurniture(item: FurnitureItem) = dao.updateFurniture(item)
    suspend fun deleteFurniture(item: FurnitureItem) = dao.deleteFurniture(item)
    suspend fun updateFurniturePosition(id: Long, posX: Float, posY: Float) =
        dao.updateFurniturePosition(id, posX, posY)
    suspend fun getFurnitureCountForRoom(roomId: Long) = dao.getFurnitureCountForRoom(roomId)

    // Shopping
    fun getShoppingByProject(projectId: Long) = dao.getShoppingByProject(projectId)
    suspend fun insertShoppingItem(item: ShoppingItem) = dao.insertShoppingItem(item)
    suspend fun updateShoppingItem(item: ShoppingItem) = dao.updateShoppingItem(item)
    suspend fun deleteShoppingItem(item: ShoppingItem) = dao.deleteShoppingItem(item)
    suspend fun toggleShoppingPurchased(id: Long, purchased: Boolean) =
        dao.toggleShoppingPurchased(id, purchased)

    // Measurements
    fun getMeasurementsByProject(projectId: Long) = dao.getMeasurementsByProject(projectId)
    suspend fun insertMeasurement(item: Measurement) = dao.insertMeasurement(item)
    suspend fun updateMeasurement(item: Measurement) = dao.updateMeasurement(item)
    suspend fun deleteMeasurement(item: Measurement) = dao.deleteMeasurement(item)

    // Notes
    fun getNotesByProject(projectId: Long) = dao.getNotesByProject(projectId)
    suspend fun insertNote(note: Note) = dao.insertNote(note)
    suspend fun updateNote(note: Note) = dao.updateNote(note)
    suspend fun deleteNote(note: Note) = dao.deleteNote(note)

    // Budget
    fun getBudgetByProject(projectId: Long) = dao.getBudgetByProject(projectId)
    fun getTotalSpent(projectId: Long): Flow<Double?> = dao.getTotalSpent(projectId)
    suspend fun insertBudgetItem(item: BudgetItem) = dao.insertBudgetItem(item)
    suspend fun updateBudgetItem(item: BudgetItem) = dao.updateBudgetItem(item)
    suspend fun deleteBudgetItem(item: BudgetItem) = dao.deleteBudgetItem(item)

    // Ideas
    fun getIdeasByProject(projectId: Long) = dao.getIdeasByProject(projectId)
    suspend fun insertIdea(idea: Idea) = dao.insertIdea(idea)
    suspend fun updateIdea(idea: Idea) = dao.updateIdea(idea)
    suspend fun deleteIdea(idea: Idea) = dao.deleteIdea(idea)

    // Backup
    suspend fun getAllRoomsForBackup() = dao.getAllRoomsForBackup()
    suspend fun getAllFurnitureForBackup() = dao.getAllFurnitureForBackup()
    suspend fun getAllShoppingForBackup() = dao.getAllShoppingForBackup()
    suspend fun getAllBudgetForBackup() = dao.getAllBudgetForBackup()
    suspend fun getAllMeasurementsForBackup() = dao.getAllMeasurementsForBackup()
    suspend fun getAllNotesForBackup() = dao.getAllNotesForBackup()
    suspend fun getAllIdeasForBackup() = dao.getAllIdeasForBackup()

    // Photos
    fun getPhotosByProject(projectId: Long) = dao.getPhotosByProject(projectId)
    suspend fun insertPhoto(photo: PhotoItem) = dao.insertPhoto(photo)
    suspend fun deletePhoto(photo: PhotoItem) = dao.deletePhoto(photo)
}
