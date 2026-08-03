package com.kafappstore.feshorde.data.engine

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.math.roundToInt

data class SelectedFileItem(
    val uri: Uri,
    val name: String,
    val size: Long
)

data class ZipCompressConfig(
    val zipName: String = "archive.zip",
    val compressionLevel: Int = 6, // 0 = Store, 1 = Fast, 6 = Normal, 9 = Ultra
    val isEncrypted: Boolean = false,
    val password: String? = null
)

data class ZipCompressResult(
    val zipFile: File,
    val originalTotalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val fileCount: Int,
    val savedPercentage: Int
)

class ZipCompressorEngine(private val context: Context) {

    suspend fun createZipArchive(
        files: List<SelectedFileItem>,
        config: ZipCompressConfig,
        onProgress: (Float) -> Unit = {}
    ): ZipCompressResult = withContext(Dispatchers.IO) {
        if (files.isEmpty()) {
            throw IllegalArgumentException("هیچ فایلی برای فشرده‌سازی انتخاب نشده است")
        }

        onProgress(0.05f)

        val outputDir = File(context.cacheDir, "compressed_zips").apply { mkdirs() }
        val sanitizedName = if (config.zipName.endsWith(".zip")) config.zipName else "${config.zipName}.zip"
        val outputFile = File(outputDir, "${System.currentTimeMillis()}_$sanitizedName")

        var originalTotalSize = 0L
        files.forEach { originalTotalSize += it.size }

        val zipOut = ZipOutputStream(FileOutputStream(outputFile))
        zipOut.setLevel(config.compressionLevel.coerceIn(0, 9))

        val buffer = ByteArray(16384)
        var processedBytes = 0L

        files.forEachIndexed { index, item ->
            val entryName = item.name.ifBlank { "file_$index" }
            val zipEntry = ZipEntry(entryName)
            zipOut.putNextEntry(zipEntry)

            val inputStream: InputStream? = context.contentResolver.openInputStream(item.uri)
            if (inputStream != null) {
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    zipOut.write(buffer, 0, bytesRead)
                    processedBytes += bytesRead

                    val progress = 0.10f + (0.85f * (processedBytes.toFloat() / originalTotalSize.coerceAtLeast(1L).toFloat()))
                    onProgress(progress.coerceIn(0.10f, 0.95f))
                }
                inputStream.close()
            }
            zipOut.closeEntry()
        }

        zipOut.close()

        onProgress(0.98f)

        val compressedSize = outputFile.length()
        val savedPercentage = if (originalTotalSize > 0) {
            (((originalTotalSize - compressedSize).toDouble() / originalTotalSize.toDouble()) * 100).roundToInt().coerceIn(0, 99)
        } else 0

        onProgress(1.0f)

        ZipCompressResult(
            zipFile = outputFile,
            originalTotalSizeBytes = originalTotalSize,
            compressedSizeBytes = compressedSize,
            fileCount = files.size,
            savedPercentage = savedPercentage
        )
    }

    suspend fun extractZipArchive(
        zipUri: Uri,
        outputFolderName: String = "extracted_files",
        onProgress: (Float) -> Unit = {}
    ): List<File> = withContext(Dispatchers.IO) {
        onProgress(0.1f)
        val extractedFiles = mutableListOf<File>()
        val outputDir = File(context.cacheDir, outputFolderName).apply { mkdirs() }

        val inputStream = context.contentResolver.openInputStream(zipUri)
            ?: throw IllegalArgumentException("امکان باز کردن فایل زیپ وجود ندارد")

        val zipIn = ZipInputStream(inputStream)
        var entry: ZipEntry? = zipIn.nextEntry

        val buffer = ByteArray(8192)

        while (entry != null) {
            if (!entry.isDirectory) {
                val newFile = File(outputDir, entry.name.substringAfterLast("/"))
                newFile.parentFile?.mkdirs()

                FileOutputStream(newFile).use { fos ->
                    var len: Int
                    while (zipIn.read(buffer).also { len = it } > 0) {
                        fos.write(buffer, 0, len)
                    }
                }
                extractedFiles.add(newFile)
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }

        zipIn.close()
        onProgress(1.0f)
        extractedFiles
    }
}
