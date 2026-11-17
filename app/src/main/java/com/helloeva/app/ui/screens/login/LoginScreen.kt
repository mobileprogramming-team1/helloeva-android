package com.helloeva.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helloeva.app.ui.components.EvaRobot
import com.helloeva.app.ui.components.KakaoLoginButton
import com.helloeva.app.ui.theme.EvaColors
import com.helloeva.app.ui.theme.HelloEvaTheme

/** Splash + login (Figma: 스플래시). */
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier.size(220.dp).background(EvaColors.Lime, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                EvaRobot(Modifier.size(180.dp))
            }
            Spacer(Modifier.height(32.dp))
            Text("Hello Eva", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(8.dp))
            Text("AI Eva와 함께하는 오픽 말하기 연습", style = MaterialTheme.typography.bodyLarge, color = EvaColors.Gray600)
        }
        // TODO: Kakao SDK login → send Kakao token to backend → save JWT
        KakaoLoginButton(onClick = onLoginSuccess)
        Spacer(Modifier.height(14.dp))
        Text(
            "시작하면 이용약관 및 개인정보 처리방침에 동의하게 됩니다",
            style = MaterialTheme.typography.labelSmall,
            color = EvaColors.Gray500,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginPreview() {
    HelloEvaTheme { LoginScreen(onLoginSuccess = {}) }
}
