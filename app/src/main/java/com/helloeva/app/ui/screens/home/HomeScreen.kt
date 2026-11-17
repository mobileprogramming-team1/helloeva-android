package com.helloeva.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helloeva.app.data.model.Conversation
import com.helloeva.app.data.model.ConversationStatus
import com.helloeva.app.ui.components.AppLogo
import com.helloeva.app.ui.components.ConversationBadge
import com.helloeva.app.ui.components.EvaIcons
import com.helloeva.app.ui.components.EvaRobot
import com.helloeva.app.ui.components.PillChip
import com.helloeva.app.ui.theme.EvaColors

/** Home: banner + conversation list (Figma: 대화 목록 조회(홈), 빈 상태). */
@Composable
fun HomeScreen(
    onNewConversation: () -> Unit,
    onOpenConversation: (Conversation) -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                AppLogo()
                Spacer(Modifier.width(8.dp))
                Text("Hello Eva", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onSettings) {
                    Icon(EvaIcons.Settings, contentDescription = "설정", tint = EvaColors.Ink)
                }
            }
        }
        item {
            Banner(
                isEmpty = !state.loading && state.conversations.isEmpty(),
                nextRound = (state.conversations.maxOfOrNull { it.round } ?: 0) + 1,
                onStart = onNewConversation,
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
            )
        }
        item {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("나의 대화", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                if (state.conversations.isNotEmpty()) {
                    Text("대화 ${state.visible.size}개", style = MaterialTheme.typography.bodySmall, color = EvaColors.Gray500)
                }
            }
        }
        when {
            state.loading -> item {
                Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EvaColors.Blue)
                }
            }
            state.conversations.isEmpty() -> item { EmptyState() }
            else -> {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                        HomeFilter.entries.forEach { filter ->
                            PillChip(
                                text = filter.label,
                                selected = state.filter == filter,
                                onClick = { viewModel.selectFilter(filter) },
                                selectedColor = EvaColors.Ink,
                            )
                        }
                    }
                }
                items(state.visible, key = { it.id }) { conversation ->
                    ConversationRow(conversation, onClick = { onOpenConversation(conversation) })
                }
            }
        }
    }
}

@Composable
private fun Banner(isEmpty: Boolean, nextRound: Int, onStart: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(172.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(EvaColors.Blue),
    ) {
        Box(
            Modifier
                .size(170.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 40.dp)
                .background(EvaColors.BlueMid, CircleShape),
        )
        EvaRobot(
            Modifier.size(150.dp).align(Alignment.BottomEnd).offset(x = (-2).dp, y = 6.dp),
            bodyColor = EvaColors.Lime,
        )
        Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (isEmpty) "첫 모의고사를\n시작해 보세요" else "오늘의 오픽 연습",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EvaColors.White,
                )
                Text(
                    if (isEmpty) "15문제 · 약 40분" else "${nextRound}번째 모의고사",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EvaColors.BlueOnDark,
                )
            }
            Button(
                onClick = onStart,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = EvaColors.Lime, contentColor = EvaColors.Ink),
                contentPadding = PaddingValues(horizontal = 18.dp),
            ) {
                Text("새 대화 시작", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.width(6.dp))
                Icon(EvaIcons.Play, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ConversationRow(conversation: Conversation, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(18.dp), color = EvaColors.Gray50, contentColor = EvaColors.Ink) {
        Row(
            Modifier.fillMaxWidth().height(72.dp).padding(start = 18.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("대화 ${conversation.round}회차", style = MaterialTheme.typography.titleMedium)
                Text(
                    "목표 ${conversation.targetGrade.name} · ${conversation.createdAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EvaColors.Gray500,
                )
            }
            ConversationBadge(
                inProgress = conversation.status == ConversationStatus.IN_PROGRESS,
                grade = conversation.resultGrade,
            )
            Icon(EvaIcons.ChevronRight, contentDescription = null, tint = EvaColors.Gray400)
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(96.dp).background(EvaColors.Gray50, CircleShape), contentAlignment = Alignment.Center) {
            Icon(EvaIcons.Chat, contentDescription = null, tint = EvaColors.Gray300, modifier = Modifier.size(44.dp))
        }
        Text("아직 대화가 없어요", style = MaterialTheme.typography.titleMedium.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize))
        Text(
            "새 대화를 시작해 오픽 말하기를\n연습해 보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = EvaColors.Gray500,
            textAlign = TextAlign.Center,
        )
    }
}
