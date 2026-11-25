package com.helloeva.app.ui.screens.chat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helloeva.app.data.model.QUESTIONS_PER_TEST
import com.helloeva.app.ui.components.AnswerAnalysisCard
import com.helloeva.app.ui.components.EvaAudioBubble
import com.helloeva.app.ui.components.EvaIcons
import com.helloeva.app.ui.components.EvaMessage
import com.helloeva.app.ui.components.EvaTextBubble
import com.helloeva.app.ui.components.EvaTopBar
import com.helloeva.app.ui.components.FinalFeedbackCard
import com.helloeva.app.ui.components.PrimaryButton
import com.helloeva.app.ui.components.QuestionDivider
import com.helloeva.app.ui.components.RecordButton
import com.helloeva.app.ui.components.StatusBar
import com.helloeva.app.ui.components.StepCounter
import com.helloeva.app.ui.components.StepProgressBar
import com.helloeva.app.ui.components.TypingBubble
import com.helloeva.app.ui.components.UserAudioMessage
import com.helloeva.app.ui.components.UserTextMessage
import com.helloeva.app.ui.components.Waveform
import com.helloeva.app.ui.theme.EvaColors

private const val ITEMS_PER_TURN = 4

/** Mock test chat (Figma: 대화 상세 조회 1–8 + the new ②/④ states). */
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val activity = LocalActivity.current
    var micDialog by remember { mutableStateOf<MicDialog?>(null) }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.startRecording()
        } else {
            // After "don't ask again" the system dialog no longer shows, so send the user to Settings.
            val canAskAgain = activity != null &&
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)
            micDialog = if (canAskAgain) MicDialog.ASK_AGAIN else MicDialog.OPEN_SETTINGS
        }
    }
    val onRecord = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.startRecording() else micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    // ON_PAUSE, not ON_STOP: Android can deliver ON_STOP seconds late after Home,
    // and we'd keep recording while the app is hidden.
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.onScreenPaused() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onScreenResumed() }

    micDialog?.let { dialog ->
        MicPermissionDialog(
            dialog = dialog,
            onConfirm = {
                micDialog = null
                when (dialog) {
                    MicDialog.ASK_AGAIN -> micPermission.launch(Manifest.permission.RECORD_AUDIO)
                    MicDialog.OPEN_SETTINGS -> context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                    )
                }
            },
            onDismiss = { micDialog = null },
        )
    }

    // Keep the newest content in view.
    LaunchedEffect(state.phase, state.turns.size) {
        val total = listState.layoutInfo.totalItemsCount
        if (total == 0) return@LaunchedEffect
        val target = when (state.phase) {
            ChatPhase.FEEDBACK -> (state.turns.size - 1) * ITEMS_PER_TURN + 1     // this turn's question text
            ChatPhase.FINISHED -> state.turns.size * ITEMS_PER_TURN               // "시험 종료" divider
            else -> total - 1
        }
        listState.animateScrollToItem(target.coerceIn(0, total - 1))
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 12.dp, end = 20.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EvaTopBar(
                onBack = onBack,
                backLabel = "목록",
                title = "OPIc 모의고사",
                trailing = {
                    if (state.phase == ChatPhase.FINISHED) {
                        Text("완료", style = MaterialTheme.typography.titleSmall, color = EvaColors.Blue)
                    } else {
                        StepCounter(state.questionIndex, QUESTIONS_PER_TEST)
                    }
                },
            )
            StepProgressBar(
                progress = if (state.phase == ChatPhase.FINISHED) 1f else state.questionIndex / QUESTIONS_PER_TEST.toFloat(),
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        if (state.phase == ChatPhase.LOADING) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EvaColors.Blue)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                finishedTurns(state)
                currentQuestion(state, onReplay = viewModel::replay)
                finalSection(state)
            }
        }

        HorizontalDivider(color = EvaColors.Gray100)
        Box(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp), contentAlignment = Alignment.Center) {
            BottomAction(
                state = state,
                onRecord = onRecord,
                onStop = viewModel::stopRecording,
                onNext = viewModel::next,
            )
        }
    }
}

private fun LazyListScope.finishedTurns(state: ChatUiState) {
    state.turns.forEach { turn ->
        item(key = "turn-${turn.index}-divider") { QuestionDivider("문제 ${turn.index} / $QUESTIONS_PER_TEST") }
        item(key = "turn-${turn.index}-q") { EvaMessage(label = "Eva의 질문") { EvaTextBubble(turn.questionText) } }
        item(key = "turn-${turn.index}-a") { UserTextMessage(turn.answerText) }
        item(key = "turn-${turn.index}-card") { EvaMessage { AnswerAnalysisCard(turn.feedback) } }
    }
}

