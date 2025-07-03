package com.example.sound2notation.ui.layouts

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.sound2notation.R

@Composable
fun AppLayout(
    modifier: Modifier = Modifier,
    title: String = "",

    showBackButton: Boolean = false,
    onNavigationIconClick: () -> Unit = {},
    navController: NavController,

    content: @Composable (PaddingValues) -> Unit // Zawartość ekranu
){
    Scaffold (
        topBar = {
            AppTopBar(
                modifier = modifier,
                title = title,
                navigationIcon = if (showBackButton)
                {
                    {
                        IconButton(
                            onClick = onNavigationIconClick,
    //                        modifier = TODO(),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.primaryContainer,
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        ){
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "TO DO")
                        }
                    }
                } else{{}}

            )
        },
        bottomBar = {
            AppBottomBar(modifier = modifier, navController = navController)
        },
        content = { innerPadding ->
            content(innerPadding)
        }
    )
}