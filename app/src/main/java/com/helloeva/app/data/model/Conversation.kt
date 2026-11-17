package com.helloeva.app.data.model

import kotlinx.serialization.Serializable

const val QUESTIONS_PER_TEST = 15

@Serializable
enum class ConversationStatus { IN_PROGRESS, COMPLETED }

/** One mock test, as listed on Home. */
@Serializable
data class Conversation(
    val id: Long,
    val round: Int,                 // "대화 N회차"
    val targetGrade: Grade,
    val createdAt: String,          // display date, e.g. "2025년 8월 1일"
    val status: ConversationStatus,
    val resultGrade: Grade? = null, // set when COMPLETED
    val currentQuestion: Int = 1,   // where to resume when IN_PROGRESS
)

/** A question as delivered before answering: audio only, text hidden in the UI. */
@Serializable
data class Question(
    val index: Int,                 // 1..15
    val text: String,
    val audioUrl: String?,          // TTS audio; null in the fake repo
    val durationSeconds: Int,
)

/** Result of submitting an answer (STT + analysis). */
@Serializable
data class AnswerResult(
    val questionText: String,
    val answerText: String,
    val feedback: AnswerFeedback,
)

/** One finished question, shown in the chat as text + analysis card. */
data class Turn(
    val index: Int,
    val questionText: String,
    val answerText: String,
    val feedback: AnswerFeedback,
)

/** Everything needed to show a finished test read-only. */
data class ConversationHistory(
    val conversation: Conversation,
    val turns: List<Turn>,
    val finalFeedback: FinalFeedback?,
)
