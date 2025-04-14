package com.example.speechmaster.ui.components.practice

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R
import com.example.speechmaster.ui.theme.AppTheme
import com.example.speechmaster.utils.audio.TextToSpeechWrapper
import com.example.speechmaster.utils.audio.TTSState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@Composable
fun ReadingTTS(
    textContent: String,
    textToSpeechWrapper: TextToSpeechWrapper,
    modifier: Modifier = Modifier,
) {
    var ttsState by remember { mutableStateOf<TTSState>(TTSState.Ready) }
    var isPlaying by remember { mutableStateOf(false) }

    // Collect TTS state changes
    LaunchedEffect(textToSpeechWrapper) {
        textToSpeechWrapper.ttsState.collect { state ->
            ttsState = state
            isPlaying = state is TTSState.Playing
        }
    }

    // Animation for the icon when playing
    val infiniteTransition = rememberInfiniteTransition(label = "TTS Animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TTS Scale Animation"
    )

    Button(
        onClick = {
            if (isPlaying) {
                textToSpeechWrapper.stop()
            } else {
                textToSpeechWrapper.speak(textContent)
            }
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "TTS Icon Animation"
        ) { playing ->
            Icon(
                imageVector = if (playing) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (playing) 
                    stringResource(R.string.practice_stop_recording)
                else 
                    stringResource(R.string.text_to_speech),
                modifier = Modifier
                    .size(24.dp)
                    .then(if (playing) Modifier.scale(scale) else Modifier),
                tint = LocalContentColor.current.copy(alpha = 0.7f)
            )
        }
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            text = if (isPlaying) 
                stringResource(R.string.practice_stop_recording)
            else 
                stringResource(R.string.listen_to_reference),
            color = LocalContentColor.current.copy(alpha = 0.9f)
        )
    }
}

/**
 * 预览专用的 TextToSpeechWrapper 模拟类
 * 用于在 Compose 预览中模拟 TTS 功能
 */
class PreviewTextToSpeechWrapper : TextToSpeechWrapper {
    override val ttsState: Flow<TTSState> = flowOf(TTSState.Ready)
    
    override fun speak(text: String) {
        // No-op for preview
    }
    
    override fun stop() {
        // No-op for preview
    }
    
    override fun release() {
        // No-op for preview
    }
}

@Preview
@Composable
private fun ReadingTTSPreview() {
    AppTheme {
        ReadingTTS(
            textContent = "I believe my experience and skills make me well-suited for this position. In my previous role, I successfully led a team that increased productivity by twenty percent. I'm particularly interested in your company because of its innovative approach to problem-solving and strong commitment to sustainability.",
            textToSpeechWrapper = PreviewTextToSpeechWrapper(),
            modifier = Modifier
        )
    }
}


