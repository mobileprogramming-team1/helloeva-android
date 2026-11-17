package com.helloeva.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helloeva.app.data.model.Grade
import com.helloeva.app.ui.theme.EvaColors
import com.helloeva.app.ui.theme.HelloEvaTheme

/** Expected grade = filled lime, target grade = outlined. [onDark] for blue backgrounds. */
@Composable
fun GradeChip(
    grade: Grade,
    filled: Boolean,
    modifier: Modifier = Modifier,
    onDark: Boolean = false,
) {
    val shape = RoundedCornerShape(12.dp)
    val outline = if (onDark) EvaColors.White else EvaColors.Blue
    Box(
        modifier = modifier
            .height(40.dp)
            .defaultMinSize(minWidth = 66.dp)
            .then(
                if (filled) Modifier.background(EvaColors.Lime, shape)
                else Modifier.border(2.dp, outline, shape),
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            grade.name,
            color = if (filled) EvaColors.Ink else outline,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 19.sp),
        )
    }
}

/** "예상 등급 → 목표 등급" row used in both feedback cards. */
@Composable
fun GradeComparison(
    expected: Grade,
    target: Grade,
    modifier: Modifier = Modifier,
    expectedLabel: String = "예상 등급",
    onDark: Boolean = false,
) {
    val labelColor = if (onDark) EvaColors.BlueOnDark else EvaColors.Gray500
    Row(modifier, verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(expectedLabel, style = MaterialTheme.typography.labelSmall, color = labelColor)
            GradeChip(expected, filled = true)
        }
        androidx.compose.material3.Icon(
            EvaIcons.ArrowForward,
            contentDescription = null,
            tint = if (onDark) EvaColors.BlueOnDark else EvaColors.Gray400,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("목표 등급", style = MaterialTheme.typography.labelSmall, color = labelColor)
            GradeChip(target, filled = false, onDark = onDark)
        }
    }
}

/** Light-blue keyword chip (주요 단어, 자주 쓴 표현). */
@Composable
fun KeywordChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(30.dp)
            .background(EvaColors.BlueSoft, CircleShape)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = EvaColors.Blue)
    }
}

/** Toggleable pill: home filters (dark when selected) and survey multi-select (blue when selected). */
@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = EvaColors.Blue,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = CircleShape,
        color = if (selected) selectedColor else EvaColors.Gray100,
        contentColor = if (selected) EvaColors.White else EvaColors.Gray900,
        border = null as BorderStroke?,
    ) {
        Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                ),
            )
        }
    }
}

/** Badge on the home list: "진행중" (lime) or the result grade (blue). */
@Composable
fun ConversationBadge(inProgress: Boolean, grade: Grade?) {
    if (inProgress) {
        Box(
            Modifier.height(30.dp).background(EvaColors.Lime, CircleShape).padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("진행중", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = EvaColors.Ink)
        }
    } else if (grade != null) {
        Box(
            Modifier
                .height(30.dp)
                .defaultMinSize(minWidth = 44.dp)
                .background(EvaColors.Blue, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(grade.name, style = MaterialTheme.typography.titleSmall, color = EvaColors.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChipsPreview() {
    HelloEvaTheme {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GradeComparison(Grade.IM2, Grade.AL)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeywordChip("lined with")
                KeywordChip("unwind")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PillChip("전체", selected = true, onClick = {}, selectedColor = EvaColors.Ink)
                PillChip("진행중", selected = false, onClick = {})
                PillChip("공원 가기", selected = true, onClick = {})
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ConversationBadge(inProgress = true, grade = null)
                ConversationBadge(inProgress = false, grade = Grade.IH)
            }
        }
    }
}
