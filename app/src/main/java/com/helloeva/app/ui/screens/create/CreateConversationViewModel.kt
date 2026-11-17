package com.helloeva.app.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.helloeva.app.data.model.Grade
import com.helloeva.app.data.model.SURVEY_MIN_TOTAL
import com.helloeva.app.data.model.SurveyData
import com.helloeva.app.data.model.SurveyPage
import com.helloeva.app.data.repository.ConversationRepository
import com.helloeva.app.ui.appContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateUiState(
    val targetGrade: Grade? = null,
    val pageIndex: Int = 0,
    /** page id → selected options */
    val answers: Map<String, Set<String>> = emptyMap(),
    val submitting: Boolean = false,
) {
    val pages: List<SurveyPage> get() = SurveyData.pages
    val page: SurveyPage get() = pages[pageIndex]
    val isLastPage: Boolean get() = pageIndex == pages.lastIndex
    val selected: Set<String> get() = answers[page.id].orEmpty()
    /** Running total across all multi-select pages (OPIc requires ≥ 12). */
    val total: Int get() = pages.filter { it.multiSelect }.sumOf { answers[it.id].orEmpty().size }
    val pageValid: Boolean get() = selected.size >= page.minSelect
    val totalValid: Boolean get() = total >= SURVEY_MIN_TOTAL
    val canProceed: Boolean get() = pageValid && (!isLastPage || totalValid) && !submitting
}

/** Shared by the target-grade screen and all 8 survey pages (scoped to the create nav graph). */
class CreateConversationViewModel(private val repository: ConversationRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateUiState())
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    fun selectGrade(grade: Grade) = _uiState.update { it.copy(targetGrade = grade) }

    fun toggle(option: String) = _uiState.update { state ->
        val page = state.page
        val current = state.selected
        val next = when {
            !page.multiSelect -> setOf(option)
            option == page.exclusiveOption -> if (option in current) emptySet() else setOf(option)
            option in current -> current - option
            else -> current + option - setOfNotNull(page.exclusiveOption)
        }
        state.copy(answers = state.answers + (page.id to next))
    }

    /** @return false when already on the first page (caller should navigate back). */
    fun previousPage(): Boolean {
        if (_uiState.value.pageIndex == 0) return false
        _uiState.update { it.copy(pageIndex = it.pageIndex - 1) }
        return true
    }

    fun nextPage(onCreated: (conversationId: Long) -> Unit) {
        val state = _uiState.value
        if (!state.canProceed) return
        if (!state.isLastPage) {
            _uiState.update { it.copy(pageIndex = it.pageIndex + 1) }
            return
        }
        val grade = state.targetGrade ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(submitting = true) }
            val conversation = repository.createConversation(grade, state.answers)
            _uiState.update { it.copy(submitting = false) }
            onCreated(conversation.id)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { CreateConversationViewModel(appContainer().conversationRepository) }
        }
    }
}
