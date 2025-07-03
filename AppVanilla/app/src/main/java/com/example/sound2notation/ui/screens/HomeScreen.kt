package com.example.sound2notation.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sound2notation.data.model.FileInfo
import com.example.sound2notation.ui.layouts.AppLayout
import com.example.sound2notation.viewModels.HomeViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.sound2notation.data.model.MyFilesResponse
import com.example.sound2notation.data.model.ResultResponse
import com.example.sound2notation.data.repository.ApiResult
import java.io.File

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    navController: NavController
) {
    val connected by viewModel.connected.collectAsState()
    val uploaded by viewModel.uploaded.collectAsState()
    val resultState by viewModel.resultState.collectAsState()
    val myFilesResult by viewModel.myFilesState.collectAsState()

    AppLayout(
        title = "Home", // Zmieniamy tytuł
        showBackButton = false,
        navController = navController
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize() // fillMaxSize, żeby LazyColumn mógł się przewijać
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // Centrowanie elementów
            verticalArrangement = Arrangement.Top
        ) {
            // Sekcja statusu połączenia
            if (!connected) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "❗ Brak połączenia z serwerem",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = { viewModel.reconnect() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text("Spróbuj ponownie")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

            }

            val context = LocalContext.current

            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    val mimeType = context.contentResolver.getType(it) ?: "application/octet-stream"
                    val fileName = getFileName(context, it)
                    viewModel.uploadFileFromUri(context, it, fileName, mimeType)
                }
            }

            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = connected
            ) {
                Text("📁 Wybierz plik do uploadu")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.tryToGetResult(navController) },
                    enabled = uploaded && connected,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📄 Pokaż nuty")
                }

                Spacer(modifier = Modifier.width(8.dp))

                when ( resultState ) {
                    null -> {
                        Text("Brak danych")
                    }
                    is ApiResult.Success -> {
                        Text("Plik: ${(resultState as ApiResult.Success<ResultResponse>).data.xml_file}", modifier = Modifier.weight(1f))
                        // Tu możesz dodać przycisk otwarcia lub pobrania pliku
                    }
                    is ApiResult.Error -> {
                        Text("Błąd: ${(resultState as ApiResult.Error).message}", color = MaterialTheme.colorScheme.error)
                    }
                    is ApiResult.NetworkError -> {
                        Text("Błąd sieci", color = MaterialTheme.colorScheme.error)
                    }
                    is ApiResult.UnknownError -> {
                        Text("Nieznany błąd", color = MaterialTheme.colorScheme.error)
                    }
                    // możesz też dodać własny status Processing, jeśli masz
                    else -> {
                        Text("Ładowanie...")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if(myFilesResult is ApiResult.Success){
                Text("Twoje pliki:", style = MaterialTheme.typography.titleMedium)

                val files = (myFilesResult as ApiResult.Success<MyFilesResponse>).data.files
                if (files.isEmpty()) {
                    Text("Nie masz żadnych plików.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(files) { file ->
                            ExpandableFileItem(file = file, onClick = { viewModel.test(navController, file.uuid ) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableFileItem(file: FileInfo, onClick: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = file.filename, style = MaterialTheme.typography.titleMedium)
            if (expanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Data uploadu: ${file.upload_date}")

                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📄 Pokaż nuty")
                }
            }
        }
    }
}

fun getFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && it.moveToFirst()) {
            return it.getString(nameIndex)
        }
    }
    return "plik_" + System.currentTimeMillis()
}

