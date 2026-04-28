package com.agon.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screenshots")
data class ScreenshotEntity(
    @PrimaryKey val imagePath: String,
    val extractedText: String,
    val categoryId: String,
    val lastModified: Long
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val systemKey: String?, // null if user created
    val isUserCreated: Boolean
)
