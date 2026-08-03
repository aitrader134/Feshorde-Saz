package com.kafappstore.feshorde.data.repository

import com.kafappstore.feshorde.data.db.CompressedFileDao
import com.kafappstore.feshorde.data.db.CompressedFileEntity
import kotlinx.coroutines.flow.Flow

class CompressorRepository(private val dao: CompressedFileDao) {
    val allFiles: Flow<List<CompressedFileEntity>> = dao.getAllCompressedFiles()
    val totalBytesSaved: Flow<Long?> = dao.getTotalBytesSaved()
    val totalFilesCount: Flow<Int> = dao.getTotalFileCount()

    fun getFilesByType(type: String): Flow<List<CompressedFileEntity>> {
        return dao.getFilesByType(type)
    }

    suspend fun saveCompressedFile(file: CompressedFileEntity): Long {
        return dao.insertFile(file)
    }

    suspend fun deleteFile(file: CompressedFileEntity) {
        dao.deleteFile(file)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
