package com.example.sound2notation.viewModels

import android.R
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sound2notation.data.model.FileInfo
import com.example.sound2notation.data.model.MyFilesResponse
import com.example.sound2notation.data.model.ResultResponse
import com.example.sound2notation.data.repository.ApiResult
import com.example.sound2notation.data.repository.NetTaskRepository
import com.example.sound2notation.di.NetworkModule.CONNTYPE
import com.example.sound2notation.di.NetworkModule.IP
import com.example.sound2notation.di.NetworkModule.PORT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import kotlin.uuid.Uuid

class HomeViewModel(
    private val repository: NetTaskRepository,
    private val app: Application
) : AndroidViewModel(app) {
    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    private val _uploaded = MutableStateFlow(false)
    val uploaded: StateFlow<Boolean> = _uploaded

    private val _resultState = MutableStateFlow<ApiResult<ResultResponse>?>(null)
    val resultState: StateFlow<ApiResult<ResultResponse>?> = _resultState

    private val _taskId = MutableStateFlow("")
    val taskId: StateFlow<String> = _taskId

    private val _myFilesState = MutableStateFlow<ApiResult<MyFilesResponse>?>(null)
    val myFilesState: StateFlow<ApiResult<MyFilesResponse>?> = _myFilesState

    init {
        viewModelScope.launch {
            val connected = checkConnection()
            _connected.value = connected
        }
        tryToGetMyFiles()
    }

    suspend fun checkConnection(): Boolean {
        return when(val result = repository.checkConnection()) {
            is ApiResult.Success -> true
            else -> false
        }
    }

    fun reconnect() {
        viewModelScope.launch {
            val connected = checkConnection()
            _connected.value = connected
        }
        tryToGetMyFiles()
    }

    fun uploadFileFromUri(context: Context, uri: Uri, filename: String, mimeType: String) {
        _resultState.value = ApiResult.Processin
        viewModelScope.launch {
            if (!checkConnection()) {
                Log.w("Profile", "Brak połączenia – przerwano")
                _connected.value = false
                return@launch
            }

            val result = repository.uploadFile(context, uri, filename, mimeType)
            when (result) {
                is ApiResult.Success -> {
                    Log.d("Upload", "Sukces: ${result.data}")
                    _uploaded.value = true
                    _taskId.value = result.data.task_id ?: ""
                }
                is ApiResult.Error -> {
                    Log.e("Upload", "Błąd: ${result.message}, kod: ${result.code}")
                    _uploaded.value = false
                }
                is ApiResult.NetworkError -> {
                    Log.e("Upload", "Błąd sieciowy: ${result.exception.message}")
                    _uploaded.value = false

                }
                is ApiResult.UnknownError -> {
                    Log.e("Upload", "Nieznany błąd: ${result.exception.message}")
                    _uploaded.value = false
                }
                else -> {
                    Log.e("Upload", "nieznany błąd")
                    _uploaded.value = false

                }
            }
        }
    }

    fun tryToGetResult(navController: NavController) {
        if (taskId.value.isNotEmpty()) {
            viewModelScope.launch {
                if (!checkConnection()) {
                    Log.w("Profile", "Brak połączenia – przerwano")
                    _connected.value = false
                    return@launch
                }

                _resultState.value = null // reset przed nowym requestem
                val result = repository.result(taskId.value)
                _resultState.value = result
                if (result is ApiResult.Success) {
                    val url = generateHtmlQuery(path = result.data.xml_file.toString())
                    Log.w("query: ", url)
                    goToNotesScreen(navController, url)
                }
            }
        }
    }

    fun tryToGetMyFiles() {
        viewModelScope.launch {
            _myFilesState.value = null // reset, jeśli chcesz
            val result = repository.myFiles()
            _myFilesState.value = result
        }
    }

    fun test(navController: NavController, uuid: String) {
        val url = generateHtmlQuery(uuid = uuid)
        Log.e("html: ", url)
        goToNotesScreen(navController, url)
    }

    fun goToNotesScreen(navController: NavController, url: String) {
        val encodedUrl = Uri.encode(url)
        navController.navigate("notes_screen/$encodedUrl")
    }

    fun generateHtmlQuery(uuid: String = "", path: String = "") : String{
        if(!uuid.isEmpty()){ // podanie uuid oznacza że mamy do czynienia z zalogowanym
            return CONNTYPE + "://" + IP + ":" + PORT + "/static/displayScore.html?scoreUrl=http://" + IP + ":" + PORT + "/xmlFiles/" + uuid + ".musicxml"
        }
        if(!path.isEmpty()){
            return  CONNTYPE + "://" + IP + ":" + PORT + "/static/displayScore.html?scoreUrl=http://" + IP + ":" + PORT + "/" + path
        }
        return ""
    }
}