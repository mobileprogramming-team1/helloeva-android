package com.helloeva.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.helloeva.app.ui.theme.EvaColors

/** The Eva mascot, drawn on a 200×200 grid and scaled to the given size. */
@Composable
fun EvaRobot(
    modifier: Modifier = Modifier,
    bodyColor: Color = EvaColors.Blue,
    accentColor: Color = EvaColors.Lime,
) {
    Canvas(modifier) {
        val s = size.minDimension / 200f
        fun p(x: Float, y: Float) = Offset(x * s, y * s)
        fun sz(w: Float, h: Float) = Size(w * s, h * s)
        fun r(v: Float) = CornerRadius(v * s)
        val ink = EvaColors.Ink

        // antenna
        drawLine(ink, p(100f, 34f), p(100f, 54f), strokeWidth = 5f * s, cap = StrokeCap.Round)
        drawCircle(accentColor, radius = 9f * s, center = p(100f, 28f))
        drawCircle(ink, radius = 9f * s, center = p(100f, 28f), style = Stroke(width = 4f * s))
        // ears
        drawRoundRect(ink, topLeft = p(28f, 88f), size = sz(18f, 40f), cornerRadius = r(9f))
        drawRoundRect(ink, topLeft = p(154f, 88f), size = sz(18f, 40f), cornerRadius = r(9f))
        // head + face
        drawRoundRect(bodyColor, topLeft = p(40f, 52f), size = sz(120f, 98f), cornerRadius = r(36f))
        drawRoundRect(Color.White, topLeft = p(56f, 70f), size = sz(88f, 60f), cornerRadius = r(24f))
        drawCircle(ink, radius = 7f * s, center = p(82f, 96f))
        drawCircle(ink, radius = 7f * s, center = p(118f, 96f))
        val smile = Path().apply {
            moveTo(86f * s, 110f * s)
            quadraticTo(100f * s, 122f * s, 114f * s, 110f * s)
        }
        drawPath(smile, ink, style = Stroke(width = 5f * s, cap = StrokeCap.Round))
        // waveform badge
        drawRoundRect(ink, topLeft = p(68f, 156f), size = sz(64f, 28f), cornerRadius = r(14f))
        listOf(82f to 10f, 90f to 18f, 98f to 12f, 106f to 20f, 114f to 10f).forEach { (x, h) ->
            drawRoundRect(accentColor, topLeft = p(x, 170f - h / 2), size = sz(4f, h), cornerRadius = r(2f))
        }
    }
}

/** Small round Eva avatar used next to chat bubbles. */
@Composable
fun EvaAvatar(modifier: Modifier = Modifier, size: Dp = 34.dp) {
    Box(
        modifier = modifier.size(size).background(EvaColors.Blue, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(size * 0.18f)) {
            repeat(2) { Box(Modifier.size(size * 0.15f).background(Color.White, CircleShape)) }
        }
    }
}

/** App logo: blue rounded square with a lime waveform. */
@Composable
fun AppLogo(modifier: Modifier = Modifier, size: Dp = 30.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(EvaColors.Blue, RoundedCornerShape(size * 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(size * 0.07f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(0.27f, 0.47f, 0.6f, 0.4f, 0.23f).forEach { h ->
                Box(
                    Modifier
                        .width(size * 0.1f)
                        .height(size * h)
                        .background(EvaColors.Lime, RoundedCornerShape(50)),
                )
            }
        }
    }
}

@Preview
@Composable
private fun EvaRobotPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        EvaRobot(Modifier.size(120.dp))
        EvaRobot(Modifier.size(120.dp), bodyColor = EvaColors.Lime)
        EvaAvatar()
        AppLogo()
    }
}
