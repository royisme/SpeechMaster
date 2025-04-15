package com.example.speechmaster.ui.components.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speechmaster.ui.theme.AppTheme
import com.example.speechmaster.utils.audio.TextToSpeechWrapper


/**

练习文本展示组件
在练习界面中展示用户需要朗读的文本内容，支持长文本滚动。
@param textContent 练习文本内容
@param modifier Modifier修饰符
 */
@Composable
fun ReadingPracticeComponent(
    textContent: String,
    modifier: Modifier = Modifier,
    textToSpeechWrapper: TextToSpeechWrapper, // Add callback for button action
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) // Example border
    ) {
        Box(
            modifier = Modifier
                .padding(36.dp)
        ) {
            Text(
                text = textContent,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    fontSize = 18.sp // 更大的字体便于阅读
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )

        }
        ReadingTTS(
            textContent = textContent,
            textToSpeechWrapper = textToSpeechWrapper,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        )
    }
}