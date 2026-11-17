package com.helloeva.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helloeva.app.ui.theme.EvaColors
import com.helloeva.app.ui.theme.HelloEvaTheme

private val EvaBubbleShape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
private val UserBubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomEnd = 20.dp, bottomStart = 20.dp)

/** Avatar + optional label + any Eva content (bubble, card). */
@Composable
fun EvaMessage(
    modifier: Modifier = Modifier,
    label: String? = null,
    content: @Composable () -> Unit,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EvaAvatar()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (label != null) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = EvaColors.Gray500)
            }
            content()
        }
    }
}

@Composable
fun EvaTextBubble(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .widthIn(max = 280.dp)
            .background(EvaColors.Gray100, EvaBubbleShape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = EvaColors.Ink)
    }
}

/** User's answer, right-aligned blue bubble. */
@Composable
fun UserTextMessage(text: String, modifier: Modifier = Modifier, label: String? = "나의 답변") {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (label != null) Text(label, style = MaterialTheme.typography.labelSmall, color = EvaColors.Gray500)
        Box(
            Modifier
                .widthIn(max = 280.dp)
                .background(EvaColors.Blue, UserBubbleShape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = EvaColors.White)
        }
    }
}

/** Default waveform shape for audio bubbles. */
val DefaultWaveform = listOf(8, 14, 20, 12, 22, 16, 24, 10, 18, 14, 20, 12, 16, 10, 14, 8)

@Composable
fun Waveform(
    bars: List<Int>,
    playedFraction: Float,
    playedColor: Color,
    idleColor: Color,
    modifier: Modifier = Modifier,
) {
    val playedCount = (bars.size * playedFraction).toInt()
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        bars.forEachIndexed { i, h ->
            Box(
                Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .background(if (i < playedCount) playedColor else idleColor, RoundedCornerShape(2.dp)),
            )
        }
    }
}

/**
 * Eva's audio-only question bubble.
 * [icon] = Play/Pause while asking, Replay when replays are left, null when disabled.
 */
@Composable
fun EvaAudioBubble(
    timeText: String,
    playedFraction: Float,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconDescription: String? = null,
    onIconClick: (() -> Unit)? = null,
    dimmed: Boolean = false,
) {
    Row(
        modifier
            .height(52.dp)
            .background(EvaColors.Gray100, EvaBubbleShape)
            .padding(start = 8.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (icon != null) {
            val filled = icon == EvaIcons.Pause || icon == EvaIcons.Play
            Surface(
                onClick = { onIconClick?.invoke() },
                enabled = onIconClick != null,
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = if (filled) EvaColors.Blue else EvaColors.White,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = iconDescription,
                        tint = when {
                            filled -> EvaColors.White
                            dimmed -> EvaColors.Gray400
                            else -> EvaColors.Blue
                        },
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        } else {
            Spacer(Modifier.width(6.dp))
        }
        Waveform(
            bars = DefaultWaveform,
            playedFraction = playedFraction,
            playedColor = EvaColors.Blue,
            idleColor = if (dimmed) EvaColors.Gray300 else EvaColors.Gray400,
        )
        Text(timeText, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = EvaColors.Gray700)
    }
}

/** User's recording bubble (red dot while recording). */
@Composable
fun UserAudioMessage(
    timeText: String,
    caption: String,
    recording: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier
                .height(52.dp)
                .background(EvaColors.Blue, UserBubbleShape)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (recording) Box(Modifier.size(10.dp).background(Color(0xFFFF5A5F), CircleShape))
            Waveform(
                bars = listOf(10, 18, 12, 24, 16, 28, 14, 20, 26, 12, 22, 16, 24, 10, 18, 14),
                playedFraction = 1f,
                playedColor = EvaColors.White,
                idleColor = EvaColors.White,
            )
            Text(timeText, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = EvaColors.White)
        }
        Text(caption, style = MaterialTheme.typography.labelSmall, color = EvaColors.Gray500)
    }
}

/** "Eva is thinking" dots. */
@Composable
fun TypingBubble(modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(48.dp)
            .background(EvaColors.Gray100, EvaBubbleShape)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        listOf(EvaColors.Blue, Color(0xFF6F88FC), Color(0xFFB5C3FD)).forEach {
            Box(Modifier.size(8.dp).background(it, CircleShape))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatBubblesPreview() {
    HelloEvaTheme {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            EvaMessage { EvaAudioBubble("0:07 / 0:15", 0.5f, icon = EvaIcons.Pause, onIconClick = {}) }
            EvaMessage { EvaAudioBubble("0:15", 0f, icon = EvaIcons.Replay, onIconClick = {}) }
            UserAudioMessage("00:42", "답변을 녹음하고 있어요 · 최대 3:00", recording = true)
            EvaMessage(label = "Eva의 질문") { EvaTextBubble("Tell me about a park you often visit.") }
            UserTextMessage("I often go to Hangang Park near my house.")
            EvaMessage { TypingBubble() }
        }
    }
}
