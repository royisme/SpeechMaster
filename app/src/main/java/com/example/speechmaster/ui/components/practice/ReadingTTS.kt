package com.example.speechmaster.ui.components.practice

import android.util.Log
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
import androidx.compose.foundation.layout.size // Import size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // Change to IconButton
import androidx.compose.material3.IconButtonDefaults // Import IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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
    modifier: Modifier = Modifier, // Modifier is now applied to the IconButton
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
        targetValue = 1.1f, // Slightly reduced scale effect for smaller button
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing), // Slightly faster animation
            repeatMode = RepeatMode.Reverse
        ),
        label = "TTS Scale Animation"
    )

    // Use IconButton instead of Button
    IconButton(
        onClick = {
            if (isPlaying) {
                textToSpeechWrapper.stop()
            } else {
                Log.d("ReadingTTS", "TTS text: $textContent")
                textToSpeechWrapper.speak(textContent)
            }
        },
        modifier = modifier, // Apply modifier here
        colors = IconButtonDefaults.iconButtonColors(
            // Use appropriate M3 colors, e.g., tertiary for distinct action
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "TTS Icon Animation"
        ) { playing ->
            Icon(
                imageVector = if (playing) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (playing)
                    stringResource(R.string.stop_speech_text)
                else
                    stringResource(R.string.text_to_speech),
                modifier = Modifier
                    .size(24.dp) // Standard icon size
                    .then(if (playing) Modifier.scale(scale) else Modifier)
                // Content color is handled by IconButton colors
            )
        }
        // Remove Spacer and Text - IconButton only contains the icon
    }
}

// --- Preview remains the same ---
class PreviewTextToSpeechWrapper : TextToSpeechWrapper {
    override val ttsState: Flow<TTSState> = flowOf(TTSState.Ready)
    override fun speak(text: String) {}
    override fun stop() {}
    override fun release() {}
}

@Preview
@Composable
private fun ReadingTTSPreview() {
    AppTheme {
        ReadingTTS(
            textContent = "Sample text for preview.",
            textToSpeechWrapper = PreviewTextToSpeechWrapper(),
            modifier = Modifier // In real use, alignment modifier would be passed here
        )
    }
}