package com.agon.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {
    @Query("SELECT * FROM screenshots")
    fun getAllScreenshots(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots")
    suspend fun getAllScreenshotsSync(): List<ScreenshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshot(screenshot: ScreenshotEntity)

    @Query("DELETE FROM screenshots")
    suspend fun deleteAllScreenshots()
    
    @Query("SELECT imagePath, lastModified FROM screenshots")
    suspend fun getScreenshotMetadata(): List<ScreenshotMetadata>
}

data class ScreenshotMetadata(
    val imagePath: String,
    val lastModified: Long
)

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesSync(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE isUserCreated = 1")
    suspend fun deleteUserCategories()
    
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)
    
    @Query("UPDATE categories SET displayName = :newName WHERE id = :id")
    suspend fun renameCategory(id: String, newName: String)
}
