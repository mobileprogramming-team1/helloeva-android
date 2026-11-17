package com.helloeva.app.data.model

import kotlinx.serialization.Serializable

/** 개별 피드백 — shown after each answer. */
@Serializable
data class AnswerFeedback(
    val expectedGrade: Grade,          // 예상 등급
    val targetGrade: Grade,            // 목표 등급
    val developTips: List<String>,     // 답변 디벨롭
    val recommendedScript: String,     // 추천 스크립트 ("이렇게 말해 보세요")
    val keywords: List<String>,        // 주요 단어
)

/** 영역별 평가, each 0–100. */
@Serializable
data class AreaScores(
    val fluency: Int,       // 유창성
    val vocabulary: Int,    // 어휘
    val grammar: Int,       // 문법
    val structure: Int,     // 내용 구성
)

/** 전체 피드백 — shown after question 15. */
@Serializable
data class FinalFeedback(
    val overallGrade: Grade,                 // 종합 예상 등급
    val targetGrade: Grade,                  // 목표 등급
    val scores: AreaScores,                  // 영역별 평가
    val strengths: List<String>,             // 잘한 점
    val improvements: List<String>,          // 보완할 점
    val recommendations: List<String>,       // 추천 학습
    val frequentExpressions: List<String>,   // 자주 쓴 표현
    val closingMessage: String,              // Eva's closing message
)
