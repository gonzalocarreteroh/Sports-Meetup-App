package com.example.sportsmeetup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sportsmeetup.ui.theme.SportsMeetupTheme
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.sportsmeetup.ui.theme.SportsMeetupTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SportsMeetupTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                val firebaseAuth = FirebaseAuth.getInstance()
                // firebaseAuth.setPersistenceEnabled(true)

                // Check if already signed in
                LaunchedEffect(Unit) {
                    isLoggedIn = firebaseAuth.currentUser != null
                }

                if (isLoggedIn) {
                    MainScreen(
                        onLogout = {
                            firebaseAuth.signOut()
                            isLoggedIn = false
                        }
                    )
                } else {
                    AuthScreen(onAuthSuccess = { isLoggedIn = true })
                }
            }
        }
    }
}

