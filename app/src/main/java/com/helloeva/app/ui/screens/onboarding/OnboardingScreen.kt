package com.helloeva.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helloeva.app.data.model.Grade
import com.helloeva.app.ui.components.AppLogo
import com.helloeva.app.ui.components.EvaAudioBubble
import com.helloeva.app.ui.components.EvaMessage
import com.helloeva.app.ui.components.GradeComparison
import com.helloeva.app.ui.components.KeywordChip
import com.helloeva.app.ui.components.PillChip
import com.helloeva.app.ui.components.PrimaryButton
import com.helloeva.app.ui.components.ScoreBar
import com.helloeva.app.ui.components.UserAudioMessage
import com.helloeva.app.ui.theme.EvaColors
import com.helloeva.app.ui.theme.HelloEvaTheme
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val description: String,
    val illustration: @Composable () -> Unit,
)

private val pages = listOf(
    OnboardingPage("AI와 실전처럼 대화 연습", "오픽 시험처럼 Eva와 영어로 대화하며\n말하기를 연습해요.") { ChatIllustration() },
    OnboardingPage("목표 등급과 배경 설문 설정", "목표 등급과 배경 설문을 선택하면\n나에게 맞는 질문이 준비돼요.") { SetupIllustration() },
    OnboardingPage("대화가 끝나면 등급 확인", "대화 결과를 바탕으로 예상 등급과\n피드백을 확인해요.") { ResultIllustration() },
)

/** First-run onboarding, 3 pages (Figma: 온보딩 1/3–3/3). */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 32.dp)) {
        Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
            AppLogo(size = 26.dp)
            Spacer(Modifier.width(8.dp))
            Text("Hello Eva", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
            Spacer(Modifier.weight(1f))
            if (!isLast) {
                TextButton(onClick = onFinish) {
                    Text("건너뛰기", style = MaterialTheme.typography.bodyMedium, color = EvaColors.Gray500)
                }
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
            val page = pages[index]
            Column(Modifier.fillMaxSize().padding(top = 20.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .background(EvaColors.BlueSoft, RoundedCornerShape(28.dp))
                        .padding(22.dp),
                    contentAlignment = Alignment.Center,
                ) { page.illustration() }
                Spacer(Modifier.height(36.dp))
                Text(page.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(10.dp))
                Text(page.description, style = MaterialTheme.typography.bodyLarge, color = EvaColors.Gray600)
            }
        }

        Row(Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.Center) {
            repeat(pages.size) { i ->
                val active = i == pagerState.currentPage
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (active) 22.dp else 8.dp, height = 8.dp)
                        .background(if (active) EvaColors.Blue else EvaColors.Gray300, CircleShape),
                )
            }
        }
        PrimaryButton(
            text = if (isLast) "시작하기" else "다음",
            onClick = {
                if (isLast) onFinish()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
        )
    }
}

@Composable
private fun ChatIllustration() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        EvaMessage { EvaAudioBubble("0:15", 1f) }
        UserAudioMessage("00:42", caption = "", recording = false)
        EvaMessage { EvaAudioBubble("0:09", 0f) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetupIllustration() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(
            Modifier.fillMaxWidth().background(EvaColors.White, RoundedCornerShape(18.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("목표 등급", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = EvaColors.Gray500)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("IL", "IM1", "IM2", "IM3", "IH", "AL").forEach { PillChip(it, selected = it == "AL", onClick = {}) }
            }
        }
        Column(
            Modifier.fillMaxWidth().background(EvaColors.White, RoundedCornerShape(18.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("배경 설문", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = EvaColors.Gray500)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("직업" to true, "여가 활동" to true, "휴가·출장" to false).forEach { (t, s) -> PillChip(t, selected = s, onClick = {}) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultIllustration() {
    Column(
        Modifier.fillMaxWidth().background(EvaColors.White, RoundedCornerShape(18.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("답변 분석", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = EvaColors.Gray500)
        GradeComparison(Grade.IM2, Grade.AL)
        ScoreBar("유창성", 62)
        ScoreBar("어휘", 55)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeywordChip("unwind")
            KeywordChip("slow walk")
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingPreview() {
    HelloEvaTheme { OnboardingScreen(onFinish = {}) }
}
