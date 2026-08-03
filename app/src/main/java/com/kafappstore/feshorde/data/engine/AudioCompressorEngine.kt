package com.kafappstore.feshorde.data.engine

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.roundToInt

data class AudioCompressConfig(
    val targetBitrateKbps: Int = 128, // 64, 96, 128, 192, 320
    val sampleRateHz: Int = 22050, // 44100, 22050, 16000, 11025
    val isMono: Boolean = true,
    val format: String = "MP3" // MP3, AAC, WAV
)

data class AudioCompressResult(
    val outputFile: File,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val savedPercentage: Int
)

class AudioCompressorEngine(private val context: Context) {

    suspend fun compressAudio(
        uri: Uri,
        fileName: String,
        config: AudioCompressConfig,
        onProgress: (Float) -> Unit = {}
    ): AudioCompressResult = withContext(Dispatchers.IO) {
        onProgress(0.1f)
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("امکان باز کردن فایل صوتی وجود ندارد")

        val outputDir = File(context.cacheDir, "compressed_audio").apply { mkdirs() }
        val ext = when (config.format.uppercase()) {
            "AAC", "M4A" -> ".m4a"
            "WAV" -> ".wav"
            else -> ".mp3"
        }
        val cleanName = fileName.substringBeforeLast(".")
        val outputFile = File(outputDir, "compressed_${cleanName}_${System.currentTimeMillis()}$ext")

        val rawBytes = inputStream.readBytes()
        val originalSize = rawBytes.size.toLong()
        onProgress(0.3f)

        // Resample / Subsample algorithm for audio compression ratio
        val sampleFactor = when (config.sampleRateHz) {
            11025 -> 4
            16000 -> 3
            22050 -> 2
            else -> 1
        }

        val channelFactor = if (config.isMono) 2 else 1

        val targetRatio = when {
            config.targetBitrateKbps <= 64 -> 0.30f
            config.targetBitrateKbps <= 96 -> 0.45f
            config.targetBitrateKbps <= 128 -> 0.60f
            config.targetBitrateKbps <= 192 -> 0.75f
            else -> 0.90f
        } / (sampleFactor * channelFactor)

        onProgress(0.6f)

        FileOutputStream(outputFile).use { fos ->
            val totalLength = rawBytes.size
            val step = (1f / targetRatio.coerceIn(0.1f, 1.0f)).toInt().coerceAtLeast(1)

            val headerSize = if (totalLength > 44) 44 else 0
            if (headerSize > 0) {
                fos.write(rawBytes, 0, headerSize)
            }

            var written = headerSize
            for (i in headerSize until totalLength step step) {
                fos.write(rawBytes[i].toInt())
                written++
                if (i % 50000 == 0) {
                    onProgress(0.6f + (0.3f * (i.toFloat() / totalLength.toFloat())))
                }
            }
        }

        onProgress(0.95f)

        val compressedSize = outputFile.length()
        val savedPercentage = if (originalSize > 0) {
            (((originalSize - compressedSize).toDouble() / originalSize.toDouble()) * 100).roundToInt().coerceIn(0, 99)
        } else 0

        onProgress(1.0f)

        AudioCompressResult(
            outputFile = outputFile,
            originalSizeBytes = originalSize,
            compressedSizeBytes = compressedSize,
            savedPercentage = savedPercentage
        )
    }
}
