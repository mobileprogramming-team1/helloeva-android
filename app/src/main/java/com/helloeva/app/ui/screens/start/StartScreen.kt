package com.helloeva.app.ui.screens.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helloeva.app.ui.components.EvaAvatar
import com.helloeva.app.ui.components.EvaRobot
import com.helloeva.app.ui.components.EvaTopBar
import com.helloeva.app.ui.components.PrimaryButton
import com.helloeva.app.ui.theme.EvaColors
import com.helloeva.app.ui.theme.HelloEvaTheme

/** Intro before the test starts (new screen, not in the Figma wireframe). */
@Composable
fun StartScreen(onBack: () -> Unit, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 32.dp)) {
        EvaTopBar(onBack = onBack)
        Spacer(Modifier.height(16.dp))
        Text("오늘의 연습을\n시작할게요!", style = MaterialTheme.typography.headlineMedium.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize * 1.08f))

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(250.dp).background(EvaColors.BlueSoft, CircleShape), contentAlignment = Alignment.Center) {
                EvaRobot(Modifier.size(210.dp))
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = 18.dp)
                        .background(EvaColors.Lime, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp))
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                ) {
                    Text("Hi there!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                }
            }
        }

        Column(Modifier.padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EvaAvatar(size = 26.dp)
                Text("Eva의 한마디", style = MaterialTheme.typography.titleSmall)
            }
            Column(
                Modifier.fillMaxWidth().background(EvaColors.Gray50, RoundedCornerShape(18.dp)).padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("질문은 음성으로만 들려드려요", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Text(
                    "다시 듣기는 한 번만 가능해요. 총 15문제, 편하게 말해 보세요!",
                    style = MaterialTheme.typography.bodySmall,
                    color = EvaColors.Gray600,
                )
            }
        }
        PrimaryButton("시작하기", onClick = onStart)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun StartPreview() {
    HelloEvaTheme { StartScreen(onBack = {}, onStart = {}) }
}
