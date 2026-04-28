package com.agon.app.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

object MediaScanner {
    
    data class MediaImage(
        val id: Long,
        val uri: Uri,
        val path: String,
        val dateModified: Long
    )

    fun getScreenshots(context: Context): List<MediaImage> {
        val screenshots = mutableListOf<MediaImage>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_MODIFIED
        )
        
        // Query only screenshots
        val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%Screenshots%")
        
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val path = cursor.getString(dataColumn)
                val dateModified = cursor.getLong(dateModifiedColumn)
                
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                
                // Double check if it's actually in a Screenshots folder
                if (path.contains("Screenshots", ignoreCase = true)) {
                    screenshots.add(MediaImage(id, contentUri, path, dateModified))
                }
            }
        }
        
        return screenshots
    }
}
