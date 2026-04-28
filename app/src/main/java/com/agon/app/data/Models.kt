package com.agon.app.data

// Domain models mapped from Entities for UI usage

data class Category(
    val id: String,
    val name: String,
    val isSystem: Boolean,
    val count: Int = 0
)

data class Screenshot(
    val id: String, // imagePath
    val categoryId: String,
    val imageUrl: String, // same as imagePath
    val extractedText: String
)
