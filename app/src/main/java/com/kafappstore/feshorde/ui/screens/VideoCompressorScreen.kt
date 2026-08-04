package com.kafappstore.feshorde.ui.screens

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kafappstore.feshorde.data.AppStrings
import com.kafappstore.feshorde.data.LanguageManager
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
    var selectedVideoSize by remember { mutableLongStateOf(0L) }
    var selectedVideoName by remember { mutableStateOf("") }
    var videoDurationMs by remember { mutableLongStateOf(0L) }

    // Mode tab: 0 = Presets, 1 = Advanced Custom
    var selectedModeTab by remember { mutableIntStateOf(0) }

    // Engine mode: "TURBO" (Ultra Fast Hardware) or "STANDARD" (Compatible)
    var engineMode by remember { mutableStateOf("TURBO") }

    // Preset selection: WHATSAPP, SMALL_FILE, BALANCED, HIGH_QUALITY
    var presetType by remember { mutableStateOf("BALANCED") }

    // Custom Mode Parameters
    var targetResolution by remember { mutableStateOf("720p") }
    var customBitrateKbps by remember { mutableFloatStateOf(2000f) }
    var customFps by remember { mutableIntStateOf(30) }
    var containerFormat by remember { mutableStateOf("MP4") }
    var muteAudio by remember { mutableStateOf(false) }

    // Video Trimming parameters
    var isTrimEnabled by remember { mutableStateOf(false) }
    var trimStartSec by remember { mutableFloatStateOf(0f) }
    var trimEndSec by remember { mutableFloatStateOf(0f) }

    val isEn = LanguageManager.isEnglishCurrent()

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            selectedVideoSize = StorageStatsManager.getFileSizeFromUri(context, uri)
            selectedVideoName = StorageStatsManager.getFileNameFromUri(context, uri)

            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = durStr?.toLongOrNull() ?: 0L
                videoDurationMs = duration
                trimStartSec = 0f
                trimEndSec = (duration / 1000f).coerceAtLeast(1f)
                retriever.release()
            } catch (_: Exception) {
                videoDurationMs = 0L
            }
        }
    }

    PersianRtlLayout {
        Scaffold(
            topBar = {
                AppHeader(
                    title = AppStrings.getString("video_compressor"),
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
                // Video Picker Card
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
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                ) {
                    if (selectedVideoUri == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(OrangeToolBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = OrangeToolIcon
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isEn) "Click to Select Video" else "برای انتخاب ویدیو کلیک کنید",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isEn) "Supports MP4, MKV, MOV, WEBM" else "پشتیبانی از فرمت‌های MP4, MKV, MOV, WEBM",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(44.dp),
                                    tint = OrangeToolIcon
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedVideoName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = LanguageManager.formatBytes(selectedVideoSize),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = OrangeToolIcon
                                        )
                                        if (videoDurationMs > 0) {
                                            Text(
                                                text = "• ${formatDuration(videoDurationMs)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { pickerLauncher.launch("video/*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = if (isEn) "Change Selected Video" else "تغییر و انتخاب ویدیو جدید",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                if (selectedVideoUri != null) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Video Trimming Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCut,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (isEn) "Trim / Cut Video" else "برش و کات قسمتی از ویدیو",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isEn) "Compress only a selected section" else "فقط قسمت دلخواه ویدیو را فشرده کنید",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = isTrimEnabled,
                                    onCheckedChange = { isTrimEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = RoyalBlue)
                                )
                            }

                            if (isTrimEnabled) {
                                Spacer(modifier = Modifier.height(14.dp))
                                val maxSec = (videoDurationMs / 1000f).coerceAtLeast(1f)

                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isEn) "Start: ${formatSec(trimStartSec)}" else "زمان شروع: ${formatSec(trimStartSec)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = RoyalBlue
                                        )
                                        Text(
                                            text = if (isEn) "End: ${formatSec(trimEndSec)}" else "زمان پایان: ${formatSec(trimEndSec)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = RoyalBlue
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Slider(
                                        value = trimStartSec,
                                        onValueChange = {
                                            if (it < trimEndSec - 1f) trimStartSec = it
                                        },
                                        valueRange = 0f..maxSec,
                                        colors = SliderDefaults.colors(
                                            thumbColor = RoyalBlue,
                                            activeTrackColor = RoyalBlue
                                        )
                                    )

                                    Slider(
                                        value = trimEndSec,
                                        onValueChange = {
                                            if (it > trimStartSec + 1f) trimEndSec = it
                                        },
                                        valueRange = 0f..maxSec,
                                        colors = SliderDefaults.colors(
                                            thumbColor = OrangeToolIcon,
                                            activeTrackColor = OrangeToolIcon
                                        )
                                    )

                                    val finalDurationSec = (trimEndSec - trimStartSec).coerceAtLeast(0f)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = RoyalBlue.copy(alpha = 0.08f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (isEn)
                                                "Final Video Duration: ${formatSec(finalDurationSec)} (Total: ${formatSec(maxSec)})"
                                            else
                                                "مدت زمان نهایی ویدیو: ${formatSec(finalDurationSec)} (از کل ${formatSec(maxSec)})",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = RoyalBlue,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Compression Engine Selector (Turbo Speed vs Standard)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = OrangeToolIcon,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isEn) "Processing Engine Mode:" else "انتخاب موتور پردازش فشرده‌سازی:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isEn) "Choose hardware speed engine vs compatible mode" else "انتخاب موتور سخت‌افزاری توربو یا حالت استاندارد",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Turbo Button
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (engineMode == "TURBO") OrangeToolIcon.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        width = if (engineMode == "TURBO") 2.dp else 1.dp,
                                        color = if (engineMode == "TURBO") OrangeToolIcon else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { engineMode = "TURBO" }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (isEn) "🚀 Turbo Speed" else "🚀 توربو (فوق سریع)",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = if (engineMode == "TURBO") OrangeToolIcon else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isEn) "Max GPU Acceleration" else "حداکثر شتاب سخت‌افزاری",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Standard Button
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (engineMode == "STANDARD") RoyalBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        width = if (engineMode == "STANDARD") 2.dp else 1.dp,
                                        color = if (engineMode == "STANDARD") RoyalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { engineMode = "STANDARD" }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (isEn) "⚖️ Standard Mode" else "⚖️ حالت استاندارد",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = if (engineMode == "STANDARD") RoyalBlue else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isEn) "Broad Compatibility" else "سازگاری استاندارد",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Compression Mode Selector (Presets vs Custom Advanced)
                    TabRow(
                        selectedTabIndex = selectedModeTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        indicator = { tabPositions ->
                            if (selectedModeTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedModeTab]),
                                    color = RoyalBlue
                                )
                            }
                        },
                        modifier = Modifier.clip(RoundedCornerShape(14.dp))
                    ) {
                        Tab(
                            selected = selectedModeTab == 0,
                            onClick = { selectedModeTab = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = if (isEn) "Presets" else "پریست هوشمند",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            },
                            selectedContentColor = RoyalBlue,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Tab(
                            selected = selectedModeTab == 1,
                            onClick = { selectedModeTab = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = if (isEn) "Custom Control" else "تنظیمات پیشرفته",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            },
                            selectedContentColor = RoyalBlue,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedModeTab == 0) {
                        // Presets Mode Content
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isEn) "Select Compression Preset:" else "انتخاب پریست پیشنهادی:",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val presets = if (isEn) {
                                    listOf(
                                        Triple("WHATSAPP", "📱 Social Media / WhatsApp", "Maximum compression for fast messaging sharing"),
                                        Triple("SMALL_FILE", "📁 Small File Size", "Low resolution (480p) for storage saving"),
                                        Triple("BALANCED", "⚖️ Balanced (Recommended)", "HD 720p with great visual balance"),
                                        Triple("HIGH_QUALITY", "🎬 High Quality 1080p", "Full HD resolution with maximum clarity")
                                    )
                                } else {
                                    listOf(
                                        Triple("WHATSAPP", "📱 ارسال پیام‌رسان (واتساپ / ایتا)", "حداکثر فشرده‌سازی جهت ارسال سریع"),
                                        Triple("SMALL_FILE", "📁 فایل فوق‌العاده کوچک", "رزولوشن ۴۸۰p جهت بیشترین آزادسازی حافظه"),
                                        Triple("BALANCED", "⚖️ متعادل و استاندارد (پیشنهادی)", "رزولوشن HD ۷۲۰p با تعادل عالی تصویر و حجم"),
                                        Triple("HIGH_QUALITY", "🎬 کیفیت بالا (1080p)", "کیفیت و رزولوشن اصلی با شفافیت بالاو کاهش حجم")
                                    )
                                }

                                presets.forEach { (type, title, desc) ->
                                    val isSelected = presetType == type
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) RoyalBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { presetType = type }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = desc,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Advanced Custom Control Content
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Target Resolution Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = if (isEn) "Output Resolution:" else "رزولوشن تصویر خروجی:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("1080p", "720p", "480p", "360p", if (isEn) "Original" else "اصلی").forEach { res ->
                                            val key = if (res == "Original" || res == "اصلی") "ORIGINAL" else res
                                            FilterChip(
                                                selected = targetResolution == key,
                                                onClick = { targetResolution = key },
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

                            // Bitrate Slider Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isEn) "Video Bitrate:" else "نرخ بیت (Bitrate):",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${customBitrateKbps.toInt()} Kbps (${String.format("%.1f", customBitrateKbps / 1000f)} Mbps)",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = RoyalBlue
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Slider(
                                        value = customBitrateKbps,
                                        onValueChange = { customBitrateKbps = it },
                                        valueRange = 500f..8000f,
                                        steps = 14,
                                        colors = SliderDefaults.colors(
                                            thumbColor = RoyalBlue,
                                            activeTrackColor = RoyalBlue
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("500 Kbps (${if (isEn) "Low" else "کم"})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("8000 Kbps (${if (isEn) "High" else "زیاد"})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            // FPS & Format Options
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = if (isEn) "Frame Rate (FPS):" else "نرخ فریم (FPS):",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(60, 30, 24).forEach { fpsVal ->
                                            FilterChip(
                                                selected = customFps == fpsVal,
                                                onClick = { customFps = fpsVal },
                                                label = { Text("$fpsVal FPS", fontWeight = FontWeight.Bold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = RoyalBlue,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = if (isEn) "Output Format:" else "فرمت خروجی:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("MP4", "MKV", "WEBM").forEach { fmt ->
                                            FilterChip(
                                                selected = containerFormat == fmt,
                                                onClick = { containerFormat = fmt },
                                                label = { Text(fmt, fontWeight = FontWeight.Bold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = RoyalBlue,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Audio Option Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (muteAudio) Icons.Default.MusicOff else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (muteAudio) Color(0xFFEF4444) else RoyalBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isEn) "Remove Audio Track" else "حذف صدای ویدیو (Mute)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isEn) "Strip audio for extra size reduction" else "حذف صدا برای کاهش فوق‌العاده حجم فایل",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = muteAudio,
                                onCheckedChange = { muteAudio = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RoyalBlue)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Start Compression Button
                    Button(
                        onClick = {
                            val uri = selectedVideoUri ?: return@Button
                            val modeStr = if (selectedModeTab == 0) "PRESET" else "CUSTOM"
                            val config = VideoCompressConfig(
                                mode = modeStr,
                                presetType = presetType,
                                targetResolution = targetResolution,
                                customBitrateKbps = customBitrateKbps.toInt(),
                                fps = customFps,
                                muteAudio = muteAudio,
                                containerFormat = containerFormat,
                                trimEnabled = isTrimEnabled,
                                trimStartMs = (trimStartSec * 1000f).toLong(),
                                trimEndMs = (trimEndSec * 1000f).toLong(),
                                engineMode = engineMode
                            )
                            onCompressVideo(uri, config)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
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
                            text = if (isEn) "Start Video Compression" else "شروع فشرده‌سازی ویدیو",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}

private fun formatSec(secFloat: Float): String {
    val totalSec = secFloat.toLong()
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}

