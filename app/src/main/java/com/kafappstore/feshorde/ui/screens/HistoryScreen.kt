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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import com.kafappstore.feshorde.data.db.CompressedFileEntity
import com.kafappstore.feshorde.data.engine.StorageStatsManager
import com.kafappstore.feshorde.ui.components.AppHeader
import com.kafappstore.feshorde.ui.components.PersianRtlLayout
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyFiles: List<CompressedFileEntity>,
    onBackClick: () -> Unit,
    onDeleteFile: (CompressedFileEntity) -> Unit,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val filteredFiles = remember(historyFiles, selectedCategory, searchQuery) {
        historyFiles.filter { entity ->
            val matchCategory = when (selectedCategory) {
                "IMAGE" -> entity.fileType == "IMAGE"
                "VIDEO" -> entity.fileType == "VIDEO"
                "AUDIO" -> entity.fileType == "AUDIO"
                "ZIP" -> entity.fileType == "ZIP"
                else -> true
            }
            val matchQuery = searchQuery.isBlank() || entity.fileName.contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    PersianRtlLayout {
        Scaffold(
            topBar = {
                AppHeader(
                    title = "تاریخچه فایل‌های فشرده شده",
                    showBackButton = true,
                    onBackClick = onBackClick
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Search & Category Filters
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("جستجو در فایل‌های فشرده...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_history"),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair("همه", "ALL"),
                            Pair("عکس", "IMAGE"),
                            Pair("ویدیو", "VIDEO"),
                            Pair("صوت", "AUDIO"),
                            Pair("زیپ", "ZIP")
                        ).forEach { (label, cat) ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(label, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    if (historyFiles.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تعداد: ${StorageStatsManager.toPersianDigits("${filteredFiles.size}")} مورد",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            TextButton(
                                onClick = { showClearConfirmDialog = true },
                                modifier = Modifier.testTag("btn_clear_history_all")
                            ) {
                                Text("پاکسازی کامل تاریخچه", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (filteredFiles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "هیچ فایلی یافت نشد",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(filteredFiles, key = { it.id }) { item ->
                            val dateStr = remember(item.timestamp) {
                                val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
                                StorageStatsManager.toPersianDigits(sdf.format(Date(item.timestamp)))
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
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
                                            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.fileName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = dateStr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(GreenToolBg)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${StorageStatsManager.toPersianDigits("${item.savedPercentage}")}٪-",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = GreenToolIcon
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "قبل: ${StorageStatsManager.formatBytes(item.originalSizeBytes)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "بعد: ${StorageStatsManager.formatBytes(item.compressedSizeBytes)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = GreenToolIcon
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(onClick = {
                                            StorageStatsManager.shareFile(context, File(item.compressedPath))
                                        }) {
                                            Icon(Icons.Default.Share, contentDescription = "اشتراک", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        IconButton(onClick = {
                                            StorageStatsManager.openFile(context, File(item.compressedPath))
                                        }) {
                                            Icon(Icons.Default.OpenInNew, contentDescription = "باز کردن", tint = RoyalBlue)
                                        }

                                        IconButton(onClick = { onDeleteFile(item) }) {
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

                if (showClearConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearConfirmDialog = false },
                        title = { Text("پاکسازی تاریخچه") },
                        text = { Text("آیا از حذف تمام تاریخچه و فایل‌های فشرده شده اطمینان دارید؟") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    onClearAll()
                                    showClearConfirmDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7675))
                            ) {
                                Text("حذف همه")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearConfirmDialog = false }) {
                                Text("انصراف")
                            }
                        }
                    )
                }
            }
        }
    }
}
