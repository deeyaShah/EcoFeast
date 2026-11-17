package com.college.ecofeast.ui.auth

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState

enum class FeedbackCategory {
    VALIDATION,
    AUTHENTICATION,
    NETWORK,
    SUCCESS,
    INFO
}

data class FeedbackMessage(
    val text: String,
    val category: FeedbackCategory = FeedbackCategory.VALIDATION
)

object AuthValidation {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email.trim())
    }

    fun validatePhoneNumber(phone: String): FeedbackMessage? {
        val cleaned = phone.trim().filter { it.isDigit() }
        return when {
            cleaned.isEmpty() -> FeedbackMessage(
                text = "Please enter your phone number.",
                category = FeedbackCategory.VALIDATION
            )
            cleaned.length < 10 -> FeedbackMessage(
                text = "Please enter a valid 10-digit phone number.",
                category = FeedbackCategory.VALIDATION
            )
            else -> null
        }
    }

    fun validateOtp(otp: String, expectedLength: Int = 6): FeedbackMessage? {
        val trimmed = otp.trim()
        return when {
            trimmed.isEmpty() -> FeedbackMessage(
                text = "Please enter the verification code.",
                category = FeedbackCategory.VALIDATION
            )
            trimmed.length < expectedLength -> FeedbackMessage(
                text = "Please enter all $expectedLength digits of the verification code.",
                category = FeedbackCategory.VALIDATION
            )
            else -> null
        }
    }

    fun validateProfileSetup(fullName: String, email: String): FeedbackMessage? {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()
        return when {
            trimmedName.isEmpty() -> FeedbackMessage(
                text = "Please enter your full name.",
                category = FeedbackCategory.VALIDATION
            )
            trimmedEmail.isEmpty() -> FeedbackMessage(
                text = "Please enter your email address.",
                category = FeedbackCategory.VALIDATION
            )
            !isValidEmail(trimmedEmail) -> FeedbackMessage(
                text = "Please enter a valid email address.",
                category = FeedbackCategory.VALIDATION
            )
            else -> null
        }
    }
}

suspend fun SnackbarHostState.showFeedback(
    feedback: FeedbackMessage
) {
    currentSnackbarData?.dismiss()
    showSnackbar(
        message = feedback.text,
        duration = SnackbarDuration.Short
    )
}

suspend fun SnackbarHostState.showFeedback(
    message: String,
    category: FeedbackCategory = FeedbackCategory.INFO
) {
    currentSnackbarData?.dismiss()
    showSnackbar(
        message = message,
        duration = SnackbarDuration.Short
    )
}
