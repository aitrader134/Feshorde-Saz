package com.kafappstore.feshorde.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kafappstore.feshorde.data.db.CompressedFileEntity
import com.kafappstore.feshorde.data.engine.StorageStatsManager
import com.kafappstore.feshorde.ui.theme.AmberToolBg
import com.kafappstore.feshorde.ui.theme.AmberToolIcon
import com.kafappstore.feshorde.ui.theme.GreenToolBg
import com.kafappstore.feshorde.ui.theme.GreenToolIcon
import com.kafappstore.feshorde.ui.theme.OrangeToolBg
import com.kafappstore.feshorde.ui.theme.OrangeToolIcon
import com.kafappstore.feshorde.ui.theme.PurpleToolBg
import com.kafappstore.feshorde.ui.theme.PurpleToolIcon
import com.kafappstore.feshorde.ui.theme.RoyalBlue
import com.kafappstore.feshorde.ui.theme.RoyalBlueContainer
import com.kafappstore.feshorde.ui.theme.RoyalBlueDark
import androidx.compose.runtime.collectAsState
import com.kafappstore.feshorde.data.AppLanguage
import com.kafappstore.feshorde.data.AppStrings
import com.kafappstore.feshorde.data.LanguageManager
import com.kafappstore.feshorde.ui.theme.RoyalBlueLight
import java.io.File

import com.kafappstore.feshorde.data.LocalAppLanguage

@Composable
fun PersianRtlLayout(content: @Composable () -> Unit) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val direction = if (currentLang == AppLanguage.EN) LayoutDirection.Ltr else LayoutDirection.Rtl
    CompositionLocalProvider(
        LocalLayoutDirection provides direction,
        LocalAppLanguage provides currentLang
    ) {
        content()
    }
}

@Composable
fun AppHeader(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentLang by LanguageManager.currentLanguage.collectAsState()

    PersianRtlLayout {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("btn_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = AppStrings.getString("back"),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = AppStrings.getString("app_subtitle"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick Language Toggle Chip Button (High contrast, highly visible)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RoyalBlue.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            val nextLang = if (currentLang == AppLanguage.FA) AppLanguage.EN else AppLanguage.FA
                            LanguageManager.setLanguage(context, nextLang)
                        }
                        .testTag("btn_toggle_header_language")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.FA) "🌐 English" else "🌐 فارسی",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = RoyalBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    totalSavedText: String,
    totalFilesCount: Int,
    usedStoragePercent: Int
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()

    PersianRtlLayout {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = RoyalBlue
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                RoyalBlueLight,
                                RoyalBlue,
                                RoyalBlueDark
                            )
                        )
                    )
                    .padding(22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = AppStrings.getString("storage_saved"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = totalSavedText,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 32.sp
                                ),
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = LanguageManager.formatNumber(totalFilesCount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = AppStrings.getString("optimized_files"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${AppStrings.getString("total_storage_used")}: ${LanguageManager.formatNumber(usedStoragePercent)}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (usedStoragePercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                }
            }
        }
    }
}

@Composable
fun GridToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerBg: Color,
    iconColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PersianRtlLayout {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .testTag(testTag),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(containerBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun QuickToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    GridToolCard(
        title = title,
        subtitle = subtitle,
        icon = icon,
        containerBg = iconColor.copy(alpha = 0.15f),
        iconColor = iconColor,
        testTag = testTag,
        onClick = onClick
    )
}

@Composable
fun CompressProgressDialog(
    percentage: Float,
    message: String
) {
    PersianRtlLayout {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        color = RoyalBlue,
                        strokeWidth = 5.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (message == "در حال فشرده‌سازی...") AppStrings.getString("compressing") else message,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { percentage.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = RoyalBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${LanguageManager.formatNumber((percentage * 100).toInt())}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = RoyalBlue
                    )
                }
            }
        }
    }
}

@Composable
fun SuccessCompressDialog(
    fileEntity: CompressedFileEntity,
    localFile: File,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    PersianRtlLayout {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(GreenToolBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = AppStrings.getString("success"),
                            tint = GreenToolIcon,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = AppStrings.getString("success"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(GreenToolBg)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${LanguageManager.formatNumber(fileEntity.savedPercentage)}% ${AppStrings.getString("saved_amount")}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = GreenToolIcon
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = AppStrings.getString("original_size") + ":",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = LanguageManager.formatBytes(fileEntity.originalSizeBytes),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = AppStrings.getString("compressed_size") + ":",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = LanguageManager.formatBytes(fileEntity.compressedSizeBytes),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GreenToolIcon
                                )
                            }
                            if (fileEntity.detailsInfo.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = fileEntity.detailsInfo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "محل ذخیره: ${localFile.parentFile?.name ?: "حافظه عمومی"}/${localFile.name}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = RoyalBlue,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                StorageStatsManager.shareFile(context, localFile)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_share"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppStrings.getString("share_file"))
                        }

                        Button(
                            onClick = {
                                StorageStatsManager.openFile(context, localFile)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_open"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppStrings.getString("open_file"), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_done"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(AppStrings.getString("close"), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
