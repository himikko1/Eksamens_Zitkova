package com.example.myapplication.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Calculate // Import the correct icon
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.models.AuthViewModel
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    // Atvērto/aizvērto nodalījumu izsekošanas stāvoklis
    var appSettingsExpanded by remember { mutableStateOf(false) }
    var notificationSettingsExpanded by remember { mutableStateOf(false) }
    var privacySettingsExpanded by remember { mutableStateOf(false) }
    var calorieCalculatorExpanded by remember { mutableStateOf(false) }

    // Kalendāra stāvokļa izveide
    val calendarState = rememberUseCaseState()
    val coroutineScope = rememberCoroutineScope()

    // Kalendāra dialogs
    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            monthSelection = true,
            yearSelection = true,
            style = CalendarStyle.MONTH
        ),
        selection = CalendarSelection.Dates { dates ->
            // Izvēlēto datumu apstrāde
            println("Izvēlētie datumi: $dates")
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Iestatījumi",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Lietojumprogrammas iestatījumu sadaļa
        SettingsSection(
            title = "Lietojumprogrammas iestatījumi",
            icon = Icons.Default.Settings,
            expanded = appSettingsExpanded,
            onToggle = { appSettingsExpanded = !appSettingsExpanded }
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        calendarState.show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = "Kalendārs",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Atvērt kalendāru")
            }

            Button(
                onClick = { /* fun for theme */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Tēmas iestatījumi")
            }

            Button(
                onClick = { /* func for lang */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Valodas iestatījumi")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Update the SettingsSection for the calorie calculator
        SettingsSection(
            title = "Kaloriju kalkulators",
            icon = Icons.Default.Calculate, // Use the correct icon here
            expanded = calorieCalculatorExpanded,
            onToggle = { calorieCalculatorExpanded = !calorieCalculatorExpanded }
        ) {
            Button(
                onClick = { navController.navigate("calorie_calculator") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Calculate, // Use the correct icon here
                    contentDescription = "Kaloriju kalkulators",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Aprēķināt kalorijas")
            }

            Button(
                onClick = { navController.navigate("calorie_history") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Kaloriju vēsture")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // kind of notif
        SettingsSection(
            title = "Paziņojumu iestatījumi",
            icon = Icons.Default.Notifications,
            expanded = notificationSettingsExpanded,
            onToggle = { notificationSettingsExpanded = !notificationSettingsExpanded }
        ) {
            Button(
                onClick = { /* fun for notif*/ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Ieslēgt/izslēgt paziņojumus")
            }

            Button(
                onClick = { /* fin for sound*/ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Skaņas iestatījumi")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Konfidencialitātes sadaļa
        SettingsSection(
            title = "Konfidencialitātes iestatījumi",
            icon = Icons.Default.PrivacyTip,
            expanded = privacySettingsExpanded,
            onToggle = { privacySettingsExpanded = !privacySettingsExpanded }
        ) {
            Button(
                onClick = { /* fun for dates */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Datu pārvaldība")
            }

            Button(
                onClick = { /* fun for resol */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Atļauju iestatījumi")
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Sadaļas galvene ar nolaižamo pogu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = onToggle) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = if (expanded) "Salikt" else "Izvērst",
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            // Sadaļas saturs (pogas)
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    content()
                }
            }
        }
    }
}