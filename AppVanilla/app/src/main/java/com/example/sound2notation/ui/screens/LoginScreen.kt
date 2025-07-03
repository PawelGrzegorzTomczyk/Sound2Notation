package com.example.sound2notation.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sound2notation.ui.layouts.AppLayout
import com.example.sound2notation.viewModels.LoginViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    navController: NavController
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val registerState by viewModel.registerSuccess.collectAsState()
    val connected by viewModel.connected.collectAsState()
    val loggedError by viewModel.loggedError.collectAsState()

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AppLayout(
        title = "Logowanie",
        showBackButton = false,
        navController = navController
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!connected) {
                Column {
                    Text(
                        text = "❗ Brak połączenia z serwerem",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.reconnect() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Spróbuj ponownie")
                    }
                }
            }

            if (isLoggedIn == 1){
                Text(text = "Zalogowano!", color = MaterialTheme.colorScheme.primary)
            }
            else if (isLoggedIn == -1){
                Text(text = "Zaloguj się lub zarejestruj")
            }
            else {
                Text(text = loggedError, color = MaterialTheme.colorScheme.error)
            }
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Login") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = connected
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Hasło") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = connected
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.login(login, password) },
                    enabled = connected
                ) {
                    Text("Zaloguj")
                }
                OutlinedButton(
                    onClick = { viewModel.register(login, password) },
                    enabled = connected
                ) {
                    Text("Zarejestruj")
                }
            }
            OutlinedButton(
                onClick = { viewModel.logout() },
                enabled = connected
            ) {
                Text("Wyloguj")
            }

            when (registerState) {
                1 -> Text("✅ Rejestracja zakończona sukcesem", color = MaterialTheme.colorScheme.primary)
                0 -> Text("❌ Błąd rejestracji" + if (viewModel.registerError.value.isNotEmpty()) ": ${viewModel.registerError.value}" else "", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
