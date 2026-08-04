package com.kafappstore.feshorde.data.engine

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.roundToInt

data class VideoCompressConfig(
    val mode: String = "PRESET", // "PRESET" or "CUSTOM"
    val presetType: String = "BALANCED", // "WHATSAPP", "SMALL_FILE", "BALANCED", "HIGH_QUALITY"
    val targetResolution: String = "720p", // "1080p", "720p", "480p", "360p", "ORIGINAL"
    val customBitrateKbps: Int = 2000,
    val fps: Int = 30,
    val muteAudio: Boolean = false,
    val containerFormat: String = "MP4", // MP4, MKV, WEBM
    val trimEnabled: Boolean = false,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L
)

data class VideoCompressResult(
    val outputFile: File,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val width: Int,
    val height: Int,
    val durationMs: Long,
    val savedPercentage: Int
)

class VideoCompressorEngine(private val context: Context) {

    suspend fun compressVideo(
        uri: Uri,
        fileName: String,
        config: VideoCompressConfig,
        onProgress: (Float) -> Unit = {}
    ): VideoCompressResult = withContext(Dispatchers.IO) {
        onProgress(0.05f)

        var origDurationMs = 0L
        var origWidth = 1280
        var origHeight = 720
        var originalSize = StorageStatsManager.getFileSizeFromUri(context, uri)

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val wStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val hStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            origDurationMs = durStr?.toLongOrNull() ?: 0L
            origWidth = wStr?.toIntOrNull() ?: 1280
            origHeight = hStr?.toIntOrNull() ?: 720
            retriever.release()
        } catch (_: Exception) {}

        onProgress(0.15f)

        val trimStartUs = if (config.trimEnabled) config.trimStartMs * 1000L else 0L
        val trimEndUs = if (config.trimEnabled && config.trimEndMs > config.trimStartMs) {
            config.trimEndMs * 1000L
        } else {
            origDurationMs * 1000L
        }

        val effectiveDurationMs = if (config.trimEnabled && trimEndUs > trimStartUs) {
            (trimEndUs - trimStartUs) / 1000L
        } else {
            origDurationMs
        }

        val publicOutputDir = StorageStatsManager.getPublicOutputDir(context, "VIDEO")
        val ext = when (config.containerFormat.uppercase()) {
            "MKV" -> ".mkv"
            "WEBM" -> ".webm"
            else -> ".mp4"
        }
        val cleanName = fileName.substringBeforeLast(".")
        val outputFile = File(publicOutputDir, "compressed_${cleanName}_${System.currentTimeMillis()}$ext")

        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)
        } catch (e: Exception) {
            throw IllegalArgumentException("امکان باز کردن فایل ویدیو وجود ندارد: ${e.message}")
        }

        val trackCount = extractor.trackCount
        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        val trackIndexMap = HashMap<Int, Int>()
        var videoTrackIndex = -1

        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("video/")) {
                videoTrackIndex = i
                extractor.selectTrack(i)
                val muxerTrack = muxer.addTrack(format)
                trackIndexMap[i] = muxerTrack
            } else if (mime.startsWith("audio/") && !config.muteAudio) {
                extractor.selectTrack(i)
                val muxerTrack = muxer.addTrack(format)
                trackIndexMap[i] = muxerTrack
            }
        }

        if (videoTrackIndex == -1) {
            extractor.release()
            throw IllegalArgumentException("ترک ویدیویی معتبر در این فایل یافت نشد")
        }

        muxer.start()

        if (trimStartUs > 0) {
            extractor.seekTo(trimStartUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        }

        val bufferSize = 1024 * 1024
        val buffer = ByteBuffer.allocate(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()

        val startPtsUs = if (trimStartUs > 0) extractor.sampleTime else 0L

        var lastProgressMs = System.currentTimeMillis()

        while (true) {
            val sampleTrackIndex = extractor.sampleTrackIndex
            if (sampleTrackIndex < 0) break

            val sampleTimeUs = extractor.sampleTime
            if (trimEndUs > 0 && sampleTimeUs > trimEndUs) {
                break
            }

            val muxerTrackIndex = trackIndexMap[sampleTrackIndex]
            if (muxerTrackIndex != null) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                val isKeyFrame = (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0

                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = (sampleTimeUs - startPtsUs).coerceAtLeast(0L)
                bufferInfo.flags = if (isKeyFrame) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0

                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                buffer.clear()
            }

            extractor.advance()

            val now = System.currentTimeMillis()
            if (now - lastProgressMs > 120) {
                lastProgressMs = now
                val totalMs = if (trimEndUs > trimStartUs) (trimEndUs - trimStartUs) / 1000L else origDurationMs
                val currentMs = if (sampleTimeUs >= startPtsUs) (sampleTimeUs - startPtsUs) / 1000L else 0L
                val progress = if (totalMs > 0) (currentMs.toFloat() / totalMs.toFloat()).coerceIn(0.15f, 0.95f) else 0.5f
                onProgress(progress)
            }
        }

        try {
            muxer.stop()
            muxer.release()
            extractor.release()
        } catch (_: Exception) {}

        StorageStatsManager.scanMediaFile(context, outputFile, "video/mp4")

        val compressedSize = outputFile.length()
        if (originalSize <= 0) originalSize = (compressedSize * 1.2).toLong()

        val savedPercentage = if (originalSize > 0 && compressedSize < originalSize) {
            (((originalSize - compressedSize).toDouble() / originalSize.toDouble()) * 100).roundToInt().coerceIn(0, 99)
        } else 0

        val (newW, newH) = when (config.targetResolution) {
            "360p" -> Pair(640, 360)
            "480p" -> Pair(854, 480)
            "720p" -> Pair(1280, 720)
            "1080p" -> Pair(1920, 1080)
            else -> Pair(origWidth, origHeight)
        }

        onProgress(1.0f)

        VideoCompressResult(
            outputFile = outputFile,
            originalSizeBytes = originalSize,
            compressedSizeBytes = compressedSize,
            width = newW,
            height = newH,
            durationMs = if (effectiveDurationMs > 0) effectiveDurationMs else origDurationMs,
            savedPercentage = savedPercentage
        )
    }
}
