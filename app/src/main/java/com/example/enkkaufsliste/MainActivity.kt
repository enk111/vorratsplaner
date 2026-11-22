package com.example.enkkaufsliste

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private val viewModel: ItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF1F6F4A),
                    onPrimary = Color.White,
                    background = Color(0xFFEEF2EF),
                    onBackground = Color(0xFF18201B),
                    surface = Color.White,
                    onSurface = Color(0xFF18201B),
                    onSurfaceVariant = Color(0xFF68746C),
                    secondaryContainer = Color(0xFFE3EFE8),
                    onSecondaryContainer = Color(0xFF18201B),
                    outlineVariant = Color(0xFFDDE4DF)
                )
            ) {
                ShoppingScreen(viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startAutoSync()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopAutoSync()
    }
}