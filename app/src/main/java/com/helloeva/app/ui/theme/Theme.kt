package com.helloeva.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val EvaColorScheme = lightColorScheme(
    primary = EvaColors.Blue,
    onPrimary = EvaColors.White,
    primaryContainer = EvaColors.BlueSoft,
    onPrimaryContainer = EvaColors.Blue,
    secondary = EvaColors.Lime,
    onSecondary = EvaColors.Ink,
    background = EvaColors.White,
    onBackground = EvaColors.Ink,
    surface = EvaColors.White,
    onSurface = EvaColors.Ink,
    surfaceVariant = EvaColors.Gray50,
    onSurfaceVariant = EvaColors.Gray500,
    outline = EvaColors.Gray200,
    error = EvaColors.Danger,
)

val EvaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HelloEvaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EvaColorScheme,
        typography = EvaTypography,
        shapes = EvaShapes,
        content = content,
    )
}
