package com.kafappstore.feshorde.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompressedFileDao {
    @Query("SELECT * FROM compressed_files ORDER BY timestamp DESC")
    fun getAllCompressedFiles(): Flow<List<CompressedFileEntity>>

    @Query("SELECT * FROM compressed_files WHERE fileType = :type ORDER BY timestamp DESC")
    fun getFilesByType(type: String): Flow<List<CompressedFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: CompressedFileEntity): Long

    @Delete
    suspend fun deleteFile(file: CompressedFileEntity)

    @Query("DELETE FROM compressed_files WHERE id = :id")
    suspend fun deleteFileById(id: Long)

    @Query("DELETE FROM compressed_files")
    suspend fun clearAll()

    @Query("SELECT SUM(originalSizeBytes - compressedSizeBytes) FROM compressed_files")
    fun getTotalBytesSaved(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM compressed_files")
    fun getTotalFileCount(): Flow<Int>
}
