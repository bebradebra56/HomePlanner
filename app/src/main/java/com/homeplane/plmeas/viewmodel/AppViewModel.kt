package com.homeplane.plmeas.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.homeplane.plmeas.bfg.presentation.app.HomePlannerApplication
import com.homeplane.plmeas.data.AppPreferences
import com.homeplane.plmeas.data.PreferencesManager
import com.homeplane.plmeas.data.entity.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as HomePlannerApplication).repository
    private val prefsManager = PreferencesManager(application)

    val preferences: StateFlow<AppPreferences> = prefsManager.appPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())

    val activeProjectId: StateFlow<Long> = preferences
        .map { it.activeProjectId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1L)

    val allProjects: StateFlow<List<Project>> = repository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProject: StateFlow<Project?> = activeProjectId
        .flatMapLatest { id ->
            allProjects.map { projects -> projects.firstOrNull { it.id == id } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rooms: StateFlow<List<InteriorRoom>> = activeProjectId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(emptyList())
            else repository.getRoomsByProject(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val furniture: StateFlow<List<FurnitureItem>> = activeProjectId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(emptyList())
            else repository.getFurnitureByProject(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentFurniture: StateFlow<List<FurnitureItem>> = activeProjectId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(emptyList())
            else repository.getRecentFurniture(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingItems: StateFlow<List<ShoppingItem>> = activeProjectId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(emptyList())
            else repository.getShoppingByProject(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val measurements: StateFlow<List<Measurement>> = activeProjectId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(emptyList())
            else repository.getMeasurementsByProject(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<Note>> = activeProjectId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(emptyList())
            else repository.getNotesByProject(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetItems: StateFlow<List<BudgetItem>> = activeProjectId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(emptyList())
            else repository.getBudgetByProject(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpent: StateFlow<Double> = activeProjectId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(0.0)
            else repository.getTotalSpent(id).map { it ?: 0.0 }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val ideas: StateFlow<List<Idea>> = activeProjectId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(emptyList())
            else repository.getIdeasByProject(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photos: StateFlow<List<PhotoItem>> = activeProjectId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(emptyList())
            else repository.getPhotosByProject(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getFurnitureByRoom(roomId: Long): Flow<List<FurnitureItem>> =
        repository.getFurnitureByRoom(roomId)

    // ── Preferences ───────────────────────────────────────────────────────────
    fun completeOnboarding() = viewModelScope.launch {
        prefsManager.setOnboardingCompleted(true)
    }

    fun setActiveProject(projectId: Long) = viewModelScope.launch {
        prefsManager.setActiveProjectId(projectId)
    }

    fun setCurrency(currency: String) = viewModelScope.launch {
        prefsManager.setCurrency(currency)
    }

    fun setUnits(units: String) = viewModelScope.launch {
        prefsManager.setUnits(units)
    }

    fun setDarkTheme(dark: Boolean) = viewModelScope.launch {
        prefsManager.setDarkTheme(dark)
    }

    fun setNotifications(enabled: Boolean) = viewModelScope.launch {
        prefsManager.setNotificationsEnabled(enabled)
    }

    // ── Projects ──────────────────────────────────────────────────────────────
    fun addProject(name: String, type: String, area: Float, startDate: String, budget: Double, notes: String) =
        viewModelScope.launch {
            val id = repository.insertProject(
                Project(name = name, apartmentType = type, area = area, startDate = startDate, budget = budget, notes = notes)
            )
            if (activeProjectId.value == -1L) prefsManager.setActiveProjectId(id)
        }

    fun updateProject(project: Project) = viewModelScope.launch { repository.updateProject(project) }

    fun deleteProject(project: Project) = viewModelScope.launch {
        repository.deleteProject(project)
        if (activeProjectId.value == project.id) prefsManager.setActiveProjectId(-1L)
    }

    // ── Rooms ─────────────────────────────────────────────────────────────────
    fun addRoom(name: String, type: String, style: String, width: Float, length: Float, notes: String) =
        viewModelScope.launch {
            val projectId = activeProjectId.value
            if (projectId != -1L) {
                repository.insertRoom(
                    InteriorRoom(projectId = projectId, name = name, type = type, style = style, width = width, length = length, notes = notes)
                )
            }
        }

    fun updateRoom(room: InteriorRoom) = viewModelScope.launch { repository.updateRoom(room) }

    fun deleteRoom(room: InteriorRoom) = viewModelScope.launch { repository.deleteRoom(room) }

    suspend fun getRoomById(id: Long) = repository.getRoomById(id)

    // ── Furniture ─────────────────────────────────────────────────────────────
    fun addFurniture(
        name: String, category: String, roomId: Long,
        width: Float, height: Float, depth: Float,
        price: Double, store: String, notes: String
    ) = viewModelScope.launch {
        val projectId = activeProjectId.value
        if (projectId != -1L) {
            repository.insertFurniture(
                FurnitureItem(
                    projectId = projectId, roomId = roomId, name = name, category = category,
                    width = width, height = height, depth = depth,
                    price = price, store = store, notes = notes
                )
            )
        }
    }

    fun updateFurniture(item: FurnitureItem) = viewModelScope.launch { repository.updateFurniture(item) }

    fun deleteFurniture(item: FurnitureItem) = viewModelScope.launch { repository.deleteFurniture(item) }

    fun updateFurniturePosition(id: Long, posX: Float, posY: Float) =
        viewModelScope.launch { repository.updateFurniturePosition(id, posX, posY) }

    // ── Shopping ──────────────────────────────────────────────────────────────
    fun addShoppingItem(name: String, room: String, category: String, price: Double, store: String) =
        viewModelScope.launch {
            val projectId = activeProjectId.value
            if (projectId != -1L) {
                repository.insertShoppingItem(
                    ShoppingItem(projectId = projectId, name = name, room = room, category = category, price = price, store = store)
                )
            }
        }

    fun toggleShoppingItem(id: Long, purchased: Boolean) =
        viewModelScope.launch { repository.toggleShoppingPurchased(id, !purchased) }

    fun deleteShoppingItem(item: ShoppingItem) = viewModelScope.launch { repository.deleteShoppingItem(item) }

    // ── Measurements ──────────────────────────────────────────────────────────
    fun addMeasurement(name: String, value: Float, unit: String, roomId: Long, notes: String) =
        viewModelScope.launch {
            val projectId = activeProjectId.value
            if (projectId != -1L) {
                repository.insertMeasurement(
                    Measurement(projectId = projectId, roomId = roomId, name = name, value = value, unit = unit, notes = notes)
                )
            }
        }

    fun deleteMeasurement(item: Measurement) = viewModelScope.launch { repository.deleteMeasurement(item) }

    fun updateMeasurement(item: Measurement) = viewModelScope.launch { repository.updateMeasurement(item) }

    // ── Notes ─────────────────────────────────────────────────────────────────
    fun addNote(title: String, content: String) = viewModelScope.launch {
        val projectId = activeProjectId.value
        if (projectId != -1L) {
            repository.insertNote(Note(projectId = projectId, title = title, content = content))
        }
    }

    fun updateNote(note: Note) = viewModelScope.launch { repository.updateNote(note) }

    fun deleteNote(note: Note) = viewModelScope.launch { repository.deleteNote(note) }

    // ── Budget ────────────────────────────────────────────────────────────────
    fun addBudgetItem(name: String, category: String, amount: Double, date: String, notes: String) =
        viewModelScope.launch {
            val projectId = activeProjectId.value
            if (projectId != -1L) {
                repository.insertBudgetItem(
                    BudgetItem(projectId = projectId, name = name, category = category, amount = amount, date = date, notes = notes)
                )
            }
        }

    fun deleteBudgetItem(item: BudgetItem) = viewModelScope.launch { repository.deleteBudgetItem(item) }

    // ── Ideas ─────────────────────────────────────────────────────────────────
    fun addIdea(title: String, description: String, tags: String, colorHex: String) =
        viewModelScope.launch {
            val projectId = activeProjectId.value
            if (projectId != -1L) {
                repository.insertIdea(
                    Idea(projectId = projectId, title = title, description = description, tags = tags, colorHex = colorHex)
                )
            }
        }

    fun deleteIdea(idea: Idea) = viewModelScope.launch { repository.deleteIdea(idea) }

    // ── Photos ────────────────────────────────────────────────────────────────
    fun addPhoto(uri: String, description: String, category: String) = viewModelScope.launch {
        val projectId = activeProjectId.value
        if (projectId != -1L) {
            repository.insertPhoto(
                PhotoItem(projectId = projectId, uri = uri, description = description, category = category)
            )
        }
    }

    fun deletePhoto(photo: PhotoItem) = viewModelScope.launch { repository.deletePhoto(photo) }

    // ── Export / Backup ───────────────────────────────────────────────────────
    fun buildExportCsv(currency: String, units: String, onReady: (String) -> Unit) =
        viewModelScope.launch {
            val project = activeProject.value
            val roomList = rooms.value
            val furnitureList = furniture.value
            val shoppingList = shoppingItems.value
            val budgetList = budgetItems.value
            val measurementList = measurements.value
            val noteList = notes.value
            val sym = when (currency.uppercase()) {
                "EUR" -> "€"; "GBP" -> "£"; "RUB" -> "₽"
                "JPY" -> "¥"; "CNY" -> "¥"; else -> "$"
            }
            val csv = buildString {
                appendLine("HomePlanner Export")
                appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(Date())}")
                appendLine("Project: ${project?.name ?: "Unknown"}")
                appendLine()
                appendLine("=== ROOMS ===")
                appendLine("Name,Type,Style,Width($units),Length($units),Area(${units}²),Notes")
                roomList.forEach { r ->
                    appendLine("\"${r.name}\",\"${r.type}\",\"${r.style}\",${r.width},${r.length},${r.area},\"${r.notes}\"")
                }
                appendLine()
                appendLine("=== FURNITURE ===")
                appendLine("Name,Category,Room,Width(cm),Height(cm),Depth(cm),Price($sym),Store,Notes")
                furnitureList.forEach { f ->
                    appendLine("\"${f.name}\",\"${f.category}\",${f.roomId},${f.width},${f.height},${f.depth},${f.price},\"${f.store}\",\"${f.notes}\"")
                }
                appendLine()
                appendLine("=== SHOPPING LIST ===")
                appendLine("Name,Category,Room,Price($sym),Store,Purchased")
                shoppingList.forEach { s ->
                    appendLine("\"${s.name}\",\"${s.category}\",\"${s.room}\",${s.price},\"${s.store}\",${s.purchased}")
                }
                appendLine()
                appendLine("=== BUDGET ===")
                appendLine("Name,Category,Amount($sym),Date,Notes")
                budgetList.forEach { b ->
                    appendLine("\"${b.name}\",\"${b.category}\",${b.amount},\"${b.date}\",\"${b.notes}\"")
                }
                appendLine()
                appendLine("=== MEASUREMENTS ===")
                appendLine("Name,RoomId,Value($units),Notes")
                measurementList.forEach { m ->
                    appendLine("\"${m.name}\",${m.roomId},${m.value},\"${m.notes}\"")
                }
                appendLine()
                appendLine("=== NOTES ===")
                appendLine("Title,Content")
                noteList.forEach { n ->
                    appendLine("\"${n.title}\",\"${n.content}\"")
                }
            }
            onReady(csv)
        }

    fun buildFullBackup(onReady: (String) -> Unit) = viewModelScope.launch {
        val projects = allProjects.value
        val allRooms = repository.getAllRoomsForBackup()
        val allFurniture = repository.getAllFurnitureForBackup()
        val allShopping = repository.getAllShoppingForBackup()
        val allBudget = repository.getAllBudgetForBackup()
        val allMeasurements = repository.getAllMeasurementsForBackup()
        val allNotes = repository.getAllNotesForBackup()
        val allIdeas = repository.getAllIdeasForBackup()
        val backup = buildString {
            appendLine("HomePlanner Full Backup")
            appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(Date())}")
            appendLine("Total projects: ${projects.size}")
            appendLine()
            projects.forEach { p ->
                appendLine("### PROJECT: ${p.name} (id=${p.id}) ###")
                appendLine("Type: ${p.apartmentType}, Area: ${p.area}, Budget: ${p.budget}")
                appendLine("Start: ${p.startDate}, Notes: ${p.notes}")
                appendLine()
                val rooms = allRooms.filter { it.projectId == p.id }
                if (rooms.isNotEmpty()) {
                    appendLine("-- Rooms (${rooms.size}) --")
                    rooms.forEach { appendLine("  ${it.name} | ${it.type} | ${it.style} | ${it.width}x${it.length} | ${it.notes}") }
                    appendLine()
                }
                val furniture = allFurniture.filter { it.projectId == p.id }
                if (furniture.isNotEmpty()) {
                    appendLine("-- Furniture (${furniture.size}) --")
                    furniture.forEach { appendLine("  ${it.name} | ${it.category} | ${it.width}x${it.height}x${it.depth} cm | ${it.price} | ${it.store}") }
                    appendLine()
                }
                val shopping = allShopping.filter { it.projectId == p.id }
                if (shopping.isNotEmpty()) {
                    appendLine("-- Shopping (${shopping.size}) --")
                    shopping.forEach { appendLine("  ${it.name} | ${it.category} | ${it.price} | ${it.store} | purchased=${it.purchased}") }
                    appendLine()
                }
                val budget = allBudget.filter { it.projectId == p.id }
                if (budget.isNotEmpty()) {
                    appendLine("-- Budget items (${budget.size}) --")
                    budget.forEach { appendLine("  ${it.name} | ${it.category} | ${it.amount} | ${it.date}") }
                    appendLine()
                }
                val meas = allMeasurements.filter { it.projectId == p.id }
                if (meas.isNotEmpty()) {
                    appendLine("-- Measurements (${meas.size}) --")
                    meas.forEach { appendLine("  ${it.name} | roomId=${it.roomId} | ${it.value} ${it.unit}") }
                    appendLine()
                }
                val notes = allNotes.filter { it.projectId == p.id }
                if (notes.isNotEmpty()) {
                    appendLine("-- Notes (${notes.size}) --")
                    notes.forEach { appendLine("  ${it.title}: ${it.content.take(80)}") }
                    appendLine()
                }
                val ideas = allIdeas.filter { it.projectId == p.id }
                if (ideas.isNotEmpty()) {
                    appendLine("-- Ideas (${ideas.size}) --")
                    ideas.forEach { appendLine("  ${it.title} | ${it.tags}: ${it.description.take(60)}") }
                    appendLine()
                }
                appendLine("-------------------------------------------")
                appendLine()
            }
        }
        onReady(backup)
    }
}

class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
