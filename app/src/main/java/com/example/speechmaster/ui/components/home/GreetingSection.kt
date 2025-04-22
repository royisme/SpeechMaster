package com.example.speechmaster.ui.components.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R

@Composable
fun GreetingSection(userName: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
        Text(
            text = stringResource(R.string.home_greeting, userName),
            style = MaterialTheme.typography.headlineSmall // 稍微调整字号
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyMedium, // 调整样式
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}