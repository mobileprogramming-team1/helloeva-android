package com.helloeva.app.data.repository

import com.helloeva.app.data.model.AnswerResult
import com.helloeva.app.data.model.Conversation
import com.helloeva.app.data.model.ConversationHistory
import com.helloeva.app.data.model.FinalFeedback
import com.helloeva.app.data.model.Grade
import com.helloeva.app.data.model.Question
import java.io.File

/**
 * Everything the screens need from the backend.
 * Screens only talk to this interface: swap [FakeConversationRepository]
 * for a Retrofit-backed implementation when the backend is ready.
 */
interface ConversationRepository {
    suspend fun getConversations(): List<Conversation>

    suspend fun getHistory(conversationId: Long): ConversationHistory

    /** survey: page id → selected options. Returns the new conversation. */
    suspend fun createConversation(targetGrade: Grade, survey: Map<String, Set<String>>): Conversation

    suspend fun getQuestion(conversationId: Long, index: Int): Question

    /** Uploads the recorded answer; backend runs STT + analysis. */
    suspend fun submitAnswer(conversationId: Long, index: Int, audio: File?): AnswerResult

    suspend fun getFinalFeedback(conversationId: Long): FinalFeedback
}
