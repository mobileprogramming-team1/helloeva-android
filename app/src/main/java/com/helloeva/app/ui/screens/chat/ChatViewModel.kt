package com.helloeva.app.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.helloeva.app.data.model.ConversationStatus
import com.helloeva.app.data.model.FinalFeedback
import com.helloeva.app.data.model.Grade
import com.helloeva.app.data.model.QUESTIONS_PER_TEST
import com.helloeva.app.data.model.Turn
import com.helloeva.app.data.repository.ConversationRepository
import com.helloeva.app.ui.appContainer
import com.helloeva.app.ui.navigation.ChatRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The mock test state machine. Each phase maps to one Figma/preview state. */
enum class ChatPhase {
    LOADING,
    ASKING,         // ① Eva's audio question is playing
    READY,          // ② tap mic to answer (1 replay allowed)
    RECORDING,      // ③ recording, max 3:00
    ANALYZING,      // ④ uploading + STT + analysis
    FEEDBACK,       // ⑤ question/answer text + analysis card
    FINAL_LOADING,  // building the final feedback
    FINISHED,       // ⑥ final feedback
}

data class ChatUiState(
    val phase: ChatPhase = ChatPhase.LOADING,
    val targetGrade: Grade? = null,
    val questionIndex: Int = 1,
    val turns: List<Turn> = emptyList(),
    val questionSeconds: Int = 15,
    val playProgress: Float = 0f,
    val replaysLeft: Int = 1,
    val recordSeconds: Int = 0,
    val finalFeedback: FinalFeedback? = null,
) {
    val isLastQuestion: Boolean get() = questionIndex == QUESTIONS_PER_TEST
}

class ChatViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ConversationRepository,
) : ViewModel() {

    private val conversationId = savedStateHandle.toRoute<ChatRoute>().conversationId

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var recordJob: Job? = null

    init {
        viewModelScope.launch {
            val history = repository.getHistory(conversationId)
            _uiState.update {
                it.copy(
                    targetGrade = history.conversation.targetGrade,
                    turns = history.turns,
                    questionIndex = (history.turns.size + 1).coerceAtMost(QUESTIONS_PER_TEST),
                    finalFeedback = history.finalFeedback,
                )
            }
            if (history.conversation.status == ConversationStatus.COMPLETED) {
                _uiState.update { it.copy(phase = ChatPhase.FINISHED, questionIndex = QUESTIONS_PER_TEST) }
            } else {
                startQuestion()
            }
        }
    }

    private fun startQuestion() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            _uiState.update { it.copy(phase = ChatPhase.ASKING, playProgress = 0f, replaysLeft = 1, recordSeconds = 0) }
            val question = repository.getQuestion(conversationId, _uiState.value.questionIndex)
            _uiState.update { it.copy(questionSeconds = question.durationSeconds) }
            playQuestion()
        }
    }

    /** TODO: play question.audioUrl with ExoPlayer and drive progress from the player. */
    private suspend fun playQuestion() {
        _uiState.update { it.copy(phase = ChatPhase.ASKING, playProgress = 0f) }
        val steps = 30 // fake: 3 seconds
        for (i in 1..steps) {
            delay(100)
            _uiState.update { it.copy(playProgress = i / steps.toFloat()) }
        }
        _uiState.update { it.copy(phase = ChatPhase.READY) }
    }

    fun replay() {
        val state = _uiState.value
        if (state.phase != ChatPhase.READY || state.replaysLeft <= 0) return
        _uiState.update { it.copy(replaysLeft = it.replaysLeft - 1) }
        playbackJob = viewModelScope.launch { playQuestion() }
    }

    /** TODO: start MediaRecorder (m4a/AAC) into a cache file. Call only after RECORD_AUDIO is granted. */
    fun startRecording() {
        if (_uiState.value.phase != ChatPhase.READY) return
        _uiState.update { it.copy(phase = ChatPhase.RECORDING, recordSeconds = 0) }
        recordJob = viewModelScope.launch {
            while (_uiState.value.recordSeconds < MAX_ANSWER_SECONDS) {
                delay(1_000)
                _uiState.update { it.copy(recordSeconds = it.recordSeconds + 1) }
            }
            stopRecording() // auto-stop at 3:00
        }
    }

    fun stopRecording() {
        if (_uiState.value.phase != ChatPhase.RECORDING) return
        recordJob?.cancel()
        _uiState.update { it.copy(phase = ChatPhase.ANALYZING) }
        viewModelScope.launch {
            val index = _uiState.value.questionIndex
            // TODO: pass the recorded file
            val result = repository.submitAnswer(conversationId, index, audio = null)
            val turn = Turn(index, result.questionText, result.answerText, result.feedback)
            _uiState.update { it.copy(phase = ChatPhase.FEEDBACK, turns = it.turns + turn) }
        }
    }

    fun next() {
        val state = _uiState.value
        if (state.phase != ChatPhase.FEEDBACK) return
        if (!state.isLastQuestion) {
            _uiState.update { it.copy(questionIndex = it.questionIndex + 1) }
            startQuestion()
        } else {
            viewModelScope.launch {
                _uiState.update { it.copy(phase = ChatPhase.FINAL_LOADING) }
                val final = repository.getFinalFeedback(conversationId)
                _uiState.update { it.copy(phase = ChatPhase.FINISHED, finalFeedback = final) }
            }
        }
    }

    companion object {
        const val MAX_ANSWER_SECONDS = 180

        val Factory = viewModelFactory {
            initializer { ChatViewModel(createSavedStateHandle(), appContainer().conversationRepository) }
        }
    }
}
