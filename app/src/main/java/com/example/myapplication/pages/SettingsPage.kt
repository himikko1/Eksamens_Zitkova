package com.example.myapplication.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.NotificationPermissionButton
import com.example.myapplication.models.AuthViewModel
import com.example.myapplication.ui.theme.ThemePreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit
) {
    var appSettingsExpanded by remember { mutableStateOf(false) }
    var notificationSettingsExpanded by remember { mutableStateOf(false) }
    var privacySettingsExpanded by remember { mutableStateOf(false) }
    var calorieCalculatorExpanded by remember { mutableStateOf(false) }
    var fastingSettingsExpanded by remember { mutableStateOf(false) }
    var healthToolsExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }

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

        SettingsSection(
            title = "Programmas iestatījumi",
            icon = Icons.Default.Settings,
            expanded = appSettingsExpanded,
            onToggle = { appSettingsExpanded = !appSettingsExpanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Contrast,
                        contentDescription = "Темная/Светлая тема",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Tumšā tēma")
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { newThemeValue ->
                        coroutineScope.launch {
                            themePreferences.setDarkTheme(newThemeValue)
                            onThemeToggle(newThemeValue)
                        }
                    }
                )
            }

            Button(
                onClick = { /* Языковые настройки */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Valodas iestatījumi")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(
            title = "Veselības iestatījumi",
            icon = Icons.Default.CalendarToday,
            expanded = healthToolsExpanded,
            onToggle = { healthToolsExpanded = !healthToolsExpanded }
        ) {
            Button(
                onClick = { navController.navigate("menstrual_calendar") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Kalendārs",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Kalendārs")
            }

            Button(
                onClick = { navController.navigate("training_videos") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = "Treniņš",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Тренировки")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(
            title = "Intervāla badošana",
            icon = Icons.Default.Timer,
            expanded = fastingSettingsExpanded,
            onToggle = { fastingSettingsExpanded = !fastingSettingsExpanded }
        ) {
            Button(
                onClick = { navController.navigate("intermittent_fasting") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = "Intervāla badošāna",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Sākt intervālu badošānu")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(
            title = "Kaloriju kalkulators",
            icon = Icons.Default.Calculate,
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
                    Icons.Default.Calculate,
                    contentDescription = "Kaloriju kalkulators",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Saskaitīt kalorijas")
            }

            Button(
                onClick = { navController.navigate("calorie_history") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Kalorijas vēsture")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(
            title = "Paziņojumu iestatījumi",
            icon = Icons.Default.PrivacyTip,
            expanded = privacySettingsExpanded,
            onToggle = { privacySettingsExpanded = !privacySettingsExpanded }
        ) {
            NotificationPermissionButton()
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
                        contentDescription = if (expanded) "Свернуть" else "Развернуть",
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

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
