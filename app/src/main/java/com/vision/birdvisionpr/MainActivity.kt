package com.vision.birdvisionpr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vision.birdvisionpr.ui.navigation.AppNavigation
import com.vision.birdvisionpr.ui.theme.BirdVisionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BirdVisionTheme {
                AppNavigation()
            }
        }
    }
}
