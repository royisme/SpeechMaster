package com.example.speechmaster.ui.components.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box // Import Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding // Import padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment // Import Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speechmaster.ui.theme.AppTheme
import com.example.speechmaster.utils.audio.TextToSpeechWrapper

@Composable
fun ReadingPracticeComponent(
    textContent: String,
    modifier: Modifier = Modifier,
    textToSpeechWrapper: TextToSpeechWrapper,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        // Change Column to Box to allow overlapping/corner placement
        Box(
            modifier = Modifier
                .fillMaxSize() // Box fills the card
                .height(240.dp) // Fixed height for text
                .padding(16.dp) // Apply padding once to the Box
        ) {
            // Text takes up the main area, scrollable
            Text(
                text = textContent,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    fontSize = 18.sp
                ),
                modifier = Modifier
                    .fillMaxSize() // Text fills the Box (allowing button overlap)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 48.dp) // Add padding at bottom to prevent overlap with button
            )

            // Place ReadingTTS (now an IconButton) at the bottom end
            ReadingTTS(
                textContent = textContent,
                textToSpeechWrapper = textToSpeechWrapper,
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Align to bottom end corner
                // Add specific padding for the button if needed, e.g., .padding(4.dp)
            )
        }
    }
}

// --- Preview needs update if TTS component changed significantly, but basic preview is fine ---
@Preview(showBackground = true)
@Composable
fun ReadingPracticeComponentPreview() {
    AppTheme {
        ReadingPracticeComponent(
            textContent = "Our quarterly results exceed expectations with a 15% increase in revenue compared to the same period last year. This growth is primarily attributed to the successful launch of our new product line and expanded market reach in the Asia-Pacific region. Several key initiatives contributed to this success.",
            textToSpeechWrapper = PreviewTextToSpeechWrapper(),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Give it a fixed height for preview
        )
    }
}