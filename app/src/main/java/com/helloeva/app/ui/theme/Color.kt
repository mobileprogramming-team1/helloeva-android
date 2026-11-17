package com.helloeva.app.ui.theme

import androidx.compose.ui.graphics.Color

/** Hello Eva palette. Brand = Lime #CDF22B × Blue #1E45FB. */
object EvaColors {
    val Blue = Color(0xFF1E45FB)
    val BlueDark = Color(0xFF1535C8)
    val BlueMid = Color(0xFF3D5FFF)
    val BlueSoft = Color(0xFFE8EDFF)
    val BlueOnDark = Color(0xFFE4E9FF)

    val Lime = Color(0xFFCDF22B)

    val Ink = Color(0xFF141414)
    val Gray900 = Color(0xFF2A2E35)
    val Gray700 = Color(0xFF3A3F47)
    val Gray600 = Color(0xFF555B66)
    val Gray500 = Color(0xFF5F6672)
    val Gray400 = Color(0xFF8A909A)
    val Gray300 = Color(0xFFC4C8D0)
    val Gray200 = Color(0xFFE8EAEE)
    val Gray100 = Color(0xFFF2F3F5)
    val Gray50 = Color(0xFFF5F6F8)
    val White = Color(0xFFFFFFFF)

    /** Recording state only. */
    val Record = Color(0xFFE5383B)
    val RecordSoft = Color(0xFFFDE4E4)
    val Danger = Color(0xFFC42B2B)
    val DangerSoft = Color(0xFFFDECEC)

    /** Kakao brand rules: keep exactly these. */
    val Kakao = Color(0xFFFEE500)
    val KakaoText = Color(0xFF191919)

    /**
     * Main button (CTA) colors. Team decision pending:
     *  - Blue + White (current)
     *  - Lime + Ink  → set CtaContainer = Lime, CtaContent = Ink
     */
    val CtaContainer = Blue
    val CtaContent = White
}
