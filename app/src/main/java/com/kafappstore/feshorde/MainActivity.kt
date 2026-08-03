package com.kafappstore.feshorde

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kafappstore.feshorde.ui.navigation.AppNavigation
import com.kafappstore.feshorde.ui.theme.CompressorTheme
import com.kafappstore.feshorde.ui.viewmodel.CompressorViewModel

class MainActivity : ComponentActivity() {
    private val compressorViewModel: CompressorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompressorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = compressorViewModel)
                }
            }
        }
    }
}
