package com.kafappstore.feshorde.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kafappstore.feshorde.ui.components.CompressProgressDialog
import com.kafappstore.feshorde.ui.components.SuccessCompressDialog
import com.kafappstore.feshorde.ui.screens.AudioCompressorScreen
import com.kafappstore.feshorde.ui.screens.DashboardScreen
import com.kafappstore.feshorde.ui.screens.HistoryScreen
import com.kafappstore.feshorde.ui.screens.ImageCompressorScreen
import com.kafappstore.feshorde.ui.screens.OnboardingScreen
import com.kafappstore.feshorde.ui.screens.SplashScreen
import com.kafappstore.feshorde.ui.screens.VideoCompressorScreen
import com.kafappstore.feshorde.ui.screens.ZipCompressorScreen
import com.kafappstore.feshorde.ui.viewmodel.CompressionUiState
import com.kafappstore.feshorde.ui.viewmodel.CompressorViewModel

object NavRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val IMAGE = "image"
    const val VIDEO = "video"
    const val AUDIO = "audio"
    const val ZIP = "zip"
    const val HISTORY = "history"
}

@Composable
fun AppNavigation(
    viewModel: CompressorViewModel,
    navController: NavHostController = rememberNavController()
) {
    val historyFiles by viewModel.historyFiles.collectAsStateWithLifecycle()
    val totalBytesSaved by viewModel.totalBytesSaved.collectAsStateWithLifecycle()
    val totalFilesCount by viewModel.totalFilesCount.collectAsStateWithLifecycle()
    val compressionState by viewModel.compressionState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onNavigateNext = { isFirstTime ->
                    val targetRoute = if (isFirstTime) NavRoutes.ONBOARDING else NavRoutes.DASHBOARD
                    navController.navigate(targetRoute) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.DASHBOARD) {
            DashboardScreen(
                totalBytesSaved = totalBytesSaved ?: 0L,
                totalFilesCount = totalFilesCount,
                recentFiles = historyFiles,
                onNavigateToImage = { navController.navigate(NavRoutes.IMAGE) },
                onNavigateToVideo = { navController.navigate(NavRoutes.VIDEO) },
                onNavigateToAudio = { navController.navigate(NavRoutes.AUDIO) },
                onNavigateToZip = { navController.navigate(NavRoutes.ZIP) },
                onNavigateToHistory = { navController.navigate(NavRoutes.HISTORY) },
                onDeleteHistoryFile = { entity -> viewModel.deleteHistoryFile(entity) }
            )
        }

        composable(NavRoutes.IMAGE) {
            ImageCompressorScreen(
                onBackClick = { navController.popBackStack() },
                onCompressImage = { uri, config ->
                    viewModel.compressImage(uri, config)
                }
            )
        }

        composable(NavRoutes.VIDEO) {
            VideoCompressorScreen(
                onBackClick = { navController.popBackStack() },
                onCompressVideo = { uri, config ->
                    viewModel.compressVideo(uri, config)
                }
            )
        }

        composable(NavRoutes.AUDIO) {
            AudioCompressorScreen(
                onBackClick = { navController.popBackStack() },
                onCompressAudio = { uri, config ->
                    viewModel.compressAudio(uri, config)
                }
            )
        }

        composable(NavRoutes.ZIP) {
            ZipCompressorScreen(
                onBackClick = { navController.popBackStack() },
                onCreateZip = { files, config ->
                    viewModel.createZip(files, config)
                },
                onExtractZip = { uri ->
                    viewModel.extractZip(uri)
                }
            )
        }

        composable(NavRoutes.HISTORY) {
            HistoryScreen(
                historyFiles = historyFiles,
                onBackClick = { navController.popBackStack() },
                onDeleteFile = { entity -> viewModel.deleteHistoryFile(entity) },
                onClearAll = { viewModel.clearHistory() }
            )
        }
    }

    // Global Modal Overlay for Progress & Success
    when (val state = compressionState) {
        is CompressionUiState.Progress -> {
            CompressProgressDialog(
                percentage = state.percentage,
                message = state.message
            )
        }
        is CompressionUiState.Success -> {
            SuccessCompressDialog(
                fileEntity = state.fileEntity,
                localFile = state.localFile,
                onDismiss = { viewModel.resetCompressionState() }
            )
        }
        else -> {}
    }
}
