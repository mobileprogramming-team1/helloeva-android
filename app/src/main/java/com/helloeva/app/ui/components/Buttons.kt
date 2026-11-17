package com.helloeva.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helloeva.app.ui.theme.EvaColors
import com.helloeva.app.ui.theme.HelloEvaTheme

/** Main call-to-action button (다음, 시작하기, 다음 문제…). */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = EvaColors.CtaContainer,
            contentColor = EvaColors.CtaContent,
            disabledContainerColor = EvaColors.Gray200,
            disabledContentColor = EvaColors.Gray500,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Disabled-looking status bar at the bottom of the chat ("Eva가 질문하고 있어요..."). */
@Composable
fun StatusBar(
    text: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(EvaColors.Gray100, CircleShape),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.bodyLarge, color = EvaColors.Gray700)
    }
}

/** Kakao login button. Colors follow Kakao's brand guide. */
@Composable
fun KakaoLoginButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EvaColors.Kakao,
            contentColor = EvaColors.KakaoText,
        ),
    ) {
        Icon(EvaIcons.KakaoTalk, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text("kakao로 시작하기", style = MaterialTheme.typography.titleMedium)
    }
}

/** Big round record button. [recording] switches mic (start) ↔ red stop. */
@Composable
fun RecordButton(
    recording: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(100.dp)
                .background(if (recording) EvaColors.RecordSoft else EvaColors.BlueSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                onClick = onClick,
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = if (recording) EvaColors.Record else EvaColors.CtaContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (recording) {
                        Box(Modifier.size(26.dp).background(EvaColors.White, RoundedCornerShape(6.dp)))
                    } else {
                        Icon(EvaIcons.Mic, contentDescription = label, tint = EvaColors.CtaContent, modifier = Modifier.size(34.dp))
                    }
                }
            }
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, color = EvaColors.Gray700)
    }
}

/** Top bar with a back chevron, optional back label, centered title and trailing slot. */
@Composable
fun EvaTopBar(
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    backLabel: String? = null,
    title: String? = null,
    trailing: @Composable () -> Unit = {},
) {
    Box(modifier.fillMaxWidth().height(44.dp)) {
        if (onBack != null) {
            if (backLabel != null) {
                TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 0.dp)) {
                    Icon(EvaIcons.ChevronLeft, contentDescription = null, tint = EvaColors.Ink)
                    Text(backLabel, style = MaterialTheme.typography.titleMedium, color = EvaColors.Ink)
                }
            } else {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).offset(x = (-12).dp)) {
                    Icon(EvaIcons.ChevronLeft, contentDescription = "뒤로", tint = EvaColors.Ink, modifier = Modifier.size(28.dp))
                }
            }
        }
        if (title != null) {
            Text(
                title,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium,
                color = EvaColors.Gray500,
            )
        }
        Box(Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)) { trailing() }
    }
}

/** "n / N" counter shown at the right of the top bar. */
@Composable
fun StepCounter(current: Int, total: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$current", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
        Text(" / $total", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = EvaColors.Gray400)
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonsPreview() {
    HelloEvaTheme {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton("다음", onClick = {})
            PrimaryButton("다음", onClick = {}, enabled = false)
            KakaoLoginButton(onClick = {})
            StatusBar("Eva가 질문하고 있어요...")
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                RecordButton(recording = false, onClick = {}, label = "눌러서 답변 시작")
                RecordButton(recording = true, onClick = {}, label = "눌러서 답변 완료")
            }
        }
    }
}
