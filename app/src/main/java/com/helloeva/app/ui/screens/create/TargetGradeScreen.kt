package com.helloeva.app.ui.screens.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helloeva.app.data.model.Grade
import com.helloeva.app.ui.components.EvaTopBar
import com.helloeva.app.ui.components.PrimaryButton
import com.helloeva.app.ui.components.SelectRow
import com.helloeva.app.ui.components.StepCounter
import com.helloeva.app.ui.components.StepProgressBar
import com.helloeva.app.ui.theme.EvaColors

/** 대화 생성 1/2: target grade (Figma: 대화 생성 - 1). */
@Composable
fun TargetGradeScreen(
    viewModel: CreateConversationViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 32.dp)) {
        EvaTopBar(onBack = onBack, trailing = { StepCounter(1, 2) })
        Spacer(Modifier.height(8.dp))
        StepProgressBar(0.5f)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(22.dp))
            Text("대화 생성", style = MaterialTheme.typography.labelMedium, color = EvaColors.Blue)
            Text("목표 등급을 선택해 주세요", style = MaterialTheme.typography.headlineMedium)
            Text("1개 선택", style = MaterialTheme.typography.bodySmall, color = EvaColors.Gray500)
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Grade.entries.forEach { grade ->
                    val selected = state.targetGrade == grade
                    SelectRow(
                        text = grade.label,
                        selected = selected,
                        onClick = { viewModel.selectGrade(grade) },
                        leading = { GradeCode(grade, selected) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        PrimaryButton("다음", onClick = onNext, enabled = state.targetGrade != null)
    }
}

@Composable
private fun GradeCode(grade: Grade, selected: Boolean) {
    Box(
        Modifier
            .defaultMinSize(minWidth = 40.dp)
            .height(26.dp)
            .background(if (selected) EvaColors.Blue else EvaColors.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            grade.name,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
            color = if (selected) EvaColors.White else EvaColors.Ink,
        )
    }
}
