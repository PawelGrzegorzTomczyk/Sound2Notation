package com.example.sound2notation

import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sound2notation.data.repository.NetTaskRepository
import com.example.sound2notation.di.NetworkModule // Importuj NetworkModule
import com.example.sound2notation.ui.screens.HomeScreen
import com.example.sound2notation.ui.screens.LoginScreen
import com.example.sound2notation.ui.screens.NotesScreen
import com.example.sound2notation.ui.screens.SplashScreen
import com.example.sound2notation.ui.theme.Sound2NotationTheme
import com.example.sound2notation.viewModels.HomeViewModel
import com.example.sound2notation.viewModels.LoginViewModel

class MainActivity : ComponentActivity() {
    private lateinit var repository: NetTaskRepository
    // private lateinit var apiService: ApiService // Nie potrzebujesz już tego

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Użyj apiService z NetworkModule
        val apiService = NetworkModule.apiService
        val repository = NetTaskRepository(apiService)

        enableEdgeToEdge()


        setContent {
            var showSplashScreen by rememberSaveable { mutableStateOf(true) }
            Sound2NotationTheme {
                if (showSplashScreen) {
                    SplashScreen(onAnimationFinished = {
                        showSplashScreen = false
                    })
                } else {
                    AppNavigation(repository, application)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(repository: NetTaskRepository, application: Application) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home_screen") {
        composable("login_screen") {
            val factory = remember { AppViewModelFactory(repository, application) }
            val viewModel: LoginViewModel = viewModel(factory = factory)
            LoginScreen(viewModel = viewModel, navController = navController)
        }
        composable("home_screen"){
            val factory = remember { AppViewModelFactory(repository, application) }
            val viewModel: HomeViewModel = viewModel(factory = factory)
            HomeScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            "notes_screen/{url}",
            arguments = listOf(navArgument("url") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val url = Uri.decode(encodedUrl)
            NotesScreen(url = url, navController = navController)
        }
    }
}

class AppViewModelFactory(
    private val repository: NetTaskRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository, application) as T
        }
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, application) as T
        }
        // Dodaj tutaj inne ViewModele, które mają takie same zależności

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}