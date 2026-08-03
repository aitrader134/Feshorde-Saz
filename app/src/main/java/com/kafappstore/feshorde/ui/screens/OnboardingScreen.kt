package com.kafappstore.feshorde.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kafappstore.feshorde.ui.components.PersianRtlLayout
import com.kafappstore.feshorde.ui.theme.AmberToolBg
import com.kafappstore.feshorde.ui.theme.AmberToolIcon
import com.kafappstore.feshorde.ui.theme.GreenToolBg
import com.kafappstore.feshorde.ui.theme.GreenToolIcon
import com.kafappstore.feshorde.ui.theme.PurpleToolBg
import com.kafappstore.feshorde.ui.theme.PurpleToolIcon
import com.kafappstore.feshorde.ui.theme.RoyalBlue

import com.kafappstore.feshorde.data.AppStrings
import com.kafappstore.feshorde.data.LanguageManager

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val containerBg: Color,
    val iconColor: Color,
    val badgeText: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val context = LocalContext.current
    var currentPageIndex by remember { mutableIntStateOf(0) }
    val isEn = LanguageManager.isEnglish()

    val pages = if (isEn) {
        listOf(
            OnboardingPageData(
                title = "Smart Photo & Video Compression",
                description = "Reduce image (JPG, PNG, WebP) and video (MP4, MKV) size up to 90% without quality loss.",
                icon = Icons.Default.PermMedia,
                containerBg = PurpleToolBg,
                iconColor = PurpleToolIcon,
                badgeText = "Gallery & Media"
            ),
            OnboardingPageData(
                title = "Audio Optimization & ZIP Archives",
                description = "Adjust bitrate and create compressed ZIP archives or extract files easily.",
                icon = Icons.Default.FolderZip,
                containerBg = AmberToolBg,
                iconColor = AmberToolIcon,
                badgeText = "Audio & ZIP"
            ),
            OnboardingPageData(
                title = "Storage Stats & File Management",
                description = "Track storage saved, access recent compressed files, and share instantly.",
                icon = Icons.Default.Memory,
                containerBg = GreenToolBg,
                iconColor = GreenToolIcon,
                badgeText = "Stats & History"
            )
        )
    } else {
        listOf(
            OnboardingPageData(
                title = "فشرده‌سازی هوشمند عکس و ویدیو",
                description = "کاهش حجم تصاویر (JPG, PNG, WebP) و ویدیوها (MP4, MKV) تا ۹۰٪ بدون افت کیفیت محسوس با کنترل دقیق کیفیت و رزولوشن.",
                icon = Icons.Default.PermMedia,
                containerBg = PurpleToolBg,
                iconColor = PurpleToolIcon,
                badgeText = "گالری و رسانه"
            ),
            OnboardingPageData(
                title = "بهینه‌سازی صوت و مدیریت ZIP",
                description = "تنظیم بیت‌ریت و تبدیل فایل‌های صوتی به حالت مونو، همراه با امکان ساخت آرشیو ZIP فشرده و استخراج آسان فایل‌ها.",
                icon = Icons.Default.FolderZip,
                containerBg = AmberToolBg,
                iconColor = AmberToolIcon,
                badgeText = "موزیک و فشرده‌سازی ZIP"
            ),
            OnboardingPageData(
                title = "آمار صرفه‌جویی و مدیریت فایل",
                description = "مشاهده میزان دقیق حافظه آزاد شده، دسترسی سریع به فایل‌های فشرده شده، و اشتراک‌گذاری یا باز کردن مستقیم فایل‌ها.",
                icon = Icons.Default.Memory,
                containerBg = GreenToolBg,
                iconColor = GreenToolIcon,
                badgeText = "آمار و تاریخچه"
            )
        )
    }

    fun completeOnboarding() {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("has_seen_onboarding", true).apply()
        onFinishOnboarding()
    }

    PersianRtlLayout {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .testTag("screen_onboarding"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Skip Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Guide (${currentPageIndex + 1} of ${pages.size})" else "راهنمای برنامه (${LanguageManager.formatNumber(currentPageIndex + 1)} از ${LanguageManager.formatNumber(pages.size)})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (currentPageIndex < pages.size - 1) {
                        TextButton(
                            onClick = { completeOnboarding() },
                            modifier = Modifier.testTag("btn_skip_onboarding")
                        ) {
                            Text(
                                text = AppStrings.getString("skip"),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                }

                // Middle Content Area with Animated Swap
                AnimatedContent(
                    targetState = currentPageIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn(tween(300))).with(
                                slideOutHorizontally { width -> -width } + fadeOut(tween(300))
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn(tween(300))).with(
                                slideOutHorizontally { width -> width } + fadeOut(tween(300))
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = "onboarding_slide"
                ) { pageIndex ->
                    val page = pages[pageIndex]

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(page.containerBg)
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = page.badgeText,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = page.iconColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Main Hero Icon
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(page.containerBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = page.icon,
                                        contentDescription = null,
                                        tint = page.iconColor,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(28.dp))

                                Text(
                                    text = page.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 21.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = page.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 24.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Bottom Page Indicators & Controls
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Page Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        pages.indices.forEach { index ->
                            val isSelected = index == currentPageIndex
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(8.dp)
                                    .width(if (isSelected) 24.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) RoyalBlue
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }

                    // Navigation Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentPageIndex > 0) {
                            OutlinedButton(
                                onClick = { currentPageIndex-- },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = AppStrings.getString("previous"),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AppStrings.getString("previous"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (currentPageIndex < pages.size - 1) {
                                    currentPageIndex++
                                } else {
                                    completeOnboarding()
                                }
                            },
                            modifier = Modifier
                                .weight(2f)
                                .height(52.dp)
                                .testTag("btn_next_onboarding"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Text(
                                text = if (currentPageIndex == pages.size - 1) AppStrings.getString("start_app") else AppStrings.getString("next"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (currentPageIndex == pages.size - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
