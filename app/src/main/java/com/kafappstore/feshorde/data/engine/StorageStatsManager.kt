package com.kafappstore.feshorde.data.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

data class SystemStorageInfo(
    val totalSpaceBytes: Long,
    val freeSpaceBytes: Long,
    val usedSpaceBytes: Long,
    val usedPercentage: Int
)

object StorageStatsManager {

    fun getStorageInfo(): SystemStorageInfo {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val total = totalBlocks * blockSize
            val free = availableBlocks * blockSize
            val used = total - free
            val percentage = if (total > 0) ((used.toDouble() / total.toDouble()) * 100).toInt() else 0

            SystemStorageInfo(
                totalSpaceBytes = total,
                freeSpaceBytes = free,
                usedSpaceBytes = used,
                usedPercentage = percentage
            )
        } catch (e: Exception) {
            SystemStorageInfo(100L * 1024 * 1024 * 1024, 40L * 1024 * 1024 * 1024, 60L * 1024 * 1024 * 1024, 60)
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "۰ بایت"
        val units = arrayOf("بایت", "کیلوبایت", "مگابایت", "گیگابایت", "ترابایت")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        val formattedValue = String.format(Locale.US, "%.1f", value)
        val persianDigits = toPersianDigits(formattedValue)
        return "$persianDigits ${units[digitGroups]}"
    }

    fun toPersianDigits(input: String): String {
        val persianNumbers = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val builder = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                builder.append(persianNumbers[ch - '0'])
            } else {
                builder.append(ch)
            }
        }
        return builder.toString()
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = c.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "file_${System.currentTimeMillis()}"
    }

    fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val sizeIndex = c.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        return c.getLong(sizeIndex)
                    }
                }
            }
        }
        val file = File(uri.path ?: "")
        if (file.exists()) {
            return file.length()
        }
        return 0L
    }

    fun shareFile(context: Context, file: File, mimeType: String = "*/*") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری فایل فشرده شده"))
        } catch (e: Exception) {
            // Fallback
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری"))
        }
    }

    fun getPublicOutputDir(context: Context, fileType: String, subFolderName: String = "Feshorde"): File {
        val baseDir = when (fileType.uppercase()) {
            "VIDEO" -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            "IMAGE" -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            "AUDIO" -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            else -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        }

        val targetDir = if (baseDir != null && (baseDir.exists() || baseDir.mkdirs())) {
            File(baseDir, subFolderName)
        } else {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), subFolderName)
        }

        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        return targetDir
    }

    fun scanMediaFile(context: Context, file: File, mimeType: String? = null) {
        try {
            android.media.MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                if (mimeType != null) arrayOf(mimeType) else null,
                null
            )
        } catch (_: Exception) {}
    }

    fun openFile(context: Context, file: File, mimeType: String = "*/*") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
