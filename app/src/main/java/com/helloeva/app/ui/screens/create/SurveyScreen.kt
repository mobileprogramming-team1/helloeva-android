package com.helloeva.app.ui.screens.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helloeva.app.data.model.SURVEY_MIN_TOTAL
import com.helloeva.app.ui.components.EvaTopBar
import com.helloeva.app.ui.components.PillChip
import com.helloeva.app.ui.components.PrimaryButton
import com.helloeva.app.ui.components.SelectRow
import com.helloeva.app.ui.components.StepCounter
import com.helloeva.app.ui.components.StepProgressBar
import com.helloeva.app.ui.theme.EvaColors

/** 대화 생성 2/2: background survey, 8 pages (Figma: 대화 생성 - 2). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SurveyScreen(
    viewModel: CreateConversationViewModel,
    onExit: () -> Unit,
    onCreated: (conversationId: Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val page = state.page
    val back = { if (!viewModel.previousPage()) onExit() }
    BackHandler(onBack = back)

    val scroll = rememberScrollState()
    LaunchedEffect(state.pageIndex) { scroll.scrollTo(0) }

    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 32.dp)) {
        EvaTopBar(onBack = back, trailing = { StepCounter(state.pageIndex + 1, state.pages.size) })
        Spacer(Modifier.height(8.dp))
        StepProgressBar((state.pageIndex + 1f) / state.pages.size)

        Column(Modifier.weight(1f).verticalScroll(scroll)) {
            Spacer(Modifier.height(22.dp))
            Text("배경 설문 · ${page.category}", style = MaterialTheme.typography.labelMedium, color = EvaColors.Blue)
            Text(page.question, style = MaterialTheme.typography.headlineMedium)
            Text(
                if (page.multiSelect) "복수 선택 · 최소 ${page.minSelect}개" else "1개 선택",
                style = MaterialTheme.typography.bodySmall,
                color = EvaColors.Gray500,
            )
            Spacer(Modifier.height(20.dp))

            if (page.multiSelect) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    page.options.forEach { option ->
                        PillChip(option, selected = option in state.selected, onClick = { viewModel.toggle(option) })
                    }
                }
                if (page.exclusiveOption != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "'${page.exclusiveOption}'을 고르면 다른 항목은 해제돼요",
                        style = MaterialTheme.typography.labelSmall,
                        color = EvaColors.Gray500,
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    page.options.forEach { option ->
                        SelectRow(option, selected = option in state.selected, onClick = { viewModel.toggle(option) })
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (page.multiSelect) {
            val short = state.isLastPage && !state.totalValid
            if (short) {
                Text(
                    "전체 ${SURVEY_MIN_TOTAL}개 이상 선택해야 완료할 수 있어요",
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = EvaColors.Danger,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(if (short) EvaColors.DangerSoft else EvaColors.BlueSoft, RoundedCornerShape(14.dp))
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "이 문항 ${state.selected.size}개 선택 (최소 ${page.minSelect}개)",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = EvaColors.Gray700,
                )
                Text(
                    "누적 ${state.total} / $SURVEY_MIN_TOTAL",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                    color = if (short) EvaColors.Danger else EvaColors.Blue,
                )
            }
        }

        PrimaryButton(
            text = if (state.isLastPage) "완료" else "다음",
            onClick = { viewModel.nextPage(onCreated) },
            enabled = state.canProceed,
        )
    }
}
