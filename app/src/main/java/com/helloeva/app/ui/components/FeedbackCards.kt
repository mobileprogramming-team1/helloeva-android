package com.helloeva.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helloeva.app.data.model.AnswerFeedback
import com.helloeva.app.data.model.AreaScores
import com.helloeva.app.data.model.FinalFeedback
import com.helloeva.app.data.model.Grade
import com.helloeva.app.ui.theme.EvaColors
import com.helloeva.app.ui.theme.HelloEvaTheme

private val CardShape = RoundedCornerShape(20.dp)

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = EvaColors.Ink)
}

@Composable
private fun BulletList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach {
            Text("• $it", style = MaterialTheme.typography.bodySmall, color = EvaColors.Gray700)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(items: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { KeywordChip(it) }
    }
}

/** 개별 피드백: "Eva의 답변 분석" card after each answer. */
@Composable
fun AnswerAnalysisCard(feedback: AnswerFeedback, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .border(1.5.dp, EvaColors.Gray200, CardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Eva의 답변 분석", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
        GradeComparison(expected = feedback.expectedGrade, target = feedback.targetGrade)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionTitle("답변 디벨롭 (목표 ${feedback.targetGrade.name} 기준)")
            BulletList(feedback.developTips)
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(EvaColors.Gray50, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("추천 스크립트", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = EvaColors.Blue)
            Text(feedback.recommendedScript, style = MaterialTheme.typography.bodySmall, color = EvaColors.Ink)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("주요 단어")
            ChipFlow(feedback.keywords)
        }
    }
}

/** 전체 피드백: "모의고사 종합 피드백" card after question 15. */
@Composable
fun FinalFeedbackCard(feedback: FinalFeedback, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(CardShape)
            .border(1.5.dp, EvaColors.Gray200, CardShape),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(EvaColors.Blue)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "모의고사 종합 피드백",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = EvaColors.White,
            )
            GradeComparison(
                expected = feedback.overallGrade,
                target = feedback.targetGrade,
                expectedLabel = "종합 예상 등급",
                onDark = true,
            )
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("영역별 평가")
                ScoreBar("유창성", feedback.scores.fluency)
                ScoreBar("어휘", feedback.scores.vocabulary)
                ScoreBar("문법", feedback.scores.grammar)
                ScoreBar("내용 구성", feedback.scores.structure)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionTitle("잘한 점")
                BulletList(feedback.strengths)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionTitle("보완할 점")
                BulletList(feedback.improvements)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionTitle("추천 학습")
                BulletList(feedback.recommendations)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("자주 쓴 표현")
                ChipFlow(feedback.frequentExpressions)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun FeedbackCardsPreview() {
    HelloEvaTheme {
        Column(
            Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AnswerAnalysisCard(
                AnswerFeedback(
                    expectedGrade = Grade.IM2,
                    targetGrade = Grade.AL,
                    developTips = listOf("공원의 분위기를 구체적으로 묘사해 보세요.", "연결어로 문장을 이어 보세요."),
                    recommendedScript = "Hangang Park is a short walk from my place, and it is lined with tall trees.",
                    keywords = listOf("lined with", "cycling path", "unwind"),
                ),
            )
            FinalFeedbackCard(
                FinalFeedback(
                    overallGrade = Grade.IM3,
                    targetGrade = Grade.AL,
                    scores = AreaScores(60, 55, 65, 50),
                    strengths = listOf("질문에 맞는 답변을 끝까지 이어 말했어요."),
                    improvements = listOf("시제 일치와 연결어 사용이 아직 불안정해요."),
                    recommendations = listOf("주제별 핵심 표현 10개씩 정리하기"),
                    frequentExpressions = listOf("very big", "many", "a lot"),
                    closingMessage = "",
                ),
            )
        }
    }
}
