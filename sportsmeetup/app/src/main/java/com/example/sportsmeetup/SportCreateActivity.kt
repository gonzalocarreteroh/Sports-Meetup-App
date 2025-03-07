package com.example.sportsmeetup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.sportsmeetup.classes.Event
import com.example.sportsmeetup.ui.theme.SportsMeetupTheme
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

class SportCreateActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sportType = intent.getStringExtra("sportType") as String
        val date = intent.getStringExtra("date") as String


        setContent {
            SportsMeetupTheme {
                SportCreateScreen(sportType,date)
            }
        }
    }
}

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SportCreateScreen(sportType: String, date: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text("SportType: $sportType")
            Text("Date: $date")
            //var time by remember { mutableStateOf("") }
            var venue by remember { mutableStateOf("") }
            TimeInput(
                state = timePickerState,
            )
            currentTime.set(Calendar.HOUR_OF_DAY, timePickerState.hour);
            currentTime.set(Calendar.MINUTE, timePickerState.minute);
            val time = SimpleDateFormat("HH:mm").format(currentTime.getTime())
            //TextField(value = time, onValueChange = { time = it })
            TextField(value = venue, onValueChange = { venue = it })
            Button(onClick = {
                coroutineScope.launch {
                val success = createEvent(sportType, date, time, venue)
                if (success) {
                    (context as? SportCreateActivity)?.finish()
                }  }
            }) { Text("Create") }
        }
    }
}

@SuppressLint("SimpleDateFormat")
@RequiresApi(Build.VERSION_CODES.O)
suspend fun createEvent(sportType: String, date: String, time: String, venue: String) : Boolean{

    return try{
        //val l = LocalDate.parse("$date $time", DateTimeFormatter.ofPattern())
        val l = SimpleDateFormat("dd-MM-yyyy HH:mm")
        val timestamp = l.parse("$date $time")
        val postMap = hashMapOf(
            "creator" to Firebase.auth.currentUser?.uid,
            "participants" to arrayListOf(Firebase.auth.currentUser?.uid.toString()),
            "sport" to sportType,
            "time" to timestamp?.let { Timestamp(it) },
            "venue" to venue
             )
        val events = Firebase.firestore.collection("events")
        events.add(postMap).await()
        //val doc = events.document(eventAO.docId)

        //doc.update("participants", FieldValue.arrayRemove(Firebase.auth.currentUser?.uid)).await()
        true
    }catch(e: Exception){
        println("Error creating event: ${e.message}")
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputExample(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    Column {
        TimeInput(
            state = timePickerState,
        )
        Button(onClick = onDismiss) {
            Text("Dismiss picker")
        }
        Button(onClick = onConfirm) {
            Text("Confirm selection")
        }
    }
}