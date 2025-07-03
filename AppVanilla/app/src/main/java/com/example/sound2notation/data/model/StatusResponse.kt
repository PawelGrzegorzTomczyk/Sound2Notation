package com.example.sound2notation.data.model

data class RegisterResponse(
    val message: String? = null,
    val error: String? = null
)

data class LoginResponse(
    val message: String? = null,
    val error: String? = null
)

data class ProfileResponse(
    val message: String? = null,
    val error: String? = null
)

data class MyFilesResponse(
    val files: List<FileInfo> = emptyList(),
    val error: String? = null
)

data class TestResponse(
    val status: String
)

data class UploadResponse(
    val status: String? = null,
    val task_id: String? = null,
    val error: String? = null
)

data class ResultResponse(
    val status: String? = null,
    val error: String? = null,
    val xml_file: String? = null,
    val message: String? = null
)
