package com.helloeva.app.data.repository

import com.helloeva.app.data.model.AnswerFeedback
import com.helloeva.app.data.model.AnswerResult
import com.helloeva.app.data.model.AreaScores
import com.helloeva.app.data.model.Conversation
import com.helloeva.app.data.model.ConversationHistory
import com.helloeva.app.data.model.ConversationStatus
import com.helloeva.app.data.model.FinalFeedback
import com.helloeva.app.data.model.Grade
import com.helloeva.app.data.model.QUESTIONS_PER_TEST
import com.helloeva.app.data.model.Question
import com.helloeva.app.data.model.Turn
import kotlinx.coroutines.delay
import java.io.File

/** In-memory backend with the example data from the Figma frames. */
class FakeConversationRepository : ConversationRepository {

    private val conversations = mutableListOf(
        Conversation(4, 4, Grade.AL, "2025년 8월 1일", ConversationStatus.IN_PROGRESS, currentQuestion = 3),
        Conversation(3, 3, Grade.AL, "2025년 7월 28일", ConversationStatus.COMPLETED, Grade.IH),
        Conversation(2, 2, Grade.IH, "2025년 7월 21일", ConversationStatus.COMPLETED, Grade.IM3),
        Conversation(1, 1, Grade.IM3, "2025년 7월 14일", ConversationStatus.COMPLETED, Grade.IM2),
    )

    override suspend fun getConversations(): List<Conversation> {
        delay(NETWORK_DELAY)
        return conversations.sortedByDescending { it.round }
    }

    override suspend fun getHistory(conversationId: Long): ConversationHistory {
        delay(NETWORK_DELAY)
        val conversation = conversations.first { it.id == conversationId }
        val answered = when (conversation.status) {
            ConversationStatus.COMPLETED -> QUESTIONS_PER_TEST
            ConversationStatus.IN_PROGRESS -> conversation.currentQuestion - 1
        }
        val turns = (1..answered).map { index ->
            val sample = samples[(index - 1) % samples.size]
            Turn(index, sample.question, sample.answer, sample.feedback(conversation.targetGrade))
        }
        val final = if (conversation.status == ConversationStatus.COMPLETED) {
            finalFeedback(conversation.targetGrade, conversation.resultGrade ?: Grade.IM3)
        } else null
        return ConversationHistory(conversation, turns, final)
    }

    override suspend fun createConversation(targetGrade: Grade, survey: Map<String, Set<String>>): Conversation {
        delay(NETWORK_DELAY)
        val next = (conversations.maxOfOrNull { it.round } ?: 0) + 1
        val created = Conversation(
            id = next.toLong(),
            round = next,
            targetGrade = targetGrade,
            createdAt = "오늘",
            status = ConversationStatus.IN_PROGRESS,
        )
        conversations += created
        return created
    }

    override suspend fun getQuestion(conversationId: Long, index: Int): Question {
        delay(NETWORK_DELAY)
        val sample = samples[(index - 1) % samples.size]
        return Question(index = index, text = sample.question, audioUrl = null, durationSeconds = 15)
    }

    override suspend fun submitAnswer(conversationId: Long, index: Int, audio: File?): AnswerResult {
        delay(ANALYSIS_DELAY)
        val conversation = conversations.first { it.id == conversationId }
        updateConversation(conversationId) {
            it.copy(currentQuestion = (index + 1).coerceAtMost(QUESTIONS_PER_TEST))
        }
        val sample = samples[(index - 1) % samples.size]
        return AnswerResult(sample.question, sample.answer, sample.feedback(conversation.targetGrade))
    }

    override suspend fun getFinalFeedback(conversationId: Long): FinalFeedback {
        delay(ANALYSIS_DELAY)
        val conversation = conversations.first { it.id == conversationId }
        updateConversation(conversationId) {
            it.copy(status = ConversationStatus.COMPLETED, resultGrade = Grade.IM3)
        }
        return finalFeedback(conversation.targetGrade, Grade.IM3)
    }

    private fun updateConversation(id: Long, transform: (Conversation) -> Conversation) {
        val i = conversations.indexOfFirst { it.id == id }
        if (i >= 0) conversations[i] = transform(conversations[i])
    }

    private data class Sample(
        val question: String,
        val answer: String,
        val expected: Grade,
        val tips: List<String>,
        val script: String,
        val keywords: List<String>,
    ) {
        fun feedback(target: Grade) = AnswerFeedback(expected, target, tips, script, keywords)
    }

    private val samples = listOf(
        Sample(
            question = "You indicated in the survey that you enjoy going to parks. Tell me about a park you often visit. Where is it, and what does it look like?",
            answer = "I often go to Hangang Park near my house. It is very big and has many trees. I usually walk there with my friend on weekends, and we talk a lot.",
            expected = Grade.IM2,
            tips = listOf(
                "very big, many trees 같은 단순한 표현 대신 공원의 분위기와 풍경을 구체적으로 묘사해 보세요.",
                "주말 루틴에 이유나 느낌을 덧붙이면 AL 수준의 전개가 돼요.",
                "which, so 같은 연결어로 문장을 이어 길고 자연스럽게 말해 보세요.",
            ),
            script = "Hangang Park is a short walk from my place, and it is lined with tall trees and a long cycling path. On weekends, my friend and I usually take a slow walk there, which helps me unwind after a busy week.",
            keywords = listOf("lined with", "cycling path", "slow walk", "unwind", "short walk"),
        ),
        Sample(
            question = "Tell me about a memorable trip you took abroad. Where did you go, and what made it special?",
            answer = "Last year I traveled to Japan with my family. We visited Osaka and ate many delicious foods. It was a really special trip for me.",
            expected = Grade.IM3,
            tips = listOf(
                "특별했던 순간을 하나 골라 시간 순서대로 구체적으로 풀어 보세요.",
                "느낌을 나타내는 표현을 더해 보세요.",
            ),
            script = "Last spring, my family and I took a trip to Osaka. The highlight was a street food tour in Dotonbori, where we tried takoyaki for the first time. It was unforgettable because we laughed the whole night.",
            keywords = listOf("unforgettable", "local cuisine", "sightseeing"),
        ),
    )

    private fun finalFeedback(target: Grade, overall: Grade) = FinalFeedback(
        overallGrade = overall,
        targetGrade = target,
        scores = AreaScores(fluency = 60, vocabulary = 55, grammar = 65, structure = 50),
        strengths = listOf(
            "질문에 맞는 답변을 끝까지 이어 말했어요.",
            "일상 주제에서 어휘가 자연스러웠어요.",
            "문제가 진행될수록 답변 길이가 늘었어요.",
        ),
        improvements = listOf(
            "시제 일치와 연결어 사용이 아직 불안정해요.",
            "같은 표현을 반복하는 경향이 있어요.",
            "경험을 설명할 때 구체적인 사례가 부족해요.",
        ),
        recommendations = listOf(
            "연결어 표현을 정리하고 한 문제당 3문장 이어 말하기",
            "주제별 핵심 표현 10개씩 정리하기",
            "목표 등급 샘플 답변 따라 말하기",
        ),
        frequentExpressions = listOf("very big", "many", "a lot", "really"),
        closingMessage = "오늘 정말 고생 많았어요. 꾸준히 연습하면 목표인 ${target.name}에 충분히 가까워질 수 있어요. 다음 모의고사에서 또 만나요!",
    )

    private companion object {
        const val NETWORK_DELAY = 400L
        const val ANALYSIS_DELAY = 1_500L
    }
}
