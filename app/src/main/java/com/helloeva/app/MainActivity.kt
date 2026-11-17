package com.helloeva.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.helloeva.app.ui.navigation.HelloEvaNavHost
import com.helloeva.app.ui.theme.EvaColors
import com.helloeva.app.ui.theme.HelloEvaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelloEvaTheme {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(EvaColors.White)
                        .safeDrawingPadding(),
                ) {
                    HelloEvaNavHost()
                }
            }
        }
    }
}
