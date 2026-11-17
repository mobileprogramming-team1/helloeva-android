package com.helloeva.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helloeva.app.ui.components.EvaIcons
import com.helloeva.app.ui.components.EvaTopBar
import com.helloeva.app.ui.theme.EvaColors
import com.helloeva.app.ui.theme.HelloEvaTheme

/** Settings (Figma: 설정). */
@Composable
fun SettingsScreen(onBack: () -> Unit, onLogout: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EvaTopBar(onBack = onBack)
            Text("설정", style = MaterialTheme.typography.headlineLarge)
        }

        Row(
            Modifier.fillMaxWidth().background(EvaColors.BlueSoft, RoundedCornerShape(20.dp)).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.size(56.dp).background(EvaColors.Blue, CircleShape), contentAlignment = Alignment.Center) {
                Icon(EvaIcons.Person, contentDescription = null, tint = EvaColors.White, modifier = Modifier.size(30.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // TODO: real nickname from the user profile API
                Text("사용자 닉네임", style = MaterialTheme.typography.titleMedium)
                Text("kakao 계정으로 로그인 중", style = MaterialTheme.typography.bodySmall, color = EvaColors.Gray700)
            }
        }

        // TODO: open notice / terms / privacy screens (WebView or text)
        SettingsGroup("정보") {
            SettingsRow("공지사항", onClick = {})
            SettingsRow("이용약관", onClick = {})
            SettingsRow("개인정보 처리방침", onClick = {})
            SettingsRow("앱 버전", trailingText = "1.0.0", showDivider = false)
        }
        SettingsGroup("계정") {
            SettingsRow("로그아웃", onClick = onLogout)
            // TODO: confirm dialog + withdraw API
            SettingsRow("회원탈퇴", onClick = {}, textColor = EvaColors.Danger, showDivider = false)
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, Modifier.padding(start = 4.dp), style = MaterialTheme.typography.labelMedium, color = EvaColors.Gray500)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(EvaColors.Gray50)) { content() }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    onClick: (() -> Unit)? = null,
    trailingText: String? = null,
    textColor: Color = EvaColors.Ink,
    showDivider: Boolean = true,
) {
    Surface(onClick = { onClick?.invoke() }, enabled = onClick != null, color = Color.Transparent) {
        Column {
            Row(
                Modifier.fillMaxWidth().height(54.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = textColor)
                if (trailingText != null) {
                    Text(trailingText, style = MaterialTheme.typography.bodyMedium, color = EvaColors.Gray500)
                } else {
                    Icon(EvaIcons.ChevronRight, contentDescription = null, tint = EvaColors.Gray400)
                }
            }
            if (showDivider) HorizontalDivider(color = EvaColors.Gray200)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsPreview() {
    HelloEvaTheme { SettingsScreen(onBack = {}, onLogout = {}) }
}
