package com.example.sound2notation.viewModels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.example.sound2notation.data.repository.ApiResult
import com.example.sound2notation.data.repository.NetTaskRepository
import com.example.sound2notation.di.NetworkModule
import com.example.sound2notation.di.NetworkModule.clearCookies

class LoginViewModel(
    private val repository: NetTaskRepository,
    private val app: Application
) : AndroidViewModel(app) {
    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    private val _registerSuccess = MutableStateFlow(-1) // -1 - nie próbowano, 0 - nie udało się, 1 - udało się
    val registerSuccess: StateFlow<Int> = _registerSuccess
    private val _registerError = MutableStateFlow("")
    val registerError: StateFlow<String> = _registerError

    private val _isLoggedIn = MutableStateFlow(-1)
    val isLoggedIn: StateFlow<Int> = _isLoggedIn
    val _loggedError = MutableStateFlow("")
    val loggedError: StateFlow<String> = _loggedError


    init {
        viewModelScope.launch {
            val connected = checkConnection()
            _connected.value = connected
            profile()

        }
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
    }

    fun register(login: String, password: String) {
        viewModelScope.launch {
            if (!checkConnection()) {
                Log.w("Register", "Brak połączenia – przerwano")
                _connected.value = false
                return@launch
            }

            _registerSuccess.value = -1

            when(val result = repository.register(login, password)) {
                is ApiResult.Success -> {
                    Log.d("Register", "Sukces: ${result.data}")
                    _registerSuccess.value = 1
                }
                is ApiResult.Error -> {
                    if(result.code == 409){
                        _registerError.value = "Użytkownik o podanym loginie już istnieje"
                    }
                    else if(result.code == 400){
                        _registerError.value = "Błędne dane"
                    }
                    Log.e("Register", "Błąd: ${result.message}, code: ${result.code}, rest: ${result.errorBody}")
                    _registerSuccess.value = 0
                }
                is ApiResult.NetworkError -> {
                    Log.e("Register", "Błąd sieciowy: ${result.exception.message}")
                    _registerSuccess.value = 0
                }
                else -> {
                    _registerSuccess.value = 0
                }
            }
        }
    }

    fun login(login: String, password: String) {
        viewModelScope.launch {
            if (!checkConnection()) {
                Log.w("Login", "Brak połączenia – przerwano")
                _connected.value = false
                return@launch
            }

            when(val result = repository.login(login, password)) {
                is ApiResult.Success -> {
                    // Tutaj nie masz nagłówków, więc zapisz coś w prefs jeśli trzeba
                    _registerSuccess.value = -1
                    Log.d("Login", "Zalogowano pomyślnie")
                    _isLoggedIn.value = 1

                }
                is ApiResult.Error -> {
                    Log.e("Login", "Błąd logowania: ${result.message}")
                    if(result.code == 401){
                        _loggedError.value = "Nieprawidłowy login lub hasło"
                    }
                    else if(result.code == 400){
                        _loggedError.value = "Błędne dane"
                    }
                    _isLoggedIn.value = 0
                }
                is ApiResult.NetworkError -> {
                    Log.e("Login", "Błąd sieciowy: ${result.exception.message}")
                    _isLoggedIn.value = 0
                }
                else -> {
                    _isLoggedIn.value = 0
                }
            }
        }
    }

    fun profile(){
        viewModelScope.launch {
            if (!checkConnection()) {
                Log.w("Profile", "Brak połączenia – przerwano")
                _connected.value = false
                return@launch
            }

            when(val result = repository.profile()){
                is ApiResult.Success -> {
                    Log.d("Profile", "Wynik: ${result.data}")
                    _isLoggedIn.value = 1
                }
                is ApiResult.Error -> {
                    Log.e("Profile", "Błąd: ${result.message}")
                    _isLoggedIn.value = -1
                }
                is ApiResult.NetworkError -> {
                    Log.e("Profile", "Błąd sieciowy: ${result.exception.message}")
                    _isLoggedIn.value = -1
                    }
                else -> {
                    _isLoggedIn.value = -1
                }
            }
        }
    }

    fun logout() {
        NetworkModule.clearCookies()
        _isLoggedIn.value = -1
    }

    fun continueAsGuest() {
        viewModelScope.launch{
            val connected = checkConnection()
            if (!connected) {
                Log.w("Register", "Brak połączenia – przerwano")
                _connected.value = false
                return@launch
            }
        }
        if (!_connected.value) {
            return
        }
    }
}