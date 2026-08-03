package com.kafappstore.feshorde.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FileType {
    IMAGE,
    VIDEO,
    AUDIO,
    ZIP
}

@Entity(tableName = "compressed_files")
data class CompressedFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val originalPath: String,
    val compressedPath: String,
    val fileType: String, // IMAGE, VIDEO, AUDIO, ZIP
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val savedPercentage: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val format: String,
    val detailsInfo: String = ""
)
