@file:OptIn(ExperimentalFoundationApi::class)

package com.example.sportsmeetup.screens


import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.*
import android.widget.Button
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import com.example.sportsmeetup.BottomBarScreen
import com.example.sportsmeetup.BottomNavGraph
import com.example.sportsmeetup.SportJoinLeaveActivity
import com.example.sportsmeetup.classes.Event
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ListenerRegistration

var isEmulatorStarted = false;
@Composable
fun EventScreen() {
    var eventList by remember { mutableStateOf<List<Category>>(mutableListOf()) }
    var loading by remember { mutableStateOf(true) }

    //var eventList = listAllEvents().map { Category(date = it.key.toString(), items = it.value) }
//    LaunchedEffect(Unit) {
//        loading = true
//        eventList = listAllEvents().map { Category(date = it.key.toString(), items = it.value) } // Call your suspend function here
//        loading = false
//    }
    LaunchedEffect(Unit) {
        val listenerRegistration = listAllEventsListener { updatedEvents ->
            eventList = updatedEvents.map { Category(date = it.key, items = it.value) }
        }


    }
    //list = list.map { Category(date = it.key.toString(), items = it.value) }
    Scaffold (){
        Box(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize().padding(it),
        ) {
//            if (loading) {
//                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
//                    Text(
//                        text = "Loading...",
//                        color = Color.Black,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                    )
//                }
//            } else {
                if(eventList.isNotEmpty()){
                    CategorizedEvents(
                        categories = eventList,
                    )
                }else{
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Text(
                            text = "You have no events!",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                }
            //}
        }
    }

}

fun listAllEventsListener(onEventsUpdated: (Map<String, List<Event>>) -> Unit) : ListenerRegistration{

    val fireFunctions =  Firebase.functions
    val db = Firebase.firestore
    if(!isEmulatorStarted){
        //var host: String = Platform.isAndroid? "10.0.2.2" : "localhost"
        fireFunctions.useEmulator("10.0.2.2", 5001)
        db.useEmulator("10.0.2.2", 8080)
        db.firestoreSettings = firestoreSettings {
            setLocalCacheSettings( persistentCacheSettings {

            })
        }
        isEmulatorStarted = true
    }
    
    val uid = Firebase.auth.currentUser?.uid
    val events = db.collection("events")
    val query = events.whereArrayContains("participants", uid.toString())
    // Set up the listener
    return query.addSnapshotListener { querySnapshot, error ->
        if (error != null) {
            Log.w(TAG, "Listen failed.", error)
            return@addSnapshotListener
        }

        val returnList = mutableListOf<Event>()
        if (querySnapshot != null) {
            val tasks = querySnapshot.documents.map { document ->
                val docId = document.id
                val sport: String = document.getString("sport").toString()
                val time: Long = document.getTimestamp("time")!!.toDate().time
                val venue: String = document.getString("venue").toString()
                val creator: String = document.getString("creator").toString() // Default given name

                // The data passing to Firebase functions
                val data = hashMapOf("text" to creator)

                fireFunctions
                    .getHttpsCallable("getUserDisplayById")
                    .call(data)
                    .continueWithTask { task ->
                        if (task.isSuccessful) {
                            val creatorName = task.result?.getData().toString()
                            val participants = document.get("participants") as List<String>
                            returnList += Event(docId, sport, time, venue, creator, participants, creatorName)
                            Tasks.forResult<Void>(null) // Return a successful task
                        } else {
                            Tasks.forException<Void>(task.exception ?: Exception("Unknown error")) // Handle error
                        }
                    }
            }
            Tasks.whenAllComplete(tasks).addOnCompleteListener {
                // Once all tasks are complete, send the updated list back
                onEventsUpdated(returnList.sortedBy { it.getTimeLong() }.groupBy { it.getFullDate() })
            }
        }
    }




}

suspend fun listAllEvents() : Map<String, List<Event>>{

    val fireFunctions =  Firebase.functions
    val db = Firebase.firestore
    if(!isEmulatorStarted){
        //var host: String = Platform.isAndroid? "10.0.2.2" : "localhost"
        fireFunctions.useEmulator("10.0.2.2", 5001)
        db.useEmulator("10.0.2.2", 8080)
        db.firestoreSettings = firestoreSettings {
            setLocalCacheSettings( persistentCacheSettings {

            })
        }
        isEmulatorStarted = true
    }





    val uid = Firebase.auth.currentUser?.uid
    val events = db.collection("events")
    val query = events.whereArrayContains("participants", uid.toString())
    val returnList = mutableListOf<Event>()

    val querySnapshot = query.get().await()

    val tasks = querySnapshot.documents.map { document ->
        val docId = document.id
        val sport: String = document.getString("sport").toString()
        val time: Long = document.getTimestamp("time")!!.toDate().time
        val venue: String = document.getString("venue").toString()
        var creator: String = document.getString("creator").toString() // Default given name

        //The data passing to firebase functions
        val data = hashMapOf(
            "text" to creator
        )

        fireFunctions
            .getHttpsCallable("getUserDisplayById")
            .call(data)
            .continueWithTask { task ->
                if (task.isSuccessful) {
                    val creatorName = task.result?.getData().toString()
                    val participants = document.get("participants") as List<String>
                    returnList += Event(docId,sport, time, venue, creator, participants,creatorName)
                    Tasks.forResult<Void>(null) // Return a successful task
                } else {
                    Tasks.forException<Void>(task.exception ?: Exception("Unknown error")) // Handle error
                }
            }
    }
    Tasks.whenAllComplete(tasks).await()
    return returnList.groupBy { it.getFullDate() }.toSortedMap()

}

data class Category(
    val date: String,
    val items: List<Event>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategorizedEvents(
    categories: List<Category>,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    LazyColumn(modifier) {
        categories.forEach { category ->
            stickyHeader {
                CategoryHeader(category.date)
            }
            items(category.items) { item ->
                CategoryItem(item.toString(),modifier,item)
            }
        }
    }
}

@Composable
fun CategoryHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.Black,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.fillMaxWidth().padding(12.dp)
    )
}

@Composable
fun CategoryItem(text: String, modifier: Modifier = Modifier, eventO:Event, isJoin: String = "Leave") {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val sizeScale by animateFloatAsState(if (isPressed) 0.75f else 1f)
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.surface
        ),
        onClick = {
            val intent = Intent(context,SportJoinLeaveActivity::class.java)
            intent.putExtra("event",eventO)
            intent.putExtra("joinOrLeave",isJoin)
            context.startActivity(intent)

        },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier.fillMaxWidth().padding(8.dp).wrapContentSize()
            .graphicsLayer(
                scaleX = sizeScale,
                scaleY = sizeScale
            ),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = modifier.fillMaxWidth().padding(16.dp)
                //.background(MaterialTheme.colorScheme.background)
        )

    }
}



//Init firestore
//fun initFireStore(){
//    Firebase.initialize()
//}

//fun useEmulator() {
//
//}

@Composable
@Preview
fun EventScreenPreview() {
    val plist = listOf(
        Event("9823h5nu213oiuh12","Badminton", 1730527200000, "Tsang Siu Tim Sports Hall", "wwkFBDW4oPQrmUa4pUwh2eCk64o2", listOf("Jim,Mary"),"John"),
        Event("9823h5nu213oiuh13","Badminton",  1731744000000, "Tsang Siu Tim Sports Hall", "wwkFBDW4oPQrmUa4pUwh2eCk64o3", listOf("Jim"),"Jane"),
        Event("9823h5nu213oiuh14","Basketball", 1730527200000, "Tsang Siu Tim Sports Hall", "wwkFBDW4oPQrmUa4pUwh2eCk64o4", listOf(""),"Jim")
    ).groupBy { it.getFullDate() }.toSortedMap()
        .map { Category(date = it.key.toString(), items = it.value) }
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
    ) {
        CategorizedEvents(
            categories = plist,
        )
    }
}