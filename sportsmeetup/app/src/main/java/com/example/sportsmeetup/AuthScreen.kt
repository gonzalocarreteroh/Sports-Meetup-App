package com.example.sportsmeetup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest


@Composable
fun AuthScreen (
    onAuthSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) } // Toggle between sign-in and sign-up
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reset by remember { mutableStateOf(false) }
    val firebaseAuth = FirebaseAuth.getInstance()

    Column(
    modifier = Modifier
    .fillMaxSize()
    .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(if (isSignUp) R.string.sign_up else R.string.sign_in), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        if (isSignUp) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.full_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Email input
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password input
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (isSignUp) {
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.confirm_password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Sign-In/Sign-Up Button
        Button(
            onClick = {
                if (isSignUp) {
                    if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && password == confirmPassword) {
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = firebaseAuth.currentUser
                                    val profileUpdates = userProfileChangeRequest {
                                        displayName = name
                                    }
                                    user?.updateProfile(profileUpdates)
                                        ?.addOnCompleteListener { profileTask ->
                                            if (profileTask.isSuccessful) {
                                                onAuthSuccess()
                                            } else {
                                                errorMessage = profileTask.exception?.localizedMessage
                                            }
                                        }
                                } else {
                                    errorMessage = task.exception?.localizedMessage
                                }
                            }
                    } else {
                        errorMessage = "A field is empty or passwords are not the same"
                    }
                } else {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onAuthSuccess()
                                } else {
                                    errorMessage = task.exception?.localizedMessage
                                }
                            }
                    } else {
                        errorMessage = "Please fill in your email and password for the account."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(if (isSignUp) R.string.sign_up else R.string.sign_in))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Switch between sign-in and sign-up
        TextButton(onClick = { isSignUp = !isSignUp }) {
            Text(text = stringResource(if (isSignUp) R.string.have_account else R.string.no_account))
        }

        if (!isSignUp) {
            TextButton(onClick = {
                reset = false
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            reset = true
                        }
                    }
            }) {
                Text(stringResource(R.string.forgot_password))
            }

            if (reset) {
                Text(stringResource(R.string.email_successfully_sent))
            }
        }
    }
}