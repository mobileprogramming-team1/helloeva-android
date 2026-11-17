package com.helloeva.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.helloeva.app.R

/** Pretendard (SIL Open Font License 1.1). */
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_black, FontWeight.Black),
)

private fun style(size: Int, weight: FontWeight, lineHeight: Int = (size * 1.45f).toInt()) = TextStyle(
    fontFamily = Pretendard,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
)

val EvaTypography = Typography(
    displaySmall = style(36, FontWeight.Black),          // "Hello Eva" on login
    headlineLarge = style(28, FontWeight.Black),         // Settings title
    headlineMedium = style(24, FontWeight.Black, 34),    // Screen titles (survey, onboarding)
    headlineSmall = style(22, FontWeight.Black),         // Banner title
    titleLarge = style(18, FontWeight.Bold),             // Section titles
    titleMedium = style(16, FontWeight.Bold),            // Card titles, list titles
    titleSmall = style(14, FontWeight.Bold),             // Card section titles
    bodyLarge = style(15, FontWeight.Normal, 22),
    bodyMedium = style(14, FontWeight.Normal, 21),
    bodySmall = style(13, FontWeight.Normal, 20),
    labelLarge = style(17, FontWeight.Bold),             // Primary buttons
    labelMedium = style(13, FontWeight.Medium),
    labelSmall = style(12, FontWeight.Normal),
)
