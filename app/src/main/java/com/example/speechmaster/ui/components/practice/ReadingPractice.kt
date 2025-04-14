package com.example.speechmaster.ui.components.practice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speechmaster.ui.theme.AppTheme
/**

练习文本展示组件
在练习界面中展示用户需要朗读的文本内容，支持长文本滚动。
@param textContent 练习文本内容
@param modifier Modifier修饰符
 */
@Composable
fun ReadingPracticeComponent(
    textContent: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
        ) {
            Text(
                text = textContent,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp,
                    fontSize = 18.sp // 更大的字体便于阅读
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}
/**

预览函数
 */
@Preview(showBackground = true)
@Composable
fun ReadingPracticeComponentPreview() {
    AppTheme {
        ReadingPracticeComponent(
            textContent = "I believe my experience and skills make me well-suited for this position. In my previous role, I successfully led a team that increased productivity by twenty percent. I'm particularly interested in your company because of its innovative approach to problem-solving and strong commitment to sustainability.",
            modifier = Modifier.fillMaxWidth()
        )
    }
}