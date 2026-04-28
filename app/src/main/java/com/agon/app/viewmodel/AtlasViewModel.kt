package com.agon.app.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.data.AppDatabase
import com.agon.app.data.Category
import com.agon.app.data.CategoryEntity
import com.agon.app.data.Screenshot
import com.agon.app.data.ScreenshotEntity
import com.agon.app.utils.CategorizationHelper
import com.agon.app.utils.MediaScanner
import com.agon.app.utils.OCRHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore(name = "settings")

class AtlasViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore
    private val database = AppDatabase.getDatabase(application)
    private val screenshotDao = database.screenshotDao()
    private val categoryDao = database.categoryDao()

    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val SYSTEM_DEFAULT_KEY = booleanPreferencesKey("system_default_theme")

    val isSystemDefaultTheme: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[SYSTEM_DEFAULT_KEY] ?: true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val isDarkMode: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[DARK_MODE_KEY] ?: true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _screenshots = MutableStateFlow<List<Screenshot>>(emptyList())
    val screenshots = _screenshots.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    init {
        initializeDatabase()
        observeData()
    }

    private fun initializeDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val existingCategories = categoryDao.getAllCategoriesSync()
            if (existingCategories.isEmpty()) {
                val defaultCategories = listOf(
                    CategoryEntity("c1", "Shopping", "shopping", false),
                    CategoryEntity("c2", "Payments", "payments", false),
                    CategoryEntity("c3", "Study", "study", false),
                    CategoryEntity("c4", "Social", "social", false),
                    CategoryEntity("c5", "Others", "others", false)
                )
                categoryDao.insertCategories(defaultCategories)
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.getAllCategories().collect { entities ->
                _categories.value = entities.map { 
                    Category(it.id, it.displayName, !it.isUserCreated, 0) // count updated later
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            screenshotDao.getAllScreenshots().collect { entities ->
                _screenshots.value = entities.map {
                    Screenshot(it.imagePath, it.categoryId, it.imagePath, it.extractedText)
                }
            }
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DARK_MODE_KEY] = isDark
                preferences[SYSTEM_DEFAULT_KEY] = false
            }
        }
    }

    fun setSystemDefaultTheme() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[SYSTEM_DEFAULT_KEY] = true
            }
        }
    }

    fun createSmartFolder(name: String): Boolean {
        if (_categories.value.any { it.name.equals(name, ignoreCase = true) }) {
            return false
        }
        viewModelScope.launch(Dispatchers.IO) {
            val newCategory = CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                displayName = name,
                systemKey = null,
                isUserCreated = true
            )
            categoryDao.insertCategory(newCategory)
            // Re-categorize existing screenshots into the new folder if applicable
            reCategorizeAll()
        }
        return true
    }

    fun renameCategory(id: String, newName: String): Boolean {
        if (_categories.value.any { it.id != id && it.name.equals(newName, ignoreCase = true) }) {
            return false
        }
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.renameCategory(id, newName)
            // Re-categorize because the name changed (which affects rules)
            reCategorizeAll()
        }
        return true
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.deleteCategoryById(id)
            // Re-categorize screenshots that belonged to this deleted category
            reCategorizeAll()
        }
    }

    fun triggerScan(forceRescanAll: Boolean = false) {
        if (_isScanning.value) return
        
        viewModelScope.launch(Dispatchers.IO) {
            _isScanning.value = true
            try {
                val context = getApplication<Application>().applicationContext
                val allCategories = categoryDao.getAllCategoriesSync()
                
                val deviceScreenshots = MediaScanner.getScreenshots(context)
                val dbScreenshotsMetadata = screenshotDao.getScreenshotMetadata().associateBy { it.imagePath }
                
                for (media in deviceScreenshots) {
                    val dbEntry = dbScreenshotsMetadata[media.uri.toString()]
                    
                    val needsScan = forceRescanAll || dbEntry == null || dbEntry.lastModified != media.dateModified
                    
                    if (needsScan) {
                        val extractedText = OCRHelper.extractTextFromUri(context, media.uri)
                        val categoryId = CategorizationHelper.categorize(extractedText, allCategories)
                        
                        val newEntity = ScreenshotEntity(
                            imagePath = media.uri.toString(),
                            extractedText = extractedText,
                            categoryId = categoryId,
                            lastModified = media.dateModified
                        )
                        screenshotDao.insertScreenshot(newEntity)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }

    private suspend fun reCategorizeAll() {
        val allCategories = categoryDao.getAllCategoriesSync()
        val allScreenshots = screenshotDao.getAllScreenshotsSync()
        
        for (screenshot in allScreenshots) {
            val newCategoryId = CategorizationHelper.categorize(screenshot.extractedText, allCategories)
            if (newCategoryId != screenshot.categoryId) {
                screenshotDao.insertScreenshot(screenshot.copy(categoryId = newCategoryId))
            }
        }
    }

    fun clearDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            screenshotDao.deleteAllScreenshots()
            categoryDao.deleteUserCategories()
        }
    }
}
