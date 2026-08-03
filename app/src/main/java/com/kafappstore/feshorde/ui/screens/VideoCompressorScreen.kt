package com.kafappstore.feshorde.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kafappstore.feshorde.data.engine.StorageStatsManager
import com.kafappstore.feshorde.data.engine.VideoCompressConfig
import com.kafappstore.feshorde.ui.components.AppHeader
import com.kafappstore.feshorde.ui.components.PersianRtlLayout
import com.kafappstore.feshorde.ui.theme.OrangeToolBg
import com.kafappstore.feshorde.ui.theme.OrangeToolIcon
import com.kafappstore.feshorde.ui.theme.RoyalBlue

@Composable
fun VideoCompressorScreen(
    onBackClick: () -> Unit,
    onCompressVideo: (Uri, VideoCompressConfig) -> Unit
) {
    val context = LocalContext.current
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoSize by remember { mutableStateOf(0L) }
    var selectedVideoName by remember { mutableStateOf("") }

    var targetResolution by remember { mutableStateOf("720p") }
    var qualityPreset by remember { mutableStateOf("BALANCED") }
    var muteAudio by remember { mutableStateOf(false) }
    var containerFormat by remember { mutableStateOf("MP4") }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            selectedVideoSize = StorageStatsManager.getFileSizeFromUri(context, uri)
            selectedVideoName = StorageStatsManager.getFileNameFromUri(context, uri)
        }
    }

    PersianRtlLayout {
        Scaffold(
            topBar = {
                AppHeader(
                    title = "فشرده‌سازی ویدیو",
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
                // Video Select Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { pickerLauncher.launch("video/*") }
                        .testTag("card_select_video"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                ) {
                    if (selectedVideoUri == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(OrangeToolBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "انتخاب ویدیو",
                                    modifier = Modifier.size(36.dp),
                                    tint = OrangeToolIcon
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "برای انتخاب ویدیو کلیک کنید",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "پشتیبانی از فرمت‌های MP4, MKV, MOV, WEBM",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoFile,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = OrangeToolIcon
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedVideoName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "حجم ویدیو: ${StorageStatsManager.formatBytes(selectedVideoSize)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OrangeToolIcon
                                )
                            }
                            Button(
                                onClick = { pickerLauncher.launch("video/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("تغییر ویدیو", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedVideoUri != null) {
                    Text(
                        text = "تنظیمات فشرده‌سازی ویدیو",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Target Resolution
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "رزولوشن خروجی:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("1080p", "720p", "480p", "360p").forEach { res ->
                                    FilterChip(
                                        selected = targetResolution == res,
                                        onClick = { targetResolution = res },
                                        label = { Text(res, fontWeight = FontWeight.Bold) },
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

                    // Quality Preset
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "سطح فشرده‌سازی:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    Pair("سریع (حداکثر کاهش)", "FAST"),
                                    Pair("متعادل", "BALANCED"),
                                    Pair("کیفیت بالا", "HIGH_QUALITY")
                                ).forEach { (label, preset) ->
                                    FilterChip(
                                        selected = qualityPreset == preset,
                                        onClick = { qualityPreset = preset },
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

                    // Mute Audio Toggle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "حذف صدای ویدیو",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "حذف صدای پس‌زمینه برای کاهش فوق‌العاده حجم",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = muteAudio,
                                onCheckedChange = { muteAudio = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RoyalBlue)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Compress Button
                    Button(
                        onClick = {
                            val uri = selectedVideoUri ?: return@Button
                            val config = VideoCompressConfig(
                                targetResolution = targetResolution,
                                qualityPreset = qualityPreset,
                                muteAudio = muteAudio,
                                containerFormat = containerFormat
                            )
                            onCompressVideo(uri, config)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("btn_start_video_compress"),
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
                            text = "شروع فشرده‌سازی ویدیو",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
