package com.helloeva.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.helloeva.app.data.model.Conversation
import com.helloeva.app.data.model.ConversationStatus
import com.helloeva.app.data.repository.ConversationRepository
import com.helloeva.app.ui.appContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeFilter(val label: String) { ALL("전체"), IN_PROGRESS("진행중"), COMPLETED("완료") }

data class HomeUiState(
    val loading: Boolean = true,
    val conversations: List<Conversation> = emptyList(),
    val filter: HomeFilter = HomeFilter.ALL,
) {
    val visible: List<Conversation>
        get() = when (filter) {
            HomeFilter.ALL -> conversations
            HomeFilter.IN_PROGRESS -> conversations.filter { it.status == ConversationStatus.IN_PROGRESS }
            HomeFilter.COMPLETED -> conversations.filter { it.status == ConversationStatus.COMPLETED }
        }
}

class HomeViewModel(private val repository: ConversationRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val list = repository.getConversations()
            _uiState.update { it.copy(loading = false, conversations = list) }
        }
    }

    fun selectFilter(filter: HomeFilter) = _uiState.update { it.copy(filter = filter) }

    companion object {
        val Factory = viewModelFactory {
            initializer { HomeViewModel(appContainer().conversationRepository) }
        }
    }
}
