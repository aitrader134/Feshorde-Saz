package com.kafappstore.feshorde.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kafappstore.feshorde.data.engine.ImageCompressConfig
import com.kafappstore.feshorde.data.engine.StorageStatsManager
import com.kafappstore.feshorde.ui.components.AppHeader
import com.kafappstore.feshorde.ui.components.PersianRtlLayout
import com.kafappstore.feshorde.ui.theme.PurpleToolBg
import com.kafappstore.feshorde.ui.theme.PurpleToolIcon
import com.kafappstore.feshorde.ui.theme.RoyalBlue

@Composable
fun ImageCompressorScreen(
    onBackClick: () -> Unit,
    onCompressImage: (Uri, ImageCompressConfig) -> Unit
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageSize by remember { mutableStateOf(0L) }
    var selectedImageName by remember { mutableStateOf("") }

    var selectedFormat by remember { mutableStateOf(Bitmap.CompressFormat.JPEG) }
    var quality by remember { mutableFloatStateOf(80f) }
    var scaleFactor by remember { mutableFloatStateOf(1.0f) }
    var isTargetSizeEnabled by remember { mutableStateOf(false) }
    var targetSizeMbStr by remember { mutableStateOf("1.0") }
    var preserveExif by remember { mutableStateOf(true) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            selectedImageSize = StorageStatsManager.getFileSizeFromUri(context, uri)
            selectedImageName = StorageStatsManager.getFileNameFromUri(context, uri)
        }
    }

    PersianRtlLayout {
        Scaffold(
            topBar = {
                AppHeader(
                    title = "فشرده‌سازی عکس",
                    showBackButton = true,
                    onBackClick = onBackClick
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Image Select Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { pickerLauncher.launch("image/*") }
                        .testTag("card_select_image"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                ) {
                    if (selectedImageUri == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(PurpleToolBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "انتخاب تصویر",
                                    modifier = Modifier.size(36.dp),
                                    tint = PurpleToolIcon
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "برای انتخاب تصویر اینجا کلیک کنید",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "پشتیبانی از فرمت‌های JPG, PNG, WEBP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(modifier = Modifier.padding(16.dp)) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "پیش‌نمایش عکس",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedImageName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "حجم اصلی: ${StorageStatsManager.formatBytes(selectedImageSize)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PurpleToolIcon
                                    )
                                }

                                Button(
                                    onClick = { pickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("تغییر عکس", color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedImageUri != null) {
                    // Controls
                    Text(
                        text = "تنظیمات فشرده‌سازی",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Format Selection
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "فرمت خروجی:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Pair("JPEG", Bitmap.CompressFormat.JPEG),
                                    Pair("WEBP", Bitmap.CompressFormat.WEBP),
                                    Pair("PNG", Bitmap.CompressFormat.PNG)
                                ).forEach { (label, fmt) ->
                                    FilterChip(
                                        selected = selectedFormat == fmt,
                                        onClick = { selectedFormat = fmt },
                                        label = { Text(label, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RoyalBlue,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quality Slider
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "کیفیت تصویر:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${StorageStatsManager.toPersianDigits("${quality.toInt()}")}٪",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = RoyalBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Slider(
                                value = quality,
                                onValueChange = { quality = it },
                                valueRange = 10f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = RoyalBlue,
                                    activeTrackColor = RoyalBlue
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Resolution Scaling
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "مقیاس ابعاد تصویر:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    Pair("۱۰۰٪ (اصلی)", 1.0f),
                                    Pair("۷۵٪", 0.75f),
                                    Pair("۵۰٪", 0.50f),
                                    Pair("۲۵٪", 0.25f)
                                ).forEach { (label, factor) ->
                                    FilterChip(
                                        selected = scaleFactor == factor,
                                        onClick = { scaleFactor = factor },
                                        label = { Text(label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RoyalBlue,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Target File Size Mode
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "محدودیت حجم هدف (مگابایت)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "تنظیم خودکار کیفیت برای رسیدن به حجم مورد نظر",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isTargetSizeEnabled,
                                    onCheckedChange = { isTargetSizeEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = RoyalBlue)
                                )
                            }

                            if (isTargetSizeEnabled) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = targetSizeMbStr,
                                    onValueChange = { targetSizeMbStr = it },
                                    label = { Text("حداکثر حجم (MB)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Start Compression Button
                    Button(
                        onClick = {
                            val uri = selectedImageUri ?: return@Button
                            val targetBytes = if (isTargetSizeEnabled) {
                                ((targetSizeMbStr.toDoubleOrNull() ?: 1.0) * 1024 * 1024).toLong()
                            } else null

                            val config = ImageCompressConfig(
                                format = selectedFormat,
                                quality = quality.toInt(),
                                scaleFactor = scaleFactor,
                                targetMaxSizeBytes = targetBytes,
                                preserveExif = preserveExif
                            )
                            onCompressImage(uri, config)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("btn_start_image_compress"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compress,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "شروع فشرده‌سازی عکس",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
