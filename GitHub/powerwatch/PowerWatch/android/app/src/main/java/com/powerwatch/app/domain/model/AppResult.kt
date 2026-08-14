package com.powerwatch.app.domain.model

/** A minimal, explicit success/failure wrapper used across the domain and UI layers. */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Failure(val error: AppError) : AppResult<Nothing>()
}

/** Categorized errors so the UI can show the right message without parsing strings. */
sealed class AppError(val message: String) {
    data object NoConnection : AppError("No internet connection. Check your network and try again.")
    data object Timeout : AppError("The request timed out. Please try again.")
    data class NotVerified(val reason: String) : AppError(reason)
    data class Validation(val reason: String) : AppError(reason)
    data class Server(val reason: String) : AppError(reason)
    data class Unknown(val reason: String) : AppError(reason)
}
