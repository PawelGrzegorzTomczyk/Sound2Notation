package com.example.sound2notation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.sound2notation.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var backgroundVisible by rememberSaveable { mutableStateOf(false) }
    var iconVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Opóźnienie przed rozpoczęciem animacji tła
        delay(300)
        backgroundVisible = true // Pokaż tło
        delay(800) // Opóźnienie po wejściu tła, przed pojawieniem się ikony
        iconVisible = true // Pokaż ikonę
        delay(1200) // Opóźnienie po pojawieniu się ikony, zanim przejdziemy do głównej zawartości
        onAnimationFinished() // Wywołaj callback po zakończeniu animacji
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Możesz zmienić kolor tła ekranu powitalnego
        contentAlignment = Alignment.Center
    ) {
        // Animacja wejścia tła z lewej strony
        AnimatedVisibility(
            visible = backgroundVisible,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth }, // Wjeżdża z lewej
                animationSpec = tween(durationMillis = 1000) // Czas trwania animacji
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.background), // Twój zasób "background"
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Animacja pojawienia się ikony (fade-in)
        AnimatedVisibility(
            visible = iconVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 800)) // Czas trwania animacji
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_icon), // Twój zasób "app_icon"
                contentDescription = "App Icon",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}