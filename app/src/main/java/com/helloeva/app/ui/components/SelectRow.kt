package com.helloeva.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helloeva.app.ui.theme.EvaColors
import com.helloeva.app.ui.theme.HelloEvaTheme

/** Single-select option row (target grade, survey pages 1–4). */
@Composable
fun SelectRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) EvaColors.BlueSoft else EvaColors.Gray50,
        border = BorderStroke(2.dp, if (selected) EvaColors.Blue else EvaColors.Gray50),
    ) {
        Row(
            Modifier
                .heightIn(min = 52.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            leading?.invoke()
            Text(
                text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (selected) EvaColors.Blue else EvaColors.Gray900,
            )
            Icon(
                EvaIcons.Check,
                contentDescription = if (selected) "선택됨" else null,
                tint = if (selected) EvaColors.Blue else EvaColors.Gray300,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectRowPreview() {
    HelloEvaTheme {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SelectRow("개인 주택·아파트에 혼자 거주", selected = true, onClick = {})
            SelectRow("학교 기숙사", selected = false, onClick = {})
        }
    }
}
