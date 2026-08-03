package com.kafappstore.feshorde.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kafappstore.feshorde.data.db.AppDatabase
import com.kafappstore.feshorde.data.db.CompressedFileEntity
import com.kafappstore.feshorde.data.db.FileType
import com.kafappstore.feshorde.data.engine.AudioCompressConfig
import com.kafappstore.feshorde.data.engine.AudioCompressorEngine
import com.kafappstore.feshorde.data.engine.ImageCompressConfig
import com.kafappstore.feshorde.data.engine.ImageCompressorEngine
import com.kafappstore.feshorde.data.engine.SelectedFileItem
import com.kafappstore.feshorde.data.engine.StorageStatsManager
import com.kafappstore.feshorde.data.engine.VideoCompressConfig
import com.kafappstore.feshorde.data.engine.VideoCompressorEngine
import com.kafappstore.feshorde.data.engine.ZipCompressConfig
import com.kafappstore.feshorde.data.engine.ZipCompressorEngine
import com.kafappstore.feshorde.data.repository.CompressorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed class CompressionUiState {
    object Idle : CompressionUiState()
    data class Progress(val percentage: Float, val message: String) : CompressionUiState()
    data class Success(
        val fileEntity: CompressedFileEntity,
        val localFile: File
    ) : CompressionUiState()
    data class Error(val errorMessage: String) : CompressionUiState()
}

class CompressorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CompressorRepository
    private val imageEngine = ImageCompressorEngine(application)
    private val videoEngine = VideoCompressorEngine(application)
    private val audioEngine = AudioCompressorEngine(application)
    private val zipEngine = ZipCompressorEngine(application)

    init {
        val dao = AppDatabase.getDatabase(application).compressedFileDao()
        repository = CompressorRepository(dao)
    }

    val historyFiles: StateFlow<List<CompressedFileEntity>> = repository.allFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBytesSaved: StateFlow<Long?> = repository.totalBytesSaved
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalFilesCount: StateFlow<Int> = repository.totalFilesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _compressionState = MutableStateFlow<CompressionUiState>(CompressionUiState.Idle)
    val compressionState: StateFlow<CompressionUiState> = _compressionState.asStateFlow()

    fun resetCompressionState() {
        _compressionState.value = CompressionUiState.Idle
    }

    // Image Compression
    fun compressImage(uri: Uri, config: ImageCompressConfig) {
        viewModelScope.launch {
            try {
                _compressionState.value = CompressionUiState.Progress(0.1f, "در حال بارگذاری تصویر...")
                val context = getApplication<Application>()
                val fileName = StorageStatsManager.getFileNameFromUri(context, uri)

                val result = imageEngine.compressImage(uri, fileName, config) { prog ->
                    _compressionState.value = CompressionUiState.Progress(prog, "در حال فشرده‌سازی تصویر...")
                }

                val entity = CompressedFileEntity(
                    fileName = result.outputFile.name,
                    originalPath = uri.toString(),
                    compressedPath = result.outputFile.absolutePath,
                    fileType = FileType.IMAGE.name,
                    originalSizeBytes = result.originalSizeBytes,
                    compressedSizeBytes = result.compressedSizeBytes,
                    savedPercentage = result.savedPercentage,
                    format = config.format.name,
                    detailsInfo = "ابعاد: ${result.width}x${result.height}"
                )
                repository.saveCompressedFile(entity)

                _compressionState.value = CompressionUiState.Success(entity, result.outputFile)
            } catch (e: Exception) {
                _compressionState.value = CompressionUiState.Error(e.message ?: "خطای ناشناخته در فشرده‌سازی عکس")
            }
        }
    }

    // Video Compression
    fun compressVideo(uri: Uri, config: VideoCompressConfig) {
        viewModelScope.launch {
            try {
                _compressionState.value = CompressionUiState.Progress(0.1f, "در حال آماده‌سازی ویدیو...")
                val context = getApplication<Application>()
                val fileName = StorageStatsManager.getFileNameFromUri(context, uri)

                val result = videoEngine.compressVideo(uri, fileName, config) { prog ->
                    val percentageStr = (prog * 100).toInt()
                    _compressionState.value = CompressionUiState.Progress(prog, "در حال فشرده‌سازی ویدیو ($percentageStr٪)...")
                }

                val entity = CompressedFileEntity(
                    fileName = result.outputFile.name,
                    originalPath = uri.toString(),
                    compressedPath = result.outputFile.absolutePath,
                    fileType = FileType.VIDEO.name,
                    originalSizeBytes = result.originalSizeBytes,
                    compressedSizeBytes = result.compressedSizeBytes,
                    savedPercentage = result.savedPercentage,
                    format = config.containerFormat,
                    detailsInfo = "رزولوشن: ${result.width}x${result.height} | کیفیت: ${config.qualityPreset}"
                )
                repository.saveCompressedFile(entity)

                _compressionState.value = CompressionUiState.Success(entity, result.outputFile)
            } catch (e: Exception) {
                _compressionState.value = CompressionUiState.Error(e.message ?: "خطای ناشناخته در فشرده‌سازی ویدیو")
            }
        }
    }

    // Audio Compression
    fun compressAudio(uri: Uri, config: AudioCompressConfig) {
        viewModelScope.launch {
            try {
                _compressionState.value = CompressionUiState.Progress(0.1f, "در حال پردازش فایل صوتی...")
                val context = getApplication<Application>()
                val fileName = StorageStatsManager.getFileNameFromUri(context, uri)

                val result = audioEngine.compressAudio(uri, fileName, config) { prog ->
                    _compressionState.value = CompressionUiState.Progress(prog, "در حال فشرده‌سازی صوت...")
                }

                val entity = CompressedFileEntity(
                    fileName = result.outputFile.name,
                    originalPath = uri.toString(),
                    compressedPath = result.outputFile.absolutePath,
                    fileType = FileType.AUDIO.name,
                    originalSizeBytes = result.originalSizeBytes,
                    compressedSizeBytes = result.compressedSizeBytes,
                    savedPercentage = result.savedPercentage,
                    format = config.format,
                    detailsInfo = "بیت‌ریت: ${config.targetBitrateKbps}kbps | کانال: ${if (config.isMono) "مونو" else "استریو"}"
                )
                repository.saveCompressedFile(entity)

                _compressionState.value = CompressionUiState.Success(entity, result.outputFile)
            } catch (e: Exception) {
                _compressionState.value = CompressionUiState.Error(e.message ?: "خطای ناشناخته در فشرده‌سازی صوت")
            }
        }
    }

    // Zip Compression
    fun createZip(files: List<SelectedFileItem>, config: ZipCompressConfig) {
        viewModelScope.launch {
            try {
                _compressionState.value = CompressionUiState.Progress(0.1f, "در حال ساخت آرشیو زیپ...")

                val result = zipEngine.createZipArchive(files, config) { prog ->
                    _compressionState.value = CompressionUiState.Progress(prog, "در حال افزودن فایل‌ها به آرشیو...")
                }

                val entity = CompressedFileEntity(
                    fileName = result.zipFile.name,
                    originalPath = "multiple_files (${result.fileCount})",
                    compressedPath = result.zipFile.absolutePath,
                    fileType = FileType.ZIP.name,
                    originalSizeBytes = result.originalTotalSizeBytes,
                    compressedSizeBytes = result.compressedSizeBytes,
                    savedPercentage = result.savedPercentage,
                    format = "ZIP",
                    detailsInfo = "تعداد فایل‌ها: ${result.fileCount} | سطح فشرده‌سازی: ${config.compressionLevel}"
                )
                repository.saveCompressedFile(entity)

                _compressionState.value = CompressionUiState.Success(entity, result.zipFile)
            } catch (e: Exception) {
                _compressionState.value = CompressionUiState.Error(e.message ?: "خطای ناشناخته در ساخت آرشیو زیپ")
            }
        }
    }

    // Zip Extraction
    fun extractZip(uri: Uri) {
        viewModelScope.launch {
            try {
                _compressionState.value = CompressionUiState.Progress(0.2f, "در حال استخراج فایل زیپ...")
                val extractedFiles = zipEngine.extractZipArchive(uri) { prog ->
                    _compressionState.value = CompressionUiState.Progress(prog, "در حال استخراج فایل‌ها...")
                }

                val firstFile = extractedFiles.firstOrNull() ?: File(getApplication<Application>().cacheDir, "extracted_files")
                val entity = CompressedFileEntity(
                    fileName = "extracted_${firstFile.name}",
                    originalPath = uri.toString(),
                    compressedPath = firstFile.absolutePath,
                    fileType = FileType.ZIP.name,
                    originalSizeBytes = firstFile.length(),
                    compressedSizeBytes = firstFile.length(),
                    savedPercentage = 0,
                    format = "EXTRACTED",
                    detailsInfo = "فایل‌های استخراج شده: ${extractedFiles.size}"
                )
                repository.saveCompressedFile(entity)

                _compressionState.value = CompressionUiState.Success(entity, firstFile)
            } catch (e: Exception) {
                _compressionState.value = CompressionUiState.Error(e.message ?: "خطا در استخراج فایل زیپ")
            }
        }
    }

    fun deleteHistoryFile(entity: CompressedFileEntity) {
        viewModelScope.launch {
            try {
                val file = File(entity.compressedPath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (_: Exception) {}
            repository.deleteFile(entity)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
