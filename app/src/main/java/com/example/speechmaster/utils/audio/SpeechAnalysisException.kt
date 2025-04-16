package com.example.speechmaster.utils.audio

import com.microsoft.cognitiveservices.speech.CancellationErrorCode

sealed class SpeechAnalysisException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** Error related to processing the input audio file (format, reading, etc.) */
    class AudioFileError(message: String, cause: Throwable? = null) : SpeechAnalysisException(message, cause)
    
    /** Error related to parsing the result from the Azure service */
    class ResultParsingError(message: String, cause: Throwable? = null) : SpeechAnalysisException(message, cause)
    
    /** Error reported by the Azure Speech Service (network, auth, quota, internal service error) */
    class ServiceError(
        message: String,
        cause: Throwable? = null,
        val errorCode: CancellationErrorCode? = null // Include Azure error code if available
    ) : SpeechAnalysisException(message, cause)
    
    /** Recognition completed but no speech matched */
    class NoMatch(message: String) : SpeechAnalysisException(message)
    
    /** The analysis operation was cancelled (e.g., by user) */
    class OperationCancelled(message: String) : SpeechAnalysisException(message)
} 