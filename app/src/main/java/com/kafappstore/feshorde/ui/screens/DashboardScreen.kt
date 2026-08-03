package com.kafappstore.feshorde.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kafappstore.feshorde.data.db.CompressedFileEntity
import com.kafappstore.feshorde.data.engine.StorageStatsManager
import com.kafappstore.feshorde.ui.components.AppHeader
import com.kafappstore.feshorde.ui.components.GridToolCard
import com.kafappstore.feshorde.ui.components.PersianRtlLayout
import com.kafappstore.feshorde.ui.components.StatCard
import com.kafappstore.feshorde.ui.theme.AmberToolBg
import com.kafappstore.feshorde.ui.theme.AmberToolIcon
import com.kafappstore.feshorde.ui.theme.GreenToolBg
import com.kafappstore.feshorde.ui.theme.GreenToolIcon
import com.kafappstore.feshorde.ui.theme.OrangeToolBg
import com.kafappstore.feshorde.ui.theme.OrangeToolIcon
import com.kafappstore.feshorde.ui.theme.PurpleToolBg
import com.kafappstore.feshorde.ui.theme.PurpleToolIcon
import com.kafappstore.feshorde.ui.theme.RoyalBlue
import java.io.File

@Composable
fun DashboardScreen(
    totalBytesSaved: Long,
    totalFilesCount: Int,
    recentFiles: List<CompressedFileEntity>,
    onNavigateToImage: () -> Unit,
    onNavigateToVideo: () -> Unit,
    onNavigateToAudio: () -> Unit,
    onNavigateToZip: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onDeleteHistoryFile: (CompressedFileEntity) -> Unit
) {
    val context = LocalContext.current
    val storageInfo = remember { StorageStatsManager.getStorageInfo() }

    PersianRtlLayout {
        Scaffold(
            topBar = {
                AppHeader(title = "فشرده ساز رسانه و فایل")
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    StatCard(
                        totalSavedText = StorageStatsManager.formatBytes(totalBytesSaved),
                        totalFilesCount = totalFilesCount,
                        usedStoragePercent = storageInfo.usedPercentage
                    )
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text = "ابزارهای فشرده‌سازی",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // 2x2 Grid of Compressor Tools
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GridToolCard(
                                title = "فشرده‌سازی عکس",
                                subtitle = "کاهش حجم JPG, PNG, WebP تا ۹۰٪",
                                icon = Icons.Default.Image,
                                containerBg = PurpleToolBg,
                                iconColor = PurpleToolIcon,
                                testTag = "tool_image",
                                onClick = onNavigateToImage,
                                modifier = Modifier.weight(1f)
                            )

                            GridToolCard(
                                title = "فشرده‌سازی ویدیو",
                                subtitle = "کاهش رزولوشن MP4 و بیت‌ریت",
                                icon = Icons.Default.Videocam,
                                containerBg = OrangeToolBg,
                                iconColor = OrangeToolIcon,
                                testTag = "tool_video",
                                onClick = onNavigateToVideo,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GridToolCard(
                                title = "فشرده‌سازی صوت",
                                subtitle = "تنظیم بیت‌ریت MP3, AAC",
                                icon = Icons.Default.MusicNote,
                                containerBg = GreenToolBg,
                                iconColor = GreenToolIcon,
                                testTag = "tool_audio",
                                onClick = onNavigateToAudio,
                                modifier = Modifier.weight(1f)
                            )

                            GridToolCard(
                                title = "فایل‌های ZIP",
                                subtitle = "ساخت و استخراج آرشیو فشرده",
                                icon = Icons.Default.FolderZip,
                                containerBg = AmberToolBg,
                                iconColor = AmberToolIcon,
                                testTag = "tool_zip",
                                onClick = onNavigateToZip,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تاریخچه اخیر",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        TextButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier.testTag("btn_view_all_history")
                        ) {
                            Text(
                                text = "مشاهده همه",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = RoyalBlue
                            )
                        }
                    }
                }

                if (recentFiles.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "هنوز فایلی فشرده نشده است",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(recentFiles.take(5)) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val (icon, bg, tint) = when (item.fileType) {
                                    "IMAGE" -> Triple(Icons.Default.Image, PurpleToolBg, PurpleToolIcon)
                                    "VIDEO" -> Triple(Icons.Default.Videocam, OrangeToolBg, OrangeToolIcon)
                                    "AUDIO" -> Triple(Icons.Default.MusicNote, GreenToolBg, GreenToolIcon)
                                    else -> Triple(Icons.Default.FolderZip, AmberToolBg, AmberToolIcon)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.fileName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${StorageStatsManager.formatBytes(item.compressedSizeBytes)} • کاهش ${StorageStatsManager.toPersianDigits("${item.savedPercentage}")}٪",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = GreenToolIcon
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val file = File(item.compressedPath)
                                        StorageStatsManager.shareFile(context, file)
                                    }
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "اشتراک", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                IconButton(
                                    onClick = {
                                        val file = File(item.compressedPath)
                                        StorageStatsManager.openFile(context, file)
                                    }
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = "باز کردن", tint = RoyalBlue)
                                }

                                IconButton(
                                    onClick = { onDeleteHistoryFile(item) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
