package com.example.speechmaster.ui.screens.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight // Import fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.Manifest
import androidx.compose.material3.Button
import com.example.speechmaster.R
import com.example.speechmaster.common.enums.RecordingState
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.components.practice.PracticeRecordComponent
import com.example.speechmaster.ui.components.practice.ReadingPracticeComponent
import com.example.speechmaster.ui.components.practice.PreviewTextToSpeechWrapper
// Removed ReadingTTS import as it's inside ReadingPracticeComponent now
import com.example.speechmaster.ui.navigation.navigateToPracticeResult
import com.example.speechmaster.ui.theme.AppTheme
import com.example.speechmaster.utils.audio.TextToSpeechWrapper
import com.example.speechmaster.utils.permissions.PermissionRequest
import com.example.speechmaster.ui.viewmodels.TopBarViewModel
import com.example.speechmaster.ui.state.TopBarState
import com.example.speechmaster.ui.state.BaseUiState
import com.example.speechmaster.ui.state.defaultTopBarState
import com.example.speechmaster.ui.state.get
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PracticeViewModel = hiltViewModel(),
    topBarViewModel: TopBarViewModel = hiltViewModel(),

) {
    // --- State collection and Effects remain the same ---
    val uiState by viewModel.uiState.collectAsState()
    val recordingState by viewModel.recordingState.collectAsState()
    val recordingDuration by viewModel.recordingDurationMillis.collectAsState()
    val isPlayingAudio by viewModel.isPlayingAudio.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    var shouldShowPermissionRequest by remember { mutableStateOf(false) }
    val practiceTitle = stringResource(R.string.practice_cards) // Corrected title resource
    val amplitude by viewModel.normalizedAmplitude.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToPracticeResult -> {
                    // Use the specific ID from the event
                    navController.navigateToPracticeResult(event.practiceId)
                }
                is NavigationEvent.RequestPermission -> {
                    shouldShowPermissionRequest = true
                }
                // Handle other events if needed
                else -> {}
            }
        }
    }

    // Update TopBar title when state is Success
    LaunchedEffect(uiState) {
        if (uiState is BaseUiState.Success) {
            topBarViewModel.updateTitle(practiceTitle)
            // Ensure back button is shown for this screen
            topBarViewModel.showBackButton(true)
            topBarViewModel.showMenuButton(false) // Usually no menu on detail screens
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            topBarViewModel.updateState(defaultTopBarState) // Reset to default on leaving
            viewModel.resetRecording() // Clean up recorder state
            viewModel.textToSpeechWrapper.stop() // Stop any TTS
        }
    }

    if (shouldShowPermissionRequest) {
        PermissionRequest(
            permission = Manifest.permission.RECORD_AUDIO,
            rationale = stringResource(R.string.record_permission_rationale),
            permissionNotAvailableContent = { /* ... existing permission denied content ... */ }
        ) {
            shouldShowPermissionRequest = false
            viewModel.startRecording() // Try starting again after permission granted
        }
    }


    Surface(
        modifier = modifier
            .fillMaxSize(), // Surface fills the Scaffold's content area
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is BaseUiState.Loading -> {
                LoadingView(Modifier.fillMaxSize())
            }
            is BaseUiState.Error -> {
                ErrorView(
                    message = stringResource(id = state.messageResId),
                    onRetry = { viewModel.retryLoading() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is BaseUiState.Success -> {
                // Use the modified PracticeContent layout
                PracticeContent(
                    textContent = (state.get<PracticeUiData>()?.textContent ?: ""),
                    recordingState = recordingState,
                    recordingDurationMillis = recordingDuration,
                    isPlayingAudio = isPlayingAudio,
                    isAnalyzing = isAnalyzing,
                    textToSpeechWrapper = viewModel.textToSpeechWrapper,
                    onRecordClick = {
                        if (viewModel.hasRecordAudioPermission()) {
                            viewModel.startRecording()
                        } else {
                            shouldShowPermissionRequest = true
                        }
                    },
                    normalizedAmplitude = amplitude,
                    onStopClick = { viewModel.stopRecording() },
                    onPlayClick = { viewModel.togglePlayback() },
                    onResetClick = { viewModel.resetRecording() },
                    onSubmitClick = { viewModel.submitForAnalysis() },
                    modifier = Modifier.fillMaxSize() // PracticeContent fills the Surface
                )
            }
        }
    }
}


@Composable
fun PracticeContent(
    modifier: Modifier = Modifier,
    textContent: String,
    recordingState: RecordingState,
    recordingDurationMillis: Long,
    isPlayingAudio: Boolean,
    isAnalyzing: Boolean,
    normalizedAmplitude: Float,
    textToSpeechWrapper: TextToSpeechWrapper,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit,
    onPlayClick: () -> Unit,
    onResetClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    // Main Column to structure the screen sections
    Column(
        modifier = modifier.padding(1.dp), // Apply padding adjustments here if needed at screen level
        horizontalAlignment = Alignment.CenterHorizontally
        // Removed verticalArrangement as we use weight and fixed height
    ) {
        // Text component taking remaining space
        ReadingPracticeComponent(
            textContent = textContent,
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp) // Fixed height for text
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp), // Added padding
            textToSpeechWrapper = textToSpeechWrapper
        )

        // Recorder component fixed at the bottom third
        PracticeRecordComponent(
            recordingState = recordingState,
            durationMillis = recordingDurationMillis,
            isPlayingAudio = isPlayingAudio,
            isAnalyzing = isAnalyzing,
            onRecordClick = onRecordClick,
            onStopClick = onStopClick,
            onPlayClick = onPlayClick,
            onResetClick = onResetClick,
            onSubmitClick = onSubmitClick,
            normalizedAmplitude = normalizedAmplitude ,
            modifier = Modifier
                .fillMaxWidth()
//                .fillMaxHeight(0.35f) // Adjust fraction as needed (e.g., 0.33f, 0.35f)
        )
    }
}

// --- Preview remains the same, showing the content structure ---
@Preview(showBackground = true)
@Composable
fun PracticeScreenPreview() {
    AppTheme {
        val sampleAmplitude = 0.7f

        PracticeContent(
            textContent = "I believe my experience and skills make me well-suited for this position...",
            recordingState = RecordingState.PREPARED,
            recordingDurationMillis = 0L,
            isPlayingAudio = false,
            isAnalyzing = false,
            normalizedAmplitude = sampleAmplitude,
            textToSpeechWrapper = PreviewTextToSpeechWrapper(),
            onRecordClick = {},
            onStopClick = {},
            onPlayClick = {},
            onResetClick = {},
            onSubmitClick = {}
        )
    }
}