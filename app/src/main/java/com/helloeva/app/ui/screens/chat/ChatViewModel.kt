package com.helloeva.app.ui.screens.chat

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.PlaybackException
import androidx.navigation.toRoute
import com.helloeva.app.audio.AnswerRecorder
import com.helloeva.app.audio.QuestionPlayer
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
    RECORDING,      // ③ recording, max 2:00
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
    /** One-line message shown above the mic button (e.g. recording was cancelled). */
    val notice: String? = null,
) {
    val isLastQuestion: Boolean get() = questionIndex == QUESTIONS_PER_TEST
}

class ChatViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ConversationRepository,
    private val player: QuestionPlayer,
    private val recorder: AnswerRecorder,
) : ViewModel() {

    private val conversationId = savedStateHandle.toRoute<ChatRoute>().conversationId

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var recordJob: Job? = null
    private var questionAudioUrl: String? = null
    private var recordStartedAt = 0L

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
            _uiState.update { it.copy(phase = ChatPhase.ASKING, playProgress = 0f, replaysLeft = 1, recordSeconds = 0, notice = null) }
            val question = repository.getQuestion(conversationId, _uiState.value.questionIndex)
            questionAudioUrl = question.audioUrl
            _uiState.update { it.copy(questionSeconds = question.durationSeconds) }
            playQuestion()
        }
    }

    /** Plays the question audio, then moves to READY. */
    private suspend fun playQuestion() {
        _uiState.update { it.copy(phase = ChatPhase.ASKING, playProgress = 0f, notice = null) }
        val url = questionAudioUrl
        if (url == null) {
            fakePlayback() // the fake repo has no audio files
        } else {
            try {
                player.play(url) { progress -> _uiState.update { it.copy(playProgress = progress) } }
            } catch (e: PlaybackException) {
                _uiState.update { it.copy(notice = "질문 음성을 재생하지 못했어요") }
            }
        }
        _uiState.update { it.copy(phase = ChatPhase.READY) }
    }

    private suspend fun fakePlayback() {
        val steps = 30 // 3 seconds
        for (i in 1..steps) {
            delay(100)
            _uiState.update { it.copy(playProgress = i / steps.toFloat()) }
        }
    }

    fun replay() {
        val state = _uiState.value
        if (state.phase != ChatPhase.READY || state.replaysLeft <= 0) return
        _uiState.update { it.copy(replaysLeft = it.replaysLeft - 1) }
        playbackJob = viewModelScope.launch { playQuestion() }
    }

    /** Call only after RECORD_AUDIO is granted (ChatScreen checks). */
    fun startRecording() {
        if (_uiState.value.phase != ChatPhase.READY) return
        try {
            recorder.start()
        } catch (e: Exception) {
            _uiState.update { it.copy(notice = "마이크를 열 수 없어요. 다른 앱이 사용 중인지 확인해 주세요") }
            return
        }
        recordStartedAt = SystemClock.elapsedRealtime()
        _uiState.update { it.copy(phase = ChatPhase.RECORDING, recordSeconds = 0, notice = null) }
        recordJob = viewModelScope.launch {
            while (_uiState.value.recordSeconds < MAX_ANSWER_SECONDS) {
                delay(1_000)
                _uiState.update { it.copy(recordSeconds = it.recordSeconds + 1) }
            }
            stopRecording() // auto-stop at 2:00
        }
    }

    fun stopRecording() {
        if (_uiState.value.phase != ChatPhase.RECORDING) return
        recordJob?.cancel()
        val tooShort = SystemClock.elapsedRealtime() - recordStartedAt < MIN_ANSWER_MILLIS
        val audio = recorder.stop()
        if (tooShort) audio?.delete()
        if (audio == null || tooShort) {
            val message = if (tooShort) "녹음이 너무 짧아요. 다시 답변해 주세요" else "녹음에 실패했어요. 다시 답변해 주세요"
            _uiState.update { it.copy(phase = ChatPhase.READY, notice = message) }
            return
        }
        _uiState.update { it.copy(phase = ChatPhase.ANALYZING) }
        viewModelScope.launch {
            val index = _uiState.value.questionIndex
            // TODO(Part B): error state + retry (keep the file until the upload succeeds)
            val result = repository.submitAnswer(conversationId, index, audio)
            audio.delete()
            val turn = Turn(index, result.questionText, result.answerText, result.feedback)
            _uiState.update { it.copy(phase = ChatPhase.FEEDBACK, turns = it.turns + turn) }
        }
    }

    /** The user left the screen (Home, another app, a call): pause the question, throw away a half-finished answer. */
    fun onScreenPaused() {
        when (_uiState.value.phase) {
            ChatPhase.ASKING -> player.pause()
            ChatPhase.RECORDING -> {
                recordJob?.cancel()
                recorder.cancel()
                _uiState.update {
                    it.copy(phase = ChatPhase.READY, recordSeconds = 0, notice = "앱을 나가서 녹음이 취소됐어요. 다시 답변해 주세요")
                }
            }
            else -> Unit
        }
    }

    fun onScreenResumed() {
        if (_uiState.value.phase == ChatPhase.ASKING) player.resume()
    }

    override fun onCleared() {
        recorder.cancel()
        player.release()
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
        /** The API accepts answers up to 2 minutes. */
        const val MAX_ANSWER_SECONDS = 120

        /** Shorter than this is almost always an accidental double tap. */
        private const val MIN_ANSWER_MILLIS = 1_000L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY]!!
                ChatViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = appContainer().conversationRepository,
                    player = QuestionPlayer(app),
                    recorder = AnswerRecorder(app),
                )
            }
        }
    }
}