private fun LazyListScope.currentQuestion(state: ChatUiState, onReplay: () -> Unit) {
    val active = state.phase in setOf(ChatPhase.ASKING, ChatPhase.READY, ChatPhase.RECORDING, ChatPhase.ANALYZING)
    if (!active) return
    val i = state.questionIndex

    item(key = "current-$i-divider") { QuestionDivider("문제 $i / $QUESTIONS_PER_TEST") }
    item(key = "current-$i-audio") {
        val total = state.questionSeconds
        EvaMessage {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                when (state.phase) {
                    ChatPhase.ASKING -> {
                        val played = (total * state.playProgress).toInt()
                        EvaAudioBubble("${played.mmss()} / ${total.mmss()}", state.playProgress, icon = EvaIcons.Pause, iconDescription = "재생 중")
                        Caption("질문은 음성으로만 제공돼요")
                    }
                    ChatPhase.READY -> {
                        val canReplay = state.replaysLeft > 0
                        EvaAudioBubble(
                            total.mmss(), 0f,
                            icon = EvaIcons.Replay,
                            iconDescription = "다시 듣기",
                            onIconClick = if (canReplay) onReplay else null,
                            dimmed = !canReplay,
                        )
                        if (canReplay) {
                            Text("다시 듣기 (${state.replaysLeft}회 남음)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = EvaColors.Blue)
                        } else {
                            Caption("다시 듣기를 모두 사용했어요")
                        }
                    }
                    else -> {
                        EvaAudioBubble(total.mmss(), 0f, icon = EvaIcons.Replay, dimmed = true)
                        if (state.phase == ChatPhase.RECORDING) Caption("녹음 중에는 다시 들을 수 없어요")
                    }
                }
            }
        }
    }
    if (state.phase == ChatPhase.RECORDING || state.phase == ChatPhase.ANALYZING) {
        item(key = "current-$i-answer") {
            UserAudioMessage(
                timeText = state.recordSeconds.mmssPadded(),
                caption = if (state.phase == ChatPhase.RECORDING) "답변을 녹음하고 있어요 · 최대 ${ChatViewModel.MAX_ANSWER_SECONDS.mmss()}" else "답변 전송 완료",
                recording = state.phase == ChatPhase.RECORDING,
            )
        }
    }
    if (state.phase == ChatPhase.ANALYZING) {
        item(key = "current-$i-typing") { EvaMessage { TypingBubble() } }
    }
}

private fun LazyListScope.finalSection(state: ChatUiState) {
    if (state.phase == ChatPhase.FINAL_LOADING) {
        item(key = "final-typing") { EvaMessage { TypingBubble() } }
    }
    val final = state.finalFeedback ?: return
    if (state.phase != ChatPhase.FINISHED) return
    item(key = "final-divider") { QuestionDivider("시험 종료 · 총 ${QUESTIONS_PER_TEST}문제") }
    item(key = "final-hello") { EvaMessage { EvaTextBubble("수고하셨어요! ${QUESTIONS_PER_TEST}문제를 모두 마쳤어요. 전체 결과를 정리해 드릴게요.") } }
    item(key = "final-card") { EvaMessage { FinalFeedbackCard(final) } }
    item(key = "final-bye") { EvaMessage { EvaTextBubble(final.closingMessage) } }
}

@Composable
private fun BottomAction(
    state: ChatUiState,
    onRecord: () -> Unit,
    onStop: () -> Unit,
    onNext: () -> Unit,
) {
    when (state.phase) {
        ChatPhase.LOADING -> StatusBar("불러오는 중...")
        ChatPhase.ASKING -> StatusBar("Eva가 질문하고 있어요...", leading = {
            Waveform(listOf(8, 14, 18, 12, 7), playedFraction = 1f, playedColor = EvaColors.Blue, idleColor = EvaColors.Blue)
        })
        ChatPhase.READY -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .padding(bottom = 12.dp)
                    .background(EvaColors.Lime, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(state.notice ?: "준비되면 마이크를 눌러 답변해 주세요", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
            RecordButton(recording = false, onClick = onRecord, label = "눌러서 답변 시작")
        }
        ChatPhase.RECORDING -> RecordButton(recording = true, onClick = onStop, label = "눌러서 답변 완료")
        ChatPhase.ANALYZING -> StatusBar("Eva가 답변을 분석하고 있어요...", leading = { SmallSpinner() })
        ChatPhase.FEEDBACK -> PrimaryButton(if (state.isLastQuestion) "전체 피드백 보기" else "다음 문제", onClick = onNext)
        ChatPhase.FINAL_LOADING -> StatusBar("전체 결과를 정리하고 있어요...", leading = { SmallSpinner() })
        ChatPhase.FINISHED -> StatusBar("수고하셨습니다 :)")
    }
}

private enum class MicDialog { ASK_AGAIN, OPEN_SETTINGS }

@Composable
private fun MicPermissionDialog(dialog: MicDialog, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("마이크 권한이 필요해요") },
        text = {
            Text(
                when (dialog) {
                    MicDialog.ASK_AGAIN -> "답변을 녹음해서 Eva가 분석하려면 마이크 권한이 필요해요."
                    MicDialog.OPEN_SETTINGS -> "답변을 녹음하려면 마이크 권한이 필요해요. 설정 > 권한에서 마이크를 허용해 주세요."
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(if (dialog == MicDialog.ASK_AGAIN) "허용하기" else "설정 열기")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

@Composable
private fun Caption(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = EvaColors.Gray500)
}

@Composable
private fun SmallSpinner() {
    CircularProgressIndicator(Modifier.size(18.dp), color = EvaColors.Blue, strokeWidth = 2.dp)
}

private fun Int.mmss() = "%d:%02d".format(this / 60, this % 60)
private fun Int.mmssPadded() = "%02d:%02d".format(this / 60, this % 60)
