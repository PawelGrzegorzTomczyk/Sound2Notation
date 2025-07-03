package com.example.sound2notation.data.repository

import android.content.Context
import android.net.Uri
import com.example.sound2notation.data.model.LoginRequest
import com.example.sound2notation.data.model.LoginResponse
import com.example.sound2notation.data.model.MyFilesResponse
import com.example.sound2notation.data.model.ProfileResponse
import com.example.sound2notation.data.model.RegisterRequest
import com.example.sound2notation.data.model.RegisterResponse
import com.example.sound2notation.data.model.ResultResponse
import com.example.sound2notation.data.model.TestResponse
import com.example.sound2notation.data.model.UploadResponse
import com.example.sound2notation.data.network.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null, val errorBody: String? = null) : ApiResult<Nothing>()
    data class NetworkError(val exception: IOException) : ApiResult<Nothing>()
    data class UnknownError(val exception: Throwable) : ApiResult<Nothing>()
    data class Processing(val message: String) : ApiResult<Nothing>()
    object Processin : ApiResult<Nothing>()
}


class NetTaskRepository(
    private val api: ApiService
) {
    suspend fun checkConnection(): ApiResult<TestResponse> {
        return try {
            val response = api.test()
            val body = response.body()

            if (response.isSuccessful && body != null && body.status == "ok") {
                ApiResult.Success(body)
            } else {
                ApiResult.Error(
                    message = response.message(),
                    code = response.code(),
                    errorBody = response.errorBody()?.string()
                )
            }
        } catch (e: HttpException) {
            println("ERROR: HTTP Exception during connection check: ${e.code()} - ${e.message}")
            ApiResult.Error(
                message = e.message ?: "HTTP error",
                code = e.code(),
                errorBody = e.response()?.errorBody()?.string()
            )
        } catch (e: IOException) {
            println("ERROR: IO Exception (network issue) during connection check: ${e.message}")
            ApiResult.NetworkError(e)
        } catch (e: Exception) {
            println("ERROR: Unknown Exception during connection check: ${e.message}")
            ApiResult.UnknownError(e)
        }
    }

    suspend fun uploadFile(context: Context, uri: Uri, filename: String, mimeType: String): ApiResult<UploadResponse> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ApiResult.Error("Nie można otworzyć strumienia pliku z Uri.", null)

            // Czytamy bajty ze strumienia. Pamiętaj, że dla bardzo dużych plików,
            // lepszym rozwiązaniem byłoby strumieniowanie zamiast ładowania wszystkiego do pamięci.
            val requestBody = inputStream.readBytes().toRequestBody(mimeType.toMediaTypeOrNull())

            val body = MultipartBody.Part.createFormData("file", filename, requestBody)

            val response = api.uploadFile(body)
            if (response.isSuccessful) {
                val uploadResponse = response.body()
                uploadResponse?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("Pusta odpowiedź serwera po wgraniu pliku.", response.code())
            } else {
                ApiResult.Error("Wgrywanie pliku nie powiodło się", response.code(), response.errorBody()?.string())
            }
        } catch (e: IOException) {
            ApiResult.NetworkError(e)
        } catch (e: Exception) {
            ApiResult.UnknownError(e)
        }
    }

    suspend fun result(taskId: String): ApiResult<ResultResponse> {
        return try {
            val response = api.getResult(taskId)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                when (body.status) {
                    "done" -> ApiResult.Success(body)
                    "processing" -> ApiResult.Processing(body.message ?: "File not ready yet")
                    "error" -> ApiResult.Error(body.message ?: "Processing failed", response.code())
                    else -> ApiResult.Error("Unknown status: ${body.status}", response.code())
                }
            } else {
                // np. 404, 500 itp.
                val errorBody = response.errorBody()?.string()
                ApiResult.Error("Request failed", response.code(), errorBody)
            }
        } catch (e: IOException) {
            ApiResult.NetworkError(e)
        } catch (e: Exception) {
            ApiResult.UnknownError(e)
        }
    }

    suspend fun register(login: String, password: String): ApiResult<RegisterResponse> {
        return try {
            val response = api.register(RegisterRequest(login, password))
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResult.Success(it)
                } ?: ApiResult.Error("Empty body", response.code())
            } else {
                ApiResult.Error("Register failed", response.code(), response.errorBody()?.string())
            }
        } catch (e: IOException) {
            ApiResult.NetworkError(e)
        } catch (e: Exception) {
            ApiResult.UnknownError(e)
        }
    }

    suspend fun login(login: String, password: String): ApiResult<LoginResponse> {
        return try {
            val request = LoginRequest(login, password)
            val response = api.login(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResult.Success(it)
                } ?: ApiResult.Error("Empty body", response.code())
            } else {
                ApiResult.Error("Login failed", response.code(), response.errorBody()?.string())
            }
        } catch (e: IOException) {
            ApiResult.NetworkError(e)
        } catch (e: Exception) {
            ApiResult.UnknownError(e)
        }
    }

    suspend fun profile(): ApiResult<ProfileResponse> {
        return try {
            val response = api.profile()
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResult.Success(it)
                } ?: ApiResult.Error("Empty body", response.code())
            } else {
                ApiResult.Error("Profile request failed", response.code(), response.errorBody()?.string())
            }
        } catch (e: IOException) {
            ApiResult.NetworkError(e)
        } catch (e: Exception) {
            ApiResult.UnknownError(e)
        }
    }

    suspend fun myFiles(): ApiResult<MyFilesResponse> {
        return try {
            val response = api.myFiles()
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResult.Success(it)
                } ?: ApiResult.Error("Empty body", response.code())
            } else {
                ApiResult.Error("MyFiles request failed", response.code(), response.errorBody()?.string())
            }
        } catch (e: IOException) {
            ApiResult.NetworkError(e)
        } catch (e: Exception) {
            ApiResult.UnknownError(e)
        }
    }
}