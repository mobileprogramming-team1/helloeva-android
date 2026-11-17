package com.helloeva.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helloeva.app.ui.theme.EvaColors
import com.helloeva.app.ui.theme.HelloEvaTheme

/** Thin blue progress bar under the top bar (survey steps, question n/15). */
@Composable
fun StepProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "progress")
    Box(
        modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(EvaColors.Gray200, RoundedCornerShape(3.dp)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(6.dp)
                .background(EvaColors.Blue, RoundedCornerShape(3.dp)),
        )
    }
}

/** One row of 영역별 평가: label, bar, percentage. */
@Composable
fun ScoreBar(label: String, score: Int, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.width(60.dp), style = MaterialTheme.typography.bodySmall, color = EvaColors.Gray700)
        Box(
            Modifier
                .weight(1f)
                .height(8.dp)
                .background(EvaColors.Gray200, RoundedCornerShape(4.dp)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(score.coerceIn(0, 100) / 100f)
                    .height(8.dp)
                    .background(EvaColors.Blue, RoundedCornerShape(4.dp)),
            )
        }
        Text(
            "$score%",
            Modifier.width(42.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = EvaColors.Blue,
            textAlign = TextAlign.End,
        )
    }
}

/** "──── 문제 1 / 15 ────" divider in the chat. */
@Composable
fun QuestionDivider(text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(1.dp).background(EvaColors.Gray200))
        Text(text, Modifier.padding(horizontal = 10.dp), style = MaterialTheme.typography.labelSmall, color = EvaColors.Gray500)
        Box(Modifier.weight(1f).height(1.dp).background(EvaColors.Gray200))
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressPreview() {
    HelloEvaTheme {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StepProgressBar(0.5f)
            ScoreBar("유창성", 60)
            ScoreBar("내용 구성", 50)
            QuestionDivider("문제 1 / 15")
        }
    }
}
