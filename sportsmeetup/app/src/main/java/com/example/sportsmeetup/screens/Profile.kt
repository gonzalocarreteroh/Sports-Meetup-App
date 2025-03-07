package com.example.sportsmeetup.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsmeetup.R
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.functions.functions
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.clickable
import com.google.firebase.storage.StorageReference


@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    var name by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    // var profileImage by remember { mutableIntStateOf(R.drawable.coleman_wong_profile) }
    // val profileImageUrl by produceState<String?>(initialValue = null, key1 = name) {
        // value = name?.let { getUserPhoto(it) }
    // }
    var newPassword by remember { mutableStateOf("") }
    var changed by remember { mutableStateOf(false) }
    var clickedDelete by remember { mutableStateOf(false) }
    // For Password change button (no functionality yet)
    var passwordChangeClicked by remember { mutableStateOf(false) }

    val firebaseAuth = FirebaseAuth.getInstance()
    val user = firebaseAuth.currentUser
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val storageReference = FirebaseStorage.getInstance().reference

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let { uploadImageToFirebase(it, user, storageReference) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*
        // Profile Image
        IconButton(
            onClick = { /* Handle image picker logic here */ },
            modifier = Modifier.size(120.dp)
        ) {
            Image(
                painter = painterResource(id = profileImage),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape) // Circle shape for the profile image
            )
        }
        */

        // val firebaseAuth = FirebaseAuth.getInstance()
        // val user = firebaseAuth.currentUser
        name = user?.displayName

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable {
                    launcher.launch("image/*") // Open gallery to pick an image
                },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                CoilImage(
                    imageModel = { imageUri },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            } else if (user?.photoUrl != null) {
                CoilImage(
                    imageModel = { user.photoUrl },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_avatar),
                    contentDescription = "Default Avatar",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        /*
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (user?.photoUrl != null) {
                CoilImage(
                    imageModel = { user.photoUrl },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_avatar),
                    contentDescription = "Default Avatar",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        */

        Spacer(modifier = Modifier.height(16.dp))



        // Name TextField
        Text(
            text = "Name: $name",
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email TextField
        email = user?.email
        Text(
            text = "Email: $email",
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Section
        Button(
            onClick = {
                passwordChangeClicked = !passwordChangeClicked
                // Implement password change logic later
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.change_password))
        }

        if (passwordChangeClicked) {
            TextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text(stringResource(R.string.new_password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Cancel Button
                Button(
                    onClick = { passwordChangeClicked = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray, // Set the button's background color to gray
                        contentColor = Color.White // Keep the text color as white
                    )
                ) {
                    Text(text = "Cancel")
                }
                Button(
                    onClick = {
                        user!!.updatePassword(newPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    changed = true
                                }
                            }
                    }
                ) { Text(text = stringResource(R.string.submit)) }
            }
        }

        if (changed) {
            TextButton(
                onClick = {
                    passwordChangeClicked = false
                    changed = false
                }
            ) { Text(stringResource(R.string.successfully_changed_password)) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = {
                // FirebaseAuth.getInstance().signOut() // Log out from Firebase
                onLogout() // Call the logout callback to navigate back to AuthScreen
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.log_out))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delete account Button
        Button(
            onClick = {
                clickedDelete = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red, // Set the button's background color to red
                contentColor = Color.White // Keep the text color as white
            )
        ) {
            Text(text = stringResource(R.string.delete_account))
        }

        if (clickedDelete) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("This action cannot be undone. Are you sure?")

                Spacer(modifier = Modifier.height(16.dp)) // Space between the text and buttons

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Cancel Button
                    Button(
                        onClick = { clickedDelete = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray, // Set the button's background color to gray
                            contentColor = Color.White // Keep the text color as white
                        )
                    ) {
                        Text(text = "Cancel")
                    }

                    // Confirm Button
                    Button(
                        onClick = {
                            user!!.delete()
                            onLogout() // Call the logout callback to navigate back to AuthScreen
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red, // Set the button's background color to red
                            contentColor = Color.White // Keep the text color as white
                        )
                    ) {
                        Text(text = "Confirm")
                    }
                }
            }
        }



    }
}

private fun uploadImageToFirebase(
    uri: Uri,
    user: FirebaseUser?,
    storageReference: StorageReference
) {
    val userId = user?.uid ?: return
    val fileRef = storageReference.child("profile_images/$userId.jpg")

    fileRef.putFile(uri)
        .addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                // Update photo URL in Firebase Auth
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(downloadUri)
                    .build()
                user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Log.d("Firebase", "User profile updated.")
                    }
                }
            }
        }
        .addOnFailureListener {
            // Log.e("Firebase", "Error uploading image", it)
        }
}

/*
suspend fun getUserPhoto(s: String): String {
    val fireFunctions = Firebase.functions
    val data = hashMapOf(
        "text" to s
    )
    return try {
        val result = fireFunctions
            .getHttpsCallable("getUserPicById")
            .call(data)
            .await()


        result.getData().toString()
    } catch (e: Exception) {
        println("Error fetching user data: ${e.message}")
        ""
    }
}
*/

@Preview
@Composable
fun PreviewProfileScreen() {
    // ProfileScreen()
}
