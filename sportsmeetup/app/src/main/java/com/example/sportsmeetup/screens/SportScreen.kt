package com.example.sportsmeetup.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.sportsmeetup.BottomBarScreen
import com.example.sportsmeetup.BottomNavGraph
import com.example.sportsmeetup.R
import com.example.sportsmeetup.classes.Event

@Composable
fun SportScreen(navController: NavHostController){
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier =  Modifier.fillMaxHeight(0.75f), verticalArrangement = Arrangement.SpaceEvenly) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SportButton(
                    icon = painterResource(id = R.drawable.sports_tennis),
                    text = stringResource(R.string.tennis),
                    onClick = { navController.navigate(BottomBarScreen.timeslots.route + "/tennis") }
                )
                SportButton(
                    icon = painterResource(id = R.drawable.exercise),
                    text = stringResource(R.string.exercise),
                    onClick = { navController.navigate(BottomBarScreen.timeslots.route + "/exercise") }
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SportButton(
                    icon = painterResource(id = R.drawable.sports_basketball),
                    text = stringResource(R.string.basketball),
                    onClick = { navController.navigate(BottomBarScreen.timeslots.route + "/basketball") }
                )
                SportButton(
                    icon = painterResource(id = R.drawable.sports_soccer),
                    text = stringResource(R.string.football),
                    onClick = { navController.navigate(BottomBarScreen.timeslots.route + "/football") }
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SportButton(
                    icon = painterResource(id = R.drawable.swimming),
                    text = stringResource(R.string.swimming),
                    onClick = { navController.navigate(BottomBarScreen.timeslots.route + "/swimming") }
                )
                SportButton(
                    icon = painterResource(id = R.drawable.jogging),
                    text = stringResource(R.string.jogging),
                    onClick = { navController.navigate(BottomBarScreen.timeslots.route + "/jogging") }
                )
            }
        }
    }
}


@Composable
fun SportButton(
    icon: Painter,
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val sizeScale by animateFloatAsState(if (isPressed) 0.75f else 1f)
    Column {
        Button(
            shape = RoundedCornerShape(15.dp),
            onClick = onClick,
            modifier = Modifier.size(90.dp).wrapContentSize()
                .graphicsLayer(
                    scaleX = sizeScale,
                    scaleY = sizeScale
                ),
            interactionSource = interactionSource,
            contentPadding = PaddingValues(0.dp),
            content = {
                    Image(modifier = Modifier.fillMaxSize(0.75f), painter = icon, contentDescription = null)
                }
        )
        Text(text, fontWeight = Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
@Preview
fun SportScreenPreview() {
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier =  Modifier.fillMaxHeight(0.75f), verticalArrangement = Arrangement.SpaceEvenly) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SportButton(
                    icon = painterResource(id = R.drawable.sports_tennis),
                    text = stringResource(R.string.tennis),
                    onClick = {}
                )
                SportButton(
                    icon = painterResource(id = R.drawable.exercise),
                    text = stringResource(R.string.exercise),
                    onClick = {}
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SportButton(
                    icon = painterResource(id = R.drawable.sports_basketball),
                    text = stringResource(R.string.basketball),
                    onClick = {}
                )
                SportButton(
                    icon = painterResource(id = R.drawable.sports_soccer),
                    text = stringResource(R.string.football),
                    onClick = {}
                )
            }
        }
    }
}