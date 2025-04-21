package com.example.speechmaster.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case responsible for splitting a raw text input into a list of potential card contents.
 * It prioritizes paragraph breaks (double newlines) and then sentence breaks.
 */
class SplitTextIntoCardsUseCase @Inject constructor() {

    companion object {
        private const val MIN_SENTENCE_LENGTH = 5 // Avoid splitting extremely short sentences if desired
    }

    /**
     * Executes the text splitting logic.
     *
     * @param rawText The raw text pasted by the user.
     * @return A Result containing a list of strings (potential card contents) or an exception.
     */
    suspend operator fun invoke(rawText: String): Result<List<String>> = withContext(Dispatchers.Default) {
        Timber.d("Starting text splitting...")
        if (rawText.isBlank()) {
            Timber.w("Input text is blank, returning empty list.")
            return@withContext Result.success(emptyList())
        }

        try {
            // 1. Split by paragraphs (double or more newlines)
            // Regex explanation: Matches two or more consecutive newline characters (\n or \r\n)
            val paragraphRegex = Regex("(\\r?\\n){2,}")
            val paragraphs = rawText.split(paragraphRegex)
                .map { it.trim() } // Trim whitespace from each paragraph
                .filter { it.isNotEmpty() } // Remove empty strings resulting from multiple splits

            Timber.d("Split into ${paragraphs.size} paragraphs.")

            val potentialCards = mutableListOf<String>()

            // 2. Process each paragraph
            for (paragraph in paragraphs) {
                // Simple check: if paragraph is reasonably short, treat it as one card
                // You might adjust this length based on testing
                if (paragraph.length < 150 && !paragraph.containsSentenceEndingPunctuation()) {
                    potentialCards.add(paragraph)
                    Timber.v("Adding short paragraph as single card: '${paragraph.take(30)}...'")
                    continue
                }

                // 3. Split long paragraphs by sentences
                // Regex explanation:
                // - (?<=[.?!]) : Positive lookbehind - asserts that the match is preceded by a period, question mark, or exclamation mark.
                // - \\s+      : Matches one or more whitespace characters (ensures we split *after* the punctuation and space).
                // - Negative lookahead `(?!\\s*[a-z])` could be added to handle abbreviations like "Mr. Smith", but makes it complex.
                //   Keeping it simpler for now, user editing is the safety net.
                val sentenceRegex = Regex("(?<=[.?!])\\s+") // Split after sentence-ending punctuation followed by space
                val sentences = paragraph.split(sentenceRegex)
                    .map { it.trim() }
                    .filter { it.length >= MIN_SENTENCE_LENGTH } // Filter out very short fragments

                if (sentences.isNotEmpty()) {
                    potentialCards.addAll(sentences)
                    Timber.v("Split paragraph into ${sentences.size} sentences.")
                } else if (paragraph.isNotEmpty()) {
                    // If splitting didn't work but paragraph wasn't empty, add the whole paragraph
                    potentialCards.add(paragraph)
                    Timber.v("Paragraph not split by sentence, adding whole: '${paragraph.take(30)}...'")
                }
            }

            Timber.d("Splitting finished. Generated ${potentialCards.size} potential cards.")
            Result.success(potentialCards)

        } catch (e: Exception) {
            Timber.e(e, "Error during text splitting")
            Result.failure(e)
        }
    }

    // Helper to check if a string likely contains sentence-ending punctuation
    private fun String.containsSentenceEndingPunctuation(): Boolean {
        return this.contains('.') || this.contains('?') || this.contains('!')
    }
}