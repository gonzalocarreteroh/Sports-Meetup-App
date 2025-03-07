package com.example.sportsmeetup

import android.annotation.SuppressLint
import android.graphics.Paint.Align
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.collection.mutableObjectListOf
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsmeetup.classes.Event
import com.example.sportsmeetup.ui.theme.SportsMeetupTheme
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import java.util.Arrays
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await



class SportJoinLeaveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val event = intent.getSerializableExtra("event") as Event
        val joinOrLeave = intent.getStringExtra("joinOrLeave") as String


        setContent {
            SportsMeetupTheme {
                SportJoinLeaveScreen(event, joinOrLeave)
            }
        }
    }
//    companion object{
//        private var finished = false
//        fun setFinished(s:Boolean){
//            finished = s
//        }
//    }

}

data class userData(var name: String, var photo: String, var number: String)


@Composable
fun SportJoinLeaveScreen(eventAO: Event, joinOrLeave: String) {
    var finisihed = false
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var c = userData("", "","")
    var loading by remember { mutableStateOf(true) }
    var ud = mutableListOf<userData>()
    val openAlertDialog = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        loading = true;
        c.name = getUserName(eventAO.creator)
        c.photo = getUserPhoto(eventAO.creator)

        eventAO.participants.forEach({
            ud.add(userData(getUserName(it), getUserPhoto(it), getUserNumber(it)))
        })
        loading = false;
    }
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize().padding(it),
        ) {
            LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                item(span = { GridItemSpan(3) }) {
                    Text(
                        text = "Sport: " + eventAO.sport + "\n"
                                + "Date: " + eventAO.getFullDate() + "\n"
                                + "Time: " + eventAO.getTime(),
                        color = Color.Black,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                item(span = { GridItemSpan(3) }) {
                    Text(
                        text = "Created by",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                //getCreatorNameAndPic(eventAO)

                if (!loading) {
                    item{
                        Column(modifier = Modifier) {
                            CoilImage(
                                imageModel = {
                                    if (c.photo.isEmpty()) {
                                        R.drawable.default_avatar
                                    } else c.photo
                                             },
                                imageOptions = ImageOptions(
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center,
                                    ),
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .padding(4.dp)
                                    // Circle shape for the profile image
                            )
                            Text(
                                text = c.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 20.sp,
                                modifier = Modifier.fillMaxSize(),
                                textAlign = TextAlign.Center
                            )
                        }

                    }
                }

                item(span = { GridItemSpan(3) }) {
                    Text(
                        text = "Participants",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                items(ud.size){ psIndex ->
                    Column(modifier = Modifier) {
                        CoilImage(
                            imageModel = {
                                if (ud.get(psIndex).photo.isEmpty()) {
                                    R.drawable.default_avatar
                                } else
                                    ud.get(psIndex).photo
                                         },
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center,
                            ),
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .padding(4.dp)
                            // Circle shape for the profile image
                        )
                        Text(
                            text = ud.get(psIndex).name,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 20.sp,
                            modifier = Modifier.fillMaxSize(),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = ud.get(psIndex).number,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 20.sp,
                            modifier = Modifier.fillMaxSize(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                item(span = { GridItemSpan(3) }) {
                    Button(modifier = Modifier.padding(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            openAlertDialog.value = true

                        }){
                        Text(joinOrLeave)
                    }
                }
            }
            if(openAlertDialog.value){
                AlertDialog(
                    title = {
                        if(joinOrLeave.equals("Join"))
                            Text("Joining event")
                        else
                            Text("Leaving event")
                    },
                    text = {
                        if(joinOrLeave.equals("Join"))
                            Text("You will be joining this event.")
                        else
                            Text("Are you sure you want to leave the event?")

                    },
                    onDismissRequest = {
                        openAlertDialog.value = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                openAlertDialog.value = false

                                coroutineScope.launch {
                                    if(joinOrLeave.equals("Join")) {
                                        val success = joinEvent(eventAO)
                                        if (success) {
                                            Toast.makeText(
                                                context,
                                                "You have joined the event",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finisihed = true
                                            (context as? SportJoinLeaveActivity)?.finish()
                                        } else
                                            Toast.makeText(
                                                context,
                                                "Error joining event",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                    }
                                    else {
                                        val success = leaveEvent(eventAO)
                                        if (success) {
                                            Toast.makeText(
                                                context,
                                                "You have left the event",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finisihed = true
                                            (context as? SportJoinLeaveActivity)?.finish()
                                        } else
                                            Toast.makeText(
                                                context,
                                                "Error leaving event",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                    }
                                }

                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                openAlertDialog.value = false
                            }
                        ) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }



}


suspend fun leaveEvent(eventAO: Event) : Boolean{
    return try{
        val events = Firebase.firestore.collection("events")
        val doc = events.document(eventAO.docId)
        doc.update("participants",FieldValue.arrayRemove(Firebase.auth.currentUser?.uid)).await()
        true
    }catch(e: Exception){
        println("Error leaving event: ${e.message}")
        false
    }
}

suspend fun joinEvent(eventAO: Event) : Boolean{
    return try{
        val events = Firebase.firestore.collection("events")
        val doc = events.document(eventAO.docId)
        doc.update("participants",FieldValue.arrayUnion(Firebase.auth.currentUser?.uid)).await()
        true
    }catch(e: Exception){
        println("Error joining event: ${e.message}")
        false
    }
}

suspend fun getUserName(s: String): String {
    //Is this instance static?

    val fireFunctions = Firebase.functions
    val data = hashMapOf(
        "text" to s
    )
    return try {
        val result = fireFunctions
            .getHttpsCallable("getUserDisplayById")
            .call(data)
            .await()


        result.getData().toString()
    } catch (e: Exception) {
        println("Error fetching user data: ${e.message}")
        ""
    }
}

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

suspend fun getUserNumber(s: String): String {
    //Is this instance static?

    val fireFunctions = Firebase.functions
    val data = hashMapOf(
        "text" to s
    )
    return try {
        val result = fireFunctions
            .getHttpsCallable("getUserNumberById")
            .call(data)
            .await()

        result.getData().toString()
    } catch (e: Exception) {
        println("Error fetching user data: ${e.message}")
        ""
    }
}
//suspend fun getCreatorNameAndPic(s: String):userData{
//    //Is this instance static?
//
//    val fireFunctions =  Firebase.functions
//    val data = hashMapOf(
//        "text" to s
//    )
//return try{
//    val result = fireFunctions
//        .getHttpsCallable("getUserDisplayAndPicById")
//        .call(data)
//        .await()
//
//    val jsonData = result.getData().toString()
//    val mapper = jacksonObjectMapper()
//    mapper.readValue(jsonData) // Deserialize the JSON to List<userData>
//}catch(e: Exception){
//    println("Error fetching user data: ${e.message}")
//    userData("","") // Return an empty list in case of an error
//}


//suspend fun getCreatorNameAndPicList(eventAO: Event):List<userData>{
//    //Is this instance static?
//    val req = eventAO.participants.joinToString(",")
//    val fireFunctions =  Firebase.functions
//    val data = hashMapOf(
//        "text" to req
//    )
//    return try {
//        // Call the Firebase function and await the result
//        val result = fireFunctions
//            .getHttpsCallable("getUserDisplayAndPicByIdList")
//            .call(data)
//            .await() // Awaiting the completion of the task
//        println(result.getData().toString())
//        val jsonData = result.getData().toString()
//        val mapper = jacksonObjectMapper()
//        mapper.readValue(jsonData) // Deserialize the JSON to List<userData>
//    } catch (e: Exception) {
//        println("Error fetching user data: ${e.message}")
//        emptyList() // Return an empty list in case of an error
//    }
//
//}

@Composable
@Preview
fun SportJoinLeaveScreenPreview() {
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize().padding(16.dp),
    ) {
        LazyVerticalGrid(columns = GridCells.Fixed(3)) {
            item(span = { GridItemSpan(3) }) {
                Text(
                    text = "Sport: Badminton \n"
                            + "Date: 02-11-2024\n"
                            + "Time: 14:00",
                    color = Color.Black,
                    fontSize = 32.sp,
                    lineHeight = 36.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
            item(span = { GridItemSpan(3) }) {
                Text(
                    text = "Created by",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }

            item (){
                Column(modifier = Modifier) {
                    CoilImage(
                        imageModel = { "https://images.emojiterra.com/microsoft/fluent-emoji/15.1/3d/1f3c0_3d.png" }, // loading a network image or local resource using an URL.
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,

                            ),
                        modifier = Modifier.size(120.dp)
                            .clip(CircleShape)
                            .padding(16.dp)
                            .fillMaxWidth(), // Circle shape for the profile image

                    )
                    Text(
                        text = "AA",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item(span = { GridItemSpan(3) }) {
                Text(
                    text = "Participants",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }

            item{
                Column(modifier = Modifier) {
                    CoilImage(
                        imageModel = { },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                        ),
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .padding(4.dp)
                        // Circle shape for the profile image
                    )
                    Text(
                        text = "A",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            item(span = { GridItemSpan(3) }) {
                Button(modifier = Modifier.padding(8.dp),colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), onClick = {}){ Text("Leave") }
            }
        }


    }
}
