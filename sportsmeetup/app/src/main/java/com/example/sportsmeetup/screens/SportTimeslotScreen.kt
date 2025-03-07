package com.example.sportsmeetup.screens

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.sportsmeetup.R
import com.example.sportsmeetup.SportCreateActivity
import com.example.sportsmeetup.SportJoinLeaveActivity
import com.example.sportsmeetup.classes.Event
import com.example.sportsmeetup.classes.CalendarUiModel
import com.example.sportsmeetup.classes.CalendarDataSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.functions.functions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SportTimeslotScreen(navController: NavHostController, sportType: String) {
    var eventList by remember { mutableStateOf<List<Category>>(mutableListOf()) }
    var timeslotMap by remember { mutableStateOf(mapOf<String, List<Event>>()) }
    var loading by remember { mutableStateOf(true) }
    //var eventList = listAllEvents().map { Category(date = it.key.toString(), items = it.value) }
    LaunchedEffect(Unit) {
        loading = true
        eventList = listSportTimeSlots(sportType).map { Category(date = it.key.toString(), items = it.value) } // Call your suspend function here
        timeslotMap = eventList.map{ it.date to it.items }.toMap()
        loading = false
    }
    Scaffold () { it ->
        Column(modifier = Modifier.background(Color.White).fillMaxSize().padding(it)) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Text(
                        text = "Loading...",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                val dataSource = CalendarDataSource()
                var calendarUiModel by remember { mutableStateOf(dataSource.getData(lastSelectedDate = dataSource.today)) }
                TimeSlotsHeader(data = calendarUiModel,
                    onPrevClickListener = { startDate ->
                        val finalStartDate = startDate.minusDays(1)
                        calendarUiModel = dataSource.getData(startDate = finalStartDate, lastSelectedDate = calendarUiModel.selectedDate.date)
                    },
                    onNextClickListener = { endDate ->
                        val finalStartDate = endDate.plusDays(2)
                        calendarUiModel = dataSource.getData(startDate = finalStartDate, lastSelectedDate = calendarUiModel.selectedDate.date)
                    })

                    TimeSlotsContent(
                        data = calendarUiModel,
                        onDateClickListener = { date ->
                            calendarUiModel = calendarUiModel.copy(
                                selectedDate = date,
                                visibleDates = calendarUiModel.visibleDates.map {
                                    it.copy(
                                        isSelected = it.date.isEqual(date.date)
                                    )
                                }
                            ) },
                        timeslots = timeslotMap[calendarUiModel.selectedDate.date.format(
                            DateTimeFormatter.ofPattern("dd-MM-yyyy")
                        )],
                        sportType = sportType
                    )
            }
        }
    }
}

suspend fun listSportTimeSlots(sportType: String) : Map<String, List<Event>>{

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
    val query = events.whereEqualTo("sport", sportType)
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeSlotsHeader(data: CalendarUiModel,
                    onPrevClickListener: (LocalDate) -> Unit,
                    onNextClickListener: (LocalDate) -> Unit,) {
    Row {
        Text(

            text = if (data.selectedDate.isToday) {
                "Today"
            } else {
                data.selectedDate.date.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
                )
            },
            fontWeight = Bold, fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        IconButton(onClick = {onPrevClickListener(data.startDate.date) }) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = stringResource(R.string.previous)
            )
        }
        IconButton(onClick = {onNextClickListener(data.endDate.date)}) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = stringResource(R.string.next)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeSlotsContent(
    data: CalendarUiModel,
    onDateClickListener: (CalendarUiModel.Date) -> Unit,
    timeslots: List<Event>?,
    modifier: Modifier = Modifier.fillMaxSize(),
    sportType: String
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
         modifier = Modifier.fillMaxWidth(),
    ) {
        data.visibleDates.onEach {index ->
            ContentItem(
                date = index,
                onDateClickListener
            )
        }
    }
    Button(modifier = Modifier.padding(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        onClick = {
            val intent = Intent(context, SportCreateActivity::class.java)
            intent.putExtra("sportType",sportType)
            intent.putExtra("date",data.selectedDate.date.format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
            ))
            context.startActivity(intent)
        }){
        Text("Create")
    }
    if (timeslots != null) {
        LazyColumn(modifier) {
            items(timeslots) { item ->
                CategoryItem(item.toString(),modifier,item,"Join")
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Text(
                text = stringResource(R.string.no_events_available),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ContentItem(
    date: CalendarUiModel.Date,
    onClickListener: (CalendarUiModel.Date) -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .clickable {
                onClickListener(date)
            },
        colors = CardDefaults.cardColors(
            containerColor = if (date.isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondary
            }
        ),
    ) {
        Column(
            modifier = Modifier
                .width(40.dp)
                .height(48.dp)
                .padding(4.dp)
        ) {
            Text(
                text = date.day,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = date.date.dayOfMonth.toString(),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
@Composable
@Preview
fun SportTimeslotsScreenPreview() {
    val plist = listOf(
        Event("Badminton", 1730527200000, "Tsang Siu Tim Sports Hall", "John", listOf("Jim,Mary")),
        Event("Badminton",  1731744000000, "Tsang Siu Tim Sports Hall", "Jane", listOf("Jim")),
        Event("Basketball", 1730527200000, "Tsang Siu Tim Sports Hall", "Jim", listOf(""))
    ).groupBy { it.getMonthandDate() }.toSortedMap()
        .map { Category(date = it.key.toString(), items = it.value) }
    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
    ) {
        CategorizedEvents(
            categories = plist
        )
    }

}
 **/