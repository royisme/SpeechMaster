package com.example.speechmaster.ui.components.practice

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.* // Import animation specifics
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape // Import RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import specific icons needed
import androidx.compose.material.icons.outlined.Refresh // Import outlined Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // Import clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color // Import Color for placeholder
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R
import com.example.speechmaster.common.enums.RecordingState
import com.example.speechmaster.ui.theme.AppTheme
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PracticeRecordComponent(
    modifier: Modifier = Modifier,
    recordingState: RecordingState,
    durationMillis: Long,
    normalizedAmplitude: Float,
    isPlayingAudio: Boolean = false,
    isAnalyzing: Boolean = false,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit,
    onPlayClick: () -> Unit,
    onResetClick: () -> Unit,
    onSubmitClick: () -> Unit,
) {
    // Apply root styling: rounded top corners and background
    Surface(
        modifier = modifier
            .fillMaxWidth() // Ensure it takes full width passed from parent
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) // Rounded top corners
            .background(MaterialTheme.colorScheme.surfaceContainerLow), // M3 background color
        // tonalElevation = 1.dp // Elevation might not be needed if background is distinct
        color = MaterialTheme.colorScheme.surfaceContainerLow // Set surface color explicitly
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the Surface
                .padding(vertical = 16.dp, horizontal = 16.dp), // Adjusted padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Space out elements vertically
        ) {
            // Optional Visualizer Placeholder (Visible only during recording)
            AnimatedVisibility(
                visible = recordingState == RecordingState.RECORDING,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                AudioVisualizer(
                    // Replace with actual state collection or parameter
                    normalizedAmplitude = normalizedAmplitude,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp) // Adjust height as needed
                        .padding(vertical = 8.dp),
                    barColor = MaterialTheme.colorScheme.primary // Or desired color
                )
            }

            // Timer Display
            AnimatedContent(
                targetState = formatDuration(durationMillis),
                transitionSpec = { fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200)) },
                label = "Timer Animation"
            ) { formattedTime ->
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayMedium, // Or displaySmall if too large
                    color = if (recordingState == RecordingState.RECORDING)
                        MaterialTheme.colorScheme.primary // Use primary color for active recording timer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp) // Add padding around timer
                )
            }

            // State-dependent Controls
            Box(modifier = Modifier.height(IntrinsicSize.Min)) { // Box to manage height of controls
                when (recordingState) {
                    RecordingState.PREPARED -> {
                        IdleStateControls(onRecordClick = onRecordClick)
                    }
                    RecordingState.RECORDING -> {
                        RecordingStateControls(onStopClick = onStopClick)
                    }
                    RecordingState.STOPPED -> {
                        RecordedStateControls(
                            isPlayingAudio = isPlayingAudio, // Pass isPlaying if needed for Play button state
                            isAnalyzing = isAnalyzing,
                            onPlayClick = onPlayClick,
                            onResetClick = onResetClick,
                            onSubmitClick = onSubmitClick
                        )
                    }
                    RecordingState.PAUSED -> {
                        // Optional: Add controls for paused state if needed
                        // For now, might show similar controls to STOPPED or a Resume button
                        Text("Paused", style = MaterialTheme.typography.bodyMedium) // Placeholder
                    }
                }
            }
        }
    }
}

// --- Idle State Controls ---
@Composable
private fun IdleStateControls(onRecordClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight() // Center vertically
    ) {
        // Large central record button (Mockup 3)
        FilledIconButton(
            onClick = onRecordClick,
            modifier = Modifier.size(80.dp), // Large button size
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = stringResource(R.string.practice_start_recording),
                modifier = Modifier.size(36.dp) // Larger icon
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.practice_tap_to_start_recording),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- Recording State Controls ---
@Composable
private fun RecordingStateControls(onStopClick: () -> Unit) {
    // Animation for pulsing effect
    val infiniteTransition = rememberInfiniteTransition(label = "Recording Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Recording Scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight() // Center vertically
    ) {
        // Central recording button (Mockup 1 style)
        FilledIconButton(
            onClick = onStopClick, // Stop recording when clicked
            modifier = Modifier
                .size(80.dp)
                .scale(scale), // Apply pulsing scale animation
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                // Use primary or error color to indicate active recording
                containerColor = MaterialTheme.colorScheme.primary, // Or .error
                contentColor = MaterialTheme.colorScheme.onPrimary // Or .onError
            )
        ) {
            Icon(
                // Using Mic icon as per Mockup 1, even though it stops recording
                imageVector = Icons.Default.Mic,
                contentDescription = stringResource(R.string.practice_stop_recording),
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.practice_recording_in_progress),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary // Match button color
        )
    }
}

// --- Recorded State Controls ---
@Composable
private fun RecordedStateControls(
    isPlayingAudio: Boolean, // Keep if visual feedback needed for play button
    isAnalyzing: Boolean,
    onPlayClick: () -> Unit,
    onResetClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(), // Takes available height
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Center controls vertically
    ) {
        FilledIconButton(
            onClick = onPlayClick,
            enabled = !isAnalyzing, // Disable while analyzing
            modifier = Modifier.size(72.dp), // Slightly smaller than record button
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            Icon(
                // Conditionally choose icon based on isPlayingAudio
                imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = stringResource(
                    // Conditionally choose content description
                    if (isPlayingAudio) R.string.practice_pause_playback else R.string.practice_play_recording
                ),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp)) // Increased spacing

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp), // Adjust spacing as needed
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onResetClick,
                enabled = !isAnalyzing,
                shape = MaterialTheme.shapes.medium, // Or small/large
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.weight(1f) // <-- Added weight
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh, // Use outlined icon
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.practice_record_again))
            }

            // Submit Button (Filled style)
            Button(
                onClick = onSubmitClick,
                enabled = !isAnalyzing,
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.weight(1f) // <-- Added weight

            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check, // Checkmark icon
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.practice_submit_for_analysis))
            }
        }
    }
}


// --- formatDuration function remains the same ---
fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}


// --- Previews ---

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0, heightDp = 250)
@Composable
fun PracticeRecordComponentIdlePreview() {
    AppTheme {
        val sampleAmplitude = 0.7f
        PracticeRecordComponent( // Wrap in Surface for preview background
            recordingState = RecordingState.PREPARED,
            normalizedAmplitude = sampleAmplitude,
            durationMillis = 0L, onRecordClick = {}, onStopClick = {}, onPlayClick = {}, onResetClick = {}, onSubmitClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0, heightDp = 250)
@Composable
fun PracticeRecordComponentRecordingPreview() {
    AppTheme {
        val sampleAmplitude = 0.7f
        PracticeRecordComponent(
            recordingState = RecordingState.RECORDING,
            normalizedAmplitude = sampleAmplitude,
            durationMillis = 37000L, onRecordClick = {}, onStopClick = {}, onPlayClick = {}, onResetClick = {}, onSubmitClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0, heightDp = 250)
@Composable
fun PracticeRecordComponentRecordedPreview() {
    AppTheme {
        val sampleAmplitude = 0.7f
        PracticeRecordComponent(
            recordingState = RecordingState.STOPPED,
            normalizedAmplitude = sampleAmplitude,
            durationMillis = 67000L, onRecordClick = {}, onStopClick = {}, onPlayClick = {}, onResetClick = {}, onSubmitClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0, heightDp = 250)
@Composable
fun PracticeRecordComponentAnalyzingPreview() {
    AppTheme {
        val sampleAmplitude = 0.7f

        PracticeRecordComponent(
            recordingState = RecordingState.STOPPED,
            durationMillis = 45000L,
            normalizedAmplitude = sampleAmplitude,
            isAnalyzing = true, // Show analyzing state
            onRecordClick = {}, onStopClick = {}, onPlayClick = {}, onResetClick = {}, onSubmitClick = {}
        )
    }
}