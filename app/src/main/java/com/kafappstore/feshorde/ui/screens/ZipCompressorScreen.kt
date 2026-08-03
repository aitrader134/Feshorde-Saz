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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Unarchive
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.kafappstore.feshorde.data.engine.SelectedFileItem
import com.kafappstore.feshorde.data.engine.StorageStatsManager
import com.kafappstore.feshorde.data.engine.ZipCompressConfig
import com.kafappstore.feshorde.ui.components.AppHeader
import com.kafappstore.feshorde.ui.components.PersianRtlLayout
import com.kafappstore.feshorde.ui.theme.AmberToolBg
import com.kafappstore.feshorde.ui.theme.AmberToolIcon
import com.kafappstore.feshorde.ui.theme.RoyalBlue

@Composable
fun ZipCompressorScreen(
    onBackClick: () -> Unit,
    onCreateZip: (List<SelectedFileItem>, ZipCompressConfig) -> Unit,
    onExtractZip: (Uri) -> Unit
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Create, 1 = Extract

    val selectedFiles = remember { mutableStateListOf<SelectedFileItem>() }
    var zipName by remember { mutableStateOf("archive.zip") }
    var compressionLevel by remember { mutableFloatStateOf(6f) }

    var selectedExtractUri by remember { mutableStateOf<Uri?>(null) }
    var selectedExtractName by remember { mutableStateOf("") }
    var selectedExtractSize by remember { mutableStateOf(0L) }

    val multiFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val name = StorageStatsManager.getFileNameFromUri(context, uri)
            val size = StorageStatsManager.getFileSizeFromUri(context, uri)
            selectedFiles.add(SelectedFileItem(uri, name, size))
        }
    }

    val extractZipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedExtractUri = uri
            selectedExtractName = StorageStatsManager.getFileNameFromUri(context, uri)
            selectedExtractSize = StorageStatsManager.getFileSizeFromUri(context, uri)
        }
    }

    PersianRtlLayout {
        Scaffold(
            topBar = {
                AppHeader(
                    title = "آرشیو و فشرده‌سازی ZIP",
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
            ) {
                // Mode Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = RoyalBlue
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("ساخت فایل زیپ (ZIP)", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
                        modifier = Modifier.testTag("tab_create_zip")
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("استخراج فایل زیپ", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Unarchive, contentDescription = null) },
                        modifier = Modifier.testTag("tab_extract_zip")
                    )
                }

                if (selectedTabIndex == 0) {
                    // Create Zip Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable { multiFilePickerLauncher.launch("*/*") }
                                    .testTag("btn_select_multi_files"),
                                shape = RoundedCornerShape(22.dp),
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
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.foundation.layout.Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(AmberToolBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = AmberToolIcon
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "افزودن فایل‌ها به آرشیو",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "امکان انتخاب چند فایل همزمان (عکس، سند، موزیک...)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (selectedFiles.isNotEmpty()) {
                            item {
                                Text(
                                    text = "فایل‌های انتخاب شده (${StorageStatsManager.toPersianDigits("${selectedFiles.size}")}):",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            itemsIndexed(selectedFiles) { index, item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderZip,
                                            contentDescription = null,
                                            tint = AmberToolIcon,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = StorageStatsManager.formatBytes(item.size),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { selectedFiles.removeAt(index) }) {
                                            Icon(Icons.Default.Close, contentDescription = "حذف", tint = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = zipName,
                                    onValueChange = { zipName = it },
                                    label = { Text("نام فایل آرشیو زیپ") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

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
                                                text = "سطح فشرده‌سازی زیپ:",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = when (compressionLevel.toInt()) {
                                                    0 -> "ذخیره بدون فشرده‌سازی (Store)"
                                                    1 -> "فشرده‌سازی سریع (Fast)"
                                                    6 -> "استاندارد (Standard)"
                                                    else -> "فوق فشرده (Ultra)"
                                                },
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = RoyalBlue
                                            )
                                        }
                                        Slider(
                                            value = compressionLevel,
                                            onValueChange = { compressionLevel = it },
                                            valueRange = 0f..9f,
                                            steps = 3,
                                            colors = SliderDefaults.colors(thumbColor = RoyalBlue, activeTrackColor = RoyalBlue)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        val config = ZipCompressConfig(
                                            zipName = zipName,
                                            compressionLevel = compressionLevel.toInt()
                                        )
                                        onCreateZip(selectedFiles.toList(), config)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("btn_start_zip_create"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                                ) {
                                    Icon(Icons.Default.Compress, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ساخت فایل زیپ فشرده", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Extract Zip Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { extractZipPickerLauncher.launch("application/zip") }
                                .testTag("btn_select_zip_to_extract"),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            if (selectedExtractUri == null) {
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
                                            .background(AmberToolBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Unarchive,
                                            contentDescription = "انتخاب فایل زیپ",
                                            modifier = Modifier.size(36.dp),
                                            tint = AmberToolIcon
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "برای انتخاب فایل ZIP جهت استخراج کلیک کنید",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = selectedExtractName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "حجم: ${StorageStatsManager.formatBytes(selectedExtractSize)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AmberToolIcon
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (selectedExtractUri != null) {
                            Button(
                                onClick = {
                                    val uri = selectedExtractUri ?: return@Button
                                    onExtractZip(uri)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("btn_start_zip_extract"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                            ) {
                                Icon(Icons.Default.Unarchive, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("استخراج فایل زیپ", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
