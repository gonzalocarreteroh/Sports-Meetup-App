package com.example.sportsmeetup

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sportsmeetup.screens.EventScreen
import com.example.sportsmeetup.screens.ProfileScreen
import com.example.sportsmeetup.screens.SportScreen
import androidx.compose.ui.Modifier
import androidx.navigation.compose.navigation
import com.example.sportsmeetup.screens.SportJoinScreen
import com.example.sportsmeetup.screens.SportTimeslotScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNavGraph(navController: NavHostController, modifier: Modifier = Modifier, onLogout: () -> Unit) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.main.route
    ) {
        navigation(startDestination = BottomBarScreen.sports.route, route = BottomBarScreen.main.route) {
            composable(route = BottomBarScreen.sports.route) {
                SportScreen(navController = navController)
            }
            composable(route = BottomBarScreen.timeslots.route+"/{sportType}") {
                val sportType = it.arguments?.getString("sportType")?: "All"
                SportTimeslotScreen(navController = navController, sportType = sportType)
            }
            composable(route = BottomBarScreen.join.route) {
                SportJoinScreen(navController = navController)
            }
        }

        composable(route = BottomBarScreen.events.route) {
            EventScreen()
        }

        composable(route = BottomBarScreen.profile.route) {
            ProfileScreen(onLogout = onLogout)
        }

        /*
        composable(route = BottomBarScreen.profile.route) { // Corrected route
            ProfileScreen(
                onLogout = {
                    navController.navigate(BottomBarScreen.login.route) {
                        popUpTo(BottomBarScreen.main.route) { inclusive = true } // Clear the back stack
                    }
                }
            )
        }

        composable(route = BottomBarScreen.login.route) {
            AuthScreen(onAuthSuccess = {
                navController.navigate(BottomBarScreen.main.route) {
                    popUpTo(BottomBarScreen.login.route) { inclusive = true }
                }
            })
        }
        */

    }
}