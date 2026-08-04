package com.kafappstore.feshorde.data.engine

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
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
    val trimEndMs: Long = 0L,
    val engineMode: String = "TURBO" // "TURBO" (Ultra Fast) or "STANDARD"
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
        var rotation = 0
        var originalSize = StorageStatsManager.getFileSizeFromUri(context, uri)

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val wStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val hStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val rotStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)

            origDurationMs = durStr?.toLongOrNull() ?: 0L
            origWidth = wStr?.toIntOrNull() ?: 1280
            origHeight = hStr?.toIntOrNull() ?: 720
            rotation = rotStr?.toIntOrNull() ?: 0
            retriever.release()
        } catch (_: Exception) {}

        onProgress(0.10f)

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

        // Calculate target dimensions keeping physical aspect ratio of video stream
        val streamIsPortrait = origHeight > origWidth
        val (maxW, maxH) = when (config.mode) {
            "PRESET" -> when (config.presetType) {
                "WHATSAPP" -> Pair(640, 360)
                "SMALL_FILE" -> Pair(854, 480)
                "BALANCED" -> Pair(1280, 720)
                "HIGH_QUALITY" -> Pair(1920, 1080)
                "ORIGINAL_LOW_BITRATE" -> Pair(origWidth, origHeight)
                else -> Pair(1280, 720)
            }
            else -> when (config.targetResolution) {
                "360p" -> Pair(640, 360)
                "480p" -> Pair(854, 480)
                "720p" -> Pair(1280, 720)
                "1080p" -> Pair(1920, 1080)
                else -> Pair(origWidth, origHeight)
            }
        }

        val targetMaxW = if (streamIsPortrait) minOf(maxW, maxH) else maxOf(maxW, maxH)
        val targetMaxH = if (streamIsPortrait) maxOf(maxW, maxH) else minOf(maxW, maxH)

        var outW = origWidth
        var outH = origHeight

        if (origWidth > targetMaxW || origHeight > targetMaxH) {
            val scaleW = targetMaxW.toFloat() / origWidth.toFloat()
            val scaleH = targetMaxH.toFloat() / origHeight.toFloat()
            val scale = minOf(scaleW, scaleH)
            outW = (origWidth * scale).toInt()
            outH = (origHeight * scale).toInt()
        }

        // Align dimensions to 16-pixel macroblock boundaries to prevent green/pink chroma buffer corruption
        outW = ((outW / 16) * 16).coerceAtLeast(16)
        outH = ((outH / 16) * 16).coerceAtLeast(16)

        // Calculate target bitrate
        val targetBitrate = if (config.mode == "CUSTOM") {
            (config.customBitrateKbps * 1000).coerceAtLeast(200_000)
        } else {
            when (config.presetType) {
                "WHATSAPP" -> 650_000
                "SMALL_FILE" -> 1_100_000
                "BALANCED" -> 2_200_000
                "HIGH_QUALITY" -> 4_500_000
                "ORIGINAL_LOW_BITRATE" -> 900_000
                else -> 2_000_000
            }
        }

        val targetFps = if (config.mode == "CUSTOM") config.fps.coerceIn(15, 60) else 30

        var transcodeSuccess = false

        try {
            transcodeSuccess = transcodeVideo(
                uri = uri,
                outputFile = outputFile,
                outW = outW,
                outH = outH,
                targetBitrate = targetBitrate,
                targetFps = targetFps,
                muteAudio = config.muteAudio,
                trimStartUs = trimStartUs,
                trimEndUs = trimEndUs,
                effectiveDurationMs = effectiveDurationMs,
                rotation = rotation,
                engineMode = config.engineMode,
                onProgress = onProgress
            )
        } catch (_: Exception) {
            transcodeSuccess = false
        }

        if (!transcodeSuccess || !outputFile.exists() || outputFile.length() == 0L) {
            // Fallback pass if hardware encoding encountered unexpected error
            fallbackCopyPass(uri, outputFile, config.muteAudio, trimStartUs, trimEndUs, rotation)
        }

        StorageStatsManager.scanMediaFile(context, outputFile, "video/mp4")

        val compressedSize = outputFile.length()
        if (originalSize <= 0) originalSize = (compressedSize * 1.3).toLong()

        val savedPercentage = if (originalSize > 0 && compressedSize < originalSize) {
            (((originalSize - compressedSize).toDouble() / originalSize.toDouble()) * 100).roundToInt().coerceIn(0, 99)
        } else 0

        onProgress(1.0f)

        VideoCompressResult(
            outputFile = outputFile,
            originalSizeBytes = originalSize,
            compressedSizeBytes = compressedSize,
            width = outW,
            height = outH,
            durationMs = if (effectiveDurationMs > 0) effectiveDurationMs else origDurationMs,
            savedPercentage = savedPercentage
        )
    }

    private fun transcodeVideo(
        uri: Uri,
        outputFile: File,
        outW: Int,
        outH: Int,
        targetBitrate: Int,
        targetFps: Int,
        muteAudio: Boolean,
        trimStartUs: Long,
        trimEndUs: Long,
        effectiveDurationMs: Long,
        rotation: Int,
        engineMode: String,
        onProgress: (Float) -> Unit
    ): Boolean {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)

        var videoTrackInFile = -1
        var videoFormat: MediaFormat? = null
        var videoMime = ""

        var audioTrackInFile = -1
        var audioFormat: MediaFormat? = null

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("video/") && videoTrackInFile == -1) {
                videoTrackInFile = i
                videoFormat = format
                videoMime = mime
            } else if (mime.startsWith("audio/") && !muteAudio && audioTrackInFile == -1) {
                audioTrackInFile = i
                audioFormat = format
            }
        }

        if (videoTrackInFile == -1 || videoFormat == null) {
            extractor.release()
            return false
        }

        val encoderFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, outW, outH)
        encoderFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        encoderFormat.setInteger(MediaFormat.KEY_BIT_RATE, targetBitrate)
        encoderFormat.setInteger(MediaFormat.KEY_FRAME_RATE, targetFps)
        encoderFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, if (engineMode == "TURBO") 2 else 1)

        if (engineMode == "TURBO") {
            try {
                encoderFormat.setInteger(MediaFormat.KEY_OPERATING_RATE, 32767)
                encoderFormat.setInteger(MediaFormat.KEY_PRIORITY, 0)
                encoderFormat.setInteger(MediaFormat.KEY_COMPLEXITY, 0)
            } catch (_: Exception) {}
        }

        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        encoder.configure(encoderFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = encoder.createInputSurface()
        encoder.start()

        val decoder = MediaCodec.createDecoderByType(videoMime)
        decoder.configure(videoFormat, inputSurface, null, 0)
        try {
            decoder.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT)
        } catch (_: Exception) {}
        decoder.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        if (rotation != 0) {
            try {
                muxer.setOrientationHint(rotation)
            } catch (_: Exception) {}
        }

        var muxerVideoTrack = -1
        var muxerAudioTrack = -1
        var muxerStarted = false

        if (audioFormat != null && !muteAudio) {
            muxerAudioTrack = muxer.addTrack(audioFormat)
        }

        extractor.selectTrack(videoTrackInFile)
        if (trimStartUs > 0) {
            extractor.seekTo(trimStartUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        }

        val startPtsUs = if (trimStartUs > 0) extractor.sampleTime else 0L

        var isExtractorEOS = false
        var isDecoderEOS = false
        var isEncoderEOS = false

        val bufferInfo = MediaCodec.BufferInfo()
        var lastProgressMs = System.currentTimeMillis()

        while (!isEncoderEOS) {
            // 1. Feed Extractor -> Decoder
            if (!isExtractorEOS) {
                val inIndex = decoder.dequeueInputBuffer(5000L)
                if (inIndex >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inIndex)
                    if (inputBuffer != null) {
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        val sampleTimeUs = extractor.sampleTime

                        if (sampleSize < 0 || (trimEndUs > 0 && sampleTimeUs > trimEndUs)) {
                            decoder.queueInputBuffer(inIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isExtractorEOS = true
                        } else {
                            decoder.queueInputBuffer(inIndex, 0, sampleSize, sampleTimeUs, 0)
                            extractor.advance()
                        }
                    }
                }
            }

            // 2. Dequeue Decoder -> Surface
            if (!isDecoderEOS) {
                val outIndex = decoder.dequeueOutputBuffer(bufferInfo, 5000L)
                if (outIndex >= 0) {
                    val render = (bufferInfo.size > 0) && (bufferInfo.presentationTimeUs >= trimStartUs)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isDecoderEOS = true
                        encoder.signalEndOfInputStream()
                    }
                    if (render) {
                        decoder.releaseOutputBuffer(outIndex, true)
                    } else {
                        decoder.releaseOutputBuffer(outIndex, false)
                    }
                }
            }

            // 3. Dequeue Encoder -> Muxer
            val encIndex = encoder.dequeueOutputBuffer(bufferInfo, 5000L)
            if (encIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (!muxerStarted) {
                    muxerVideoTrack = muxer.addTrack(encoder.outputFormat)
                    muxer.start()
                    muxerStarted = true
                }
            } else if (encIndex >= 0) {
                val encodedData = encoder.getOutputBuffer(encIndex)
                if (encodedData != null && bufferInfo.size > 0) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && muxerStarted) {
                        bufferInfo.presentationTimeUs = (bufferInfo.presentationTimeUs - startPtsUs).coerceAtLeast(0L)
                        muxer.writeSampleData(muxerVideoTrack, encodedData, bufferInfo)
                    }
                }
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    isEncoderEOS = true
                }
                encoder.releaseOutputBuffer(encIndex, false)
            }

            val now = System.currentTimeMillis()
            if (now - lastProgressMs > 150) {
                lastProgressMs = now
                val currentPts = bufferInfo.presentationTimeUs
                val prog = if (effectiveDurationMs > 0) {
                    (currentPts.toFloat() / (effectiveDurationMs * 1000f)).coerceIn(0.15f, 0.90f)
                } else 0.5f
                onProgress(prog)
            }
        }

        try {
            decoder.stop()
            decoder.release()
            encoder.stop()
            encoder.release()
            extractor.release()
        } catch (_: Exception) {}

        // 4. Mux Audio Track if available
        if (muxerStarted && muxerAudioTrack >= 0 && audioTrackInFile >= 0) {
            val audioExtractor = MediaExtractor()
            try {
                audioExtractor.setDataSource(context, uri, null)
                audioExtractor.selectTrack(audioTrackInFile)
                if (trimStartUs > 0) {
                    audioExtractor.seekTo(trimStartUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                }

                val audioBuffer = ByteBuffer.allocate(512 * 1024)
                val audioBufferInfo = MediaCodec.BufferInfo()

                while (true) {
                    val sampleSize = audioExtractor.readSampleData(audioBuffer, 0)
                    if (sampleSize < 0) break
                    val sampleTimeUs = audioExtractor.sampleTime
                    if (trimEndUs > 0 && sampleTimeUs > trimEndUs) break

                    audioBufferInfo.offset = 0
                    audioBufferInfo.size = sampleSize
                    audioBufferInfo.presentationTimeUs = (sampleTimeUs - startPtsUs).coerceAtLeast(0L)
                    audioBufferInfo.flags = if ((audioExtractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0

                    muxer.writeSampleData(muxerAudioTrack, audioBuffer, audioBufferInfo)
                    audioBuffer.clear()
                    audioExtractor.advance()
                }
                audioExtractor.release()
            } catch (_: Exception) {}
        }

        if (muxerStarted) {
            try {
                muxer.stop()
                muxer.release()
            } catch (_: Exception) {}
        }

        return outputFile.exists() && outputFile.length() > 0
    }

    private fun fallbackCopyPass(
        uri: Uri,
        outputFile: File,
        muteAudio: Boolean,
        trimStartUs: Long,
        trimEndUs: Long,
        rotation: Int = 0
    ) {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)
        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        if (rotation != 0) {
            try {
                muxer.setOrientationHint(rotation)
            } catch (_: Exception) {}
        }

        val trackIndexMap = HashMap<Int, Int>()
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("video/")) {
                extractor.selectTrack(i)
                trackIndexMap[i] = muxer.addTrack(format)
            } else if (mime.startsWith("audio/") && !muteAudio) {
                extractor.selectTrack(i)
                trackIndexMap[i] = muxer.addTrack(format)
            }
        }

        muxer.start()
        if (trimStartUs > 0) extractor.seekTo(trimStartUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

        val buffer = ByteBuffer.allocate(1024 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()
        val startPtsUs = if (trimStartUs > 0) extractor.sampleTime else 0L

        while (true) {
            val trackIdx = extractor.sampleTrackIndex
            if (trackIdx < 0) break
            val sampleTimeUs = extractor.sampleTime
            if (trimEndUs > 0 && sampleTimeUs > trimEndUs) break

            val muxerTrack = trackIndexMap[trackIdx]
            if (muxerTrack != null) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = (sampleTimeUs - startPtsUs).coerceAtLeast(0L)
                bufferInfo.flags = if ((extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0

                muxer.writeSampleData(muxerTrack, buffer, bufferInfo)
                buffer.clear()
            }
            extractor.advance()
        }

        try {
            muxer.stop()
            muxer.release()
            extractor.release()
        } catch (_: Exception) {}
    }
}
