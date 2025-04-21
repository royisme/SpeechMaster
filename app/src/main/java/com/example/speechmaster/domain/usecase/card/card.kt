package com.example.speechmaster.domain.usecase.card

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import com.example.speechmaster.domain.model.CourseCardRules
import java.util.LinkedList // Using LinkedList again as it's efficient for remove operations during merge

class SplitTextIntoCardsUseCase @Inject constructor() {

    companion object {
        private const val TAG = "SplitTextUseCase"
    }

    suspend operator fun invoke(rawText: String): Result<List<String>> = withContext(Dispatchers.Default) {
        Timber.tag(TAG).d("Starting enhanced text splitting...")
        if (rawText.isBlank()) {
            Timber.tag(TAG).w("Input text is blank.")
            return@withContext Result.success(emptyList())
        }

        try {
            // 1. Initial Split (Paragraphs -> Sentences)
            val initialSegments = splitByParagraphsAndSentences(rawText)
            Timber.tag(TAG).d("Initial split: ${initialSegments.size} segments.")

            // 2. Enforce MAX Length (Secondary Split for Long Segments)
            // This list now contains segments, none exceeding MAX length, but some might be too short.
            val maxLengthProcessedSegments = enforceMaxLength(initialSegments)
            Timber.tag(TAG).d("After MAX length enforcement: ${maxLengthProcessedSegments.size} segments.")

            // 3. Enforce MIN Length (Merge Short Segments)
            val finalCards = mergeShortSegments(maxLengthProcessedSegments)
            Timber.tag(TAG).d("After MIN length enforcement (merging): ${finalCards.size} final cards.")

            Result.success(finalCards)

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error during text splitting")
            Result.failure(e)
        }
    }

    // Step 1: Initial split logic (Remains the same)
    private fun splitByParagraphsAndSentences(text: String): List<String> {
        val paragraphRegex = Regex("(\\r?\\n){2,}")
        val sentenceRegex = Regex("(?<=[.?!])\\s+")

        return text.split(paragraphRegex)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .flatMap { paragraph ->
                val sentences = paragraph.split(sentenceRegex)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                // Ensure paragraph isn't lost if sentence split fails or results in empty list
                if (sentences.isNotEmpty()) sentences else (if (paragraph.isNotEmpty()) listOf(paragraph) else emptyList())
            }
    }

    // Step 2: Enforce MAX length by splitting longer segments
    private fun enforceMaxLength(segments: List<String>): List<String> {
        val outputList = LinkedList<String>() // Use LinkedList for efficient add operations during recursive calls
        segments.forEach { segment ->
            splitSegmentIfNeeded(segment.trim(), outputList)
        }
        return outputList
    }

    // Recursive helper for enforceMaxLength
    private fun splitSegmentIfNeeded(segment: String, outputList: MutableList<String>) {
        if (segment.isBlank()) return

        if (segment.length <= CourseCardRules.MAX_CARD_CONTENT_LENGTH) {
            // Already within max length, add it (will be checked for MIN length later)
            outputList.add(segment)
        } else {
            // Segment is too long, perform secondary split
            Timber.tag(TAG).v("Segment exceeds MAX (${segment.length}), splitting: '${segment.take(50)}...'")
            var remainingSegment = segment
            while (remainingSegment.length > CourseCardRules.MAX_CARD_CONTENT_LENGTH) {
                var splitIndex = findBestSplitPoint(remainingSegment, CourseCardRules.TARGET_SPLIT_LENGTH)
                if (splitIndex <= 0) {
                    splitIndex = findBestSplitPoint(remainingSegment, CourseCardRules.MAX_CARD_CONTENT_LENGTH)
                }
                if (splitIndex <= 0) {
                    splitIndex = CourseCardRules.MAX_CARD_CONTENT_LENGTH
                    Timber.tag(TAG).w("Force splitting long segment at $splitIndex")
                }

                val part1 = remainingSegment.substring(0, splitIndex).trim()
                val part2 = remainingSegment.substring(splitIndex).trim()

                if (part1.isNotEmpty()) {
                    // Add the valid first part directly (it shouldn't exceed MAX)
                    outputList.add(part1)
                    Timber.tag(TAG).v(" -> Added split part: '${part1.take(30)}...' (len: ${part1.length})")
                }
                remainingSegment = part2
            }
            // Add the final remaining part (must be <= MAX length now)
            if (remainingSegment.isNotEmpty()) {
                outputList.add(remainingSegment)
                Timber.tag(TAG).v(" -> Added remaining part: '${remainingSegment.take(30)}...' (len: ${remainingSegment.length})")
            }
        }
    }

    // Helper to find split point (Remains the same)
    private fun findBestSplitPoint(text: String, limit: Int): Int {
        if (text.length <= limit) return -1
        val searchText = text.substring(0, limit)
        var splitPoint = searchText.lastIndexOfAny(charArrayOf('.', '?', '!'))
        if (splitPoint > 0 && splitPoint > limit / 2) return splitPoint + 1
        splitPoint = searchText.lastIndexOfAny(charArrayOf(',', ';'))
        if (splitPoint > 0 && splitPoint > limit / 2) return splitPoint + 1
        splitPoint = searchText.lastIndexOf(' ')
        if (splitPoint > 0) return splitPoint + 1
        Timber.tag(TAG).v("No preferred split point found before limit $limit in text: '${searchText.takeLast(50)}'")
        return -1
    }

    // Step 3: Merge segments shorter than MIN length
    private fun mergeShortSegments(segments: List<String>): List<String> {
        if (segments.size < 2) {
            // Cannot merge if less than 2 segments exist
            return segments.filter { it.length >= CourseCardRules.MIN_CARD_CONTENT_LENGTH } // Optionally filter shorts if no merge possible
                .ifEmpty { segments } // Return original if filter results in empty
        }

        val mergedList = LinkedList(segments) // Work on a mutable copy
        val iterator = mergedList.listIterator()

        // Need to track the index of the 'previous' segment accurately
        var prevIndex = -1
        var currentVal = "" // Keep track of value at current iterator position

        while (iterator.hasNext()) {
            val currentIndex = iterator.nextIndex() // Get index *before* calling next()
            currentVal = iterator.next()

            if (prevIndex != -1 && currentVal.length < CourseCardRules.MIN_CARD_CONTENT_LENGTH) {
                val prevVal = mergedList[prevIndex] // Get previous value directly by index
                val mergedText = prevVal + " " + currentVal

                if (mergedText.length <= CourseCardRules.MAX_CARD_CONTENT_LENGTH) {
                    // Merge is possible and within limits
                    Timber.tag(TAG).v("Merging short segment (idx $currentIndex, len ${currentVal.length}) onto previous (idx $prevIndex, len ${prevVal.length}). New len: ${mergedText.length}")

                    // 1. Update the previous element directly in the list
                    mergedList[prevIndex] = mergedText

                    // 2. Remove the current element using the iterator
                    iterator.remove() // remove() removes the element last returned by next()

                    while(iterator.hasPrevious() && iterator.previousIndex() != prevIndex) {
                        iterator.previous()
                    }
                    continue
                } else {
                    // Cannot merge due to length constraint
                    Timber.tag(TAG).v("Cannot merge short segment (idx $currentIndex, len ${currentVal.length}), merged length (${mergedText.length}) would exceed MAX.")
                    // The short segment remains, update prevIndex to point to it
                    prevIndex = currentIndex
                }
            } else {
                // Current segment is not short OR it's the very first segment
                prevIndex = currentIndex // Update prevIndex to current
            }
        }
        return mergedList
    }
}