@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication.ui.theme

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
//import com.maxkeppeler.sheets.core.models.base.rememberUseCaseState
import kotlinx.coroutines.launch

@Composable
fun Calendar() {
    // Izveidoju kalendāru , lietotjot maxkeppler
    val calendarState = rememberUseCaseState()
    val coroutineScope = rememberCoroutineScope()

    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            monthSelection = true,
            yearSelection = true,
            style = CalendarStyle.MONTH
        ),
        selection = CalendarSelection.Dates { dates ->
            Log.d("SelectedDate", "$dates") // ielogojām izvēlētos datus
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            coroutineScope.launch {
                calendarState.show() // button, lai atvēras kalendārs
            }
        }) {
            Text(text = "Date Picker")
        }
    }
}

@Preview
@Composable
fun PreviewCalendar() {
    Calendar()
}