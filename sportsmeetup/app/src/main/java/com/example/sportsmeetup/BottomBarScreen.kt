package com.example.sportsmeetup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
){
    object main: BottomBarScreen(
        route = "main",
        title = "Main",
        icon = Icons.Default.SportsSoccer
    )
    object sports: BottomBarScreen(
        route = "sports",
        title = "Sports",
        icon = Icons.Default.SportsSoccer
    )
    object timeslots: BottomBarScreen(
        route = "timeslots",
        title = "Timeslots",
        icon = Icons.Default.SportsSoccer
    )
    object join: BottomBarScreen(
        route = "join",
        title = "Join",
        icon = Icons.Default.SportsSoccer
    )
    object events: BottomBarScreen(
        route = "events",
        title = "Events",
        icon = Icons.Default.CalendarMonth
    )
    object profile: BottomBarScreen(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    )
    object login: BottomBarScreen( // New route for login/authentication screen
        route = "login",
        title = "Login",
        icon = Icons.Default.Person // You can pick a different icon or leave it
    )
}
