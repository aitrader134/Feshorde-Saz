package com.kafappstore.feshorde.data.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.roundToInt

data class ImageCompressConfig(
    val format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    val quality: Int = 80, // 10..100
    val scaleFactor: Float = 1.0f, // 0.25f .. 1.0f
    val targetWidth: Int? = null,
    val targetHeight: Int? = null,
    val targetMaxSizeBytes: Long? = null, // e.g., 1MB = 1048576
    val preserveExif: Boolean = true
)

data class ImageCompressResult(
    val outputFile: File,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val width: Int,
    val height: Int,
    val savedPercentage: Int
)

class ImageCompressorEngine(private val context: Context) {

    suspend fun compressImage(
        uri: Uri,
        fileName: String,
        config: ImageCompressConfig,
        onProgress: (Float) -> Unit = {}
    ): ImageCompressResult = withContext(Dispatchers.IO) {
        onProgress(0.1f)
        
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("امکان باز کردن فایل تصویر وجود ندارد")
        
        val tempOriginalBytes = inputStream.readBytes()
        val originalSize = tempOriginalBytes.size.toLong()
        onProgress(0.3f)

        // Decode bounds first to check dimension
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(tempOriginalBytes, 0, tempOriginalBytes.size, options)
        val origWidth = options.outWidth
        val origHeight = options.outHeight

        // Calculate sample size if scaling down heavily
        var sampleSize = 1
        var reqWidth = (origWidth * config.scaleFactor).roundToInt().coerceAtLeast(1)
        var reqHeight = (origHeight * config.scaleFactor).roundToInt().coerceAtLeast(1)

        if (config.targetWidth != null && config.targetHeight != null) {
            reqWidth = config.targetWidth
            reqHeight = config.targetHeight
        }

        if (origWidth > reqWidth || origHeight > reqHeight) {
            val halfHeight = origHeight / 2
            val halfWidth = origWidth / 2
            while ((halfHeight / sampleSize) >= reqHeight && (halfWidth / sampleSize) >= reqWidth) {
                sampleSize *= 2
            }
        }

        onProgress(0.5f)

        // Decode bitmap with calculated sample size
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        var bitmap = BitmapFactory.decodeByteArray(tempOriginalBytes, 0, tempOriginalBytes.size, decodeOptions)
            ?: throw IllegalStateException("خطا در پردازش فایل تصویر")

        // Handle rotation from EXIF
        try {
            val exifStream: InputStream = context.contentResolver.openInputStream(uri)!!
            val exif = ExifInterface(exifStream)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            if (orientation != ExifInterface.ORIENTATION_NORMAL && orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) {
                    bitmap.recycle()
                    bitmap = rotated
                }
            }
            exifStream.close()
        } catch (_: Exception) { }

        // Fine resize if needed
        if (bitmap.width != reqWidth || bitmap.height != reqHeight) {
            val scaled = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true)
            if (scaled != bitmap) {
                bitmap.recycle()
                bitmap = scaled
            }
        }

        onProgress(0.7f)

        // Target File Size compress loop if specified
        var quality = config.quality
        val targetSize = config.targetMaxSizeBytes

        val outputDir = File(context.cacheDir, "compressed_images").apply { mkdirs() }
        val ext = when (config.format) {
            Bitmap.CompressFormat.PNG -> ".png"
            Bitmap.CompressFormat.WEBP -> ".webp"
            else -> ".jpg"
        }
        val cleanName = fileName.substringBeforeLast(".")
        val outputFile = File(outputDir, "compressed_${cleanName}_${System.currentTimeMillis()}$ext")

        var baos = ByteArrayOutputStream()
        bitmap.compress(config.format, quality, baos)

        if (targetSize != null && targetSize > 0) {
            while (baos.size() > targetSize && quality > 15) {
                quality -= 10
                baos.reset()
                bitmap.compress(config.format, quality, baos)
            }
        }

        FileOutputStream(outputFile).use { fos ->
            baos.writeTo(fos)
        }

        val compressedSize = outputFile.length()
        val savedPercentage = if (originalSize > 0) {
            (((originalSize - compressedSize).toDouble() / originalSize.toDouble()) * 100).roundToInt().coerceIn(0, 99)
        } else 0

        onProgress(1.0f)

        ImageCompressResult(
            outputFile = outputFile,
            originalSizeBytes = originalSize,
            compressedSizeBytes = compressedSize,
            width = bitmap.width,
            height = bitmap.height,
            savedPercentage = savedPercentage
        )
    }
}
