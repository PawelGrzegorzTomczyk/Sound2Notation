package com.example.sound2notation.data.model

data class RegisterRequest(
    val login: String,
    val password: String
)

data class LoginRequest(
    val login: String,
    val password: String
)