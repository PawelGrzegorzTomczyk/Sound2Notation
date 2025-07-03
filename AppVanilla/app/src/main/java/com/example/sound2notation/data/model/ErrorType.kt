package com.example.sound2notation.data.model

enum class ErrorType(val possibleSolutions: List<String>) {
    NETWORK(
        listOf(
            "Check your internet connection (Wi-Fi, mobile data).",
            "Ensure airplane mode is off.",
            "Try again in a few moments; the issue might be temporary.",
            "DEV: Verify server availability."
        )
    ),
    SERVER(
        listOf(
            "Try again in a few moments. The server might be overloaded or undergoing maintenance.",
            "If the problem persists, please contact the application administrator."
        )
    ),
    CLIENT(
        listOf(
            "Ensure the data entered is correct.",
            "Refresh the application or try again later.",
            "If the problem persists, try reinstalling the application."
        )
    ),
    UNKNOWN(
        listOf(
            "An unexpected problem occurred.",
            "Try performing the operation again.",
            "If the problem persists, restart the application or your device.",
            "DEV: Review functions in viewModels/ServerConnectionViewModel.kt and data/repository/TaskRepository.kt"
        )
    )
}