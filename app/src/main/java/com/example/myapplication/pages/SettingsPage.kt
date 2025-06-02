package com.example.myapplication.pages



import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent

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

import androidx.compose.material.icons.filled.Calculate

import androidx.compose.material.icons.filled.CalendarToday

import androidx.compose.material.icons.filled.Contrast

import androidx.compose.material.icons.filled.Language // !!! NEW: Import for language icon !!!

import androidx.compose.material.icons.filled.PrivacyTip

import androidx.compose.material.icons.filled.Settings

import androidx.compose.material.icons.filled.Timer

import androidx.compose.material3.Button

import androidx.compose.material3.Card

import androidx.compose.material3.DropdownMenuItem // !!! NEW: For dropdown menu items !!!

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.ExposedDropdownMenuBox // !!! NEW: For the dropdown container !!!

import androidx.compose.material3.ExposedDropdownMenuDefaults // !!! NEW: For dropdown defaults like trailing icon !!!

import androidx.compose.material3.Icon

import androidx.compose.material3.IconButton

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Switch

import androidx.compose.material3.Text

import androidx.compose.material3.TextField // !!! NEW: For the dropdown text field !!!

import androidx.compose.runtime.Composable

import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.stringResource // !!! NEW: For string resources !!!

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController
import com.example.myapplication.MainActivity

import com.example.myapplication.NotificationPermissionButton

import com.example.myapplication.R // !!! Make sure R is imported for string resources !!!

import com.example.myapplication.models.AuthViewModel

//import com.example.myapplication.models.LanguageViewModel // !!! NEW: Import LanguageViewModel !!!

import com.example.myapplication.models.ThemeViewModel




@SuppressLint("ContextCastToActivity")

@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun SettingsPage(

    modifier: Modifier = Modifier,

    navController: NavController,

    authViewModel: AuthViewModel,

    themeViewModel: ThemeViewModel = viewModel() // ThemeViewModel is passed here

    //languageViewModel: LanguageViewModel = viewModel() // !!! NEW: LanguageViewModel is passed here !!!

) {

    var appSettingsExpanded by remember { mutableStateOf(false) }

    var notificationSettingsExpanded by remember { mutableStateOf(false) }

    var privacySettingsExpanded by remember { mutableStateOf(false) }

    var calorieCalculatorExpanded by remember { mutableStateOf(false) }

    var fastingSettingsExpanded by remember { mutableStateOf(false) }

    var healthToolsExpanded by remember { mutableStateOf(false) }



    val coroutineScope = rememberCoroutineScope()



// Observe the current theme state from the ViewModel

    val isDarkThemeFromViewModel by themeViewModel.isDarkTheme.collectAsState(initial = false)



// Observe the current language state from the ViewModel

   // val currentLanguage by languageViewModel.currentLanguage.collectAsState()



// Get the current Activity to be able to recreate it for language changes

    val activity = LocalContext.current as Activity // Cast to android.app.Activity



    Column(

        modifier = modifier

            .fillMaxSize()

            .padding(16.dp),

        verticalArrangement = Arrangement.Top,

        horizontalAlignment = Alignment.Start

    ) {

        Text(

            text = stringResource(R.string.settings_title), // Use string resource

            fontSize = 24.sp,

            fontWeight = FontWeight.Bold,

            modifier = Modifier.padding(bottom = 24.dp)

        )



// --- Programmas iestatījumi (App Settings) section ---

        SettingsSection(

            title = stringResource(R.string.app_settings), // Use string resource

            icon = Icons.Default.Settings,

            expanded = appSettingsExpanded,

            onToggle = { appSettingsExpanded = !appSettingsExpanded }

        ) {



// Dark Theme Switch

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

                        contentDescription = stringResource(R.string.dark_theme), // Use string resource

                        modifier = Modifier.padding(end = 8.dp)

                    )

                    Text(stringResource(R.string.dark_theme)) // Use string resource

                }

                Switch(

                    checked = isDarkThemeFromViewModel,

                    onCheckedChange = { newThemeValue ->

                        themeViewModel.setDarkTheme(newThemeValue)

                    }

                )

            }



            Spacer(modifier = Modifier.height(16.dp)) // Spacing before language selector



/*// !!! LANGUAGE SWITCH UI !!!

            var languageDropdownExpanded by remember { mutableStateOf(false) }

            val languageOptions = listOf("en", "lv") // Your supported language codes



// Map language codes to their display names using string resources

            val languageDisplayNames = mapOf(

                "en" to stringResource(R.string.language_english),

                "lv" to stringResource(R.string.language_latvian)

            )



            ExposedDropdownMenuBox(

                expanded = languageDropdownExpanded,

                onExpandedChange = { languageDropdownExpanded = !languageDropdownExpanded },

                modifier = Modifier.fillMaxWidth()

            ) {

                TextField(

                    readOnly = true,

                    value = languageDisplayNames[currentLanguage] ?: stringResource(R.string.select_language),

                    onValueChange = { },

                    label = { Text(stringResource(R.string.language_settings)) }, // Use string resource

                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageDropdownExpanded) },

                    modifier = Modifier

                        .menuAnchor() // This modifier is crucial for the dropdown to anchor to the TextField

                        .fillMaxWidth()

                )



                ExposedDropdownMenu(

                    expanded = languageDropdownExpanded,

                    onDismissRequest = { languageDropdownExpanded = false }

                ) {

                    languageOptions.forEach { langCode ->

                        DropdownMenuItem(

                            text = { Text(languageDisplayNames[langCode] ?: langCode) },

                            onClick = {

                                languageViewModel.setLanguage(langCode) // Save the new language

                                languageDropdownExpanded = false

// !!! CRUCIAL: RECREATE THE ACTIVITY TO APPLY THE NEW LOCALE !!!

                                activity.recreate()

                            }

                        )

                    }

                }

            }*/

        }







// --- Veselības iestatījumi (Health Tools) section ---

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(

            title = "Veselības iestatījumi", // Placeholder, consider a string resource

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

                    contentDescription = "Kalendārs", // Placeholder

                    modifier = Modifier.padding(end = 8.dp)

                )

                Text("Kalendārs") // Placeholder

            }



            Button(

                onClick = { navController.navigate("training_videos") },

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(vertical = 8.dp)

            ) {

                Icon(

                    Icons.Default.Timer,

                    contentDescription = "Treniņš", // Placeholder

                    modifier = Modifier.padding(end = 8.dp)

                )

                Text("Apmācību video") // Placeholder

            }

        }







// --- Intervāla badošana (Intermittent Fasting) section ---

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(

            title = "Intervāla badošana", // Placeholder

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

                    contentDescription = "Intervāla badošāna", // Placeholder

                    modifier = Modifier.padding(end = 8.dp)

                )

                Text("Sākt intervālu badošānu") // Placeholder

            }

        }







// --- Kaloriju kalkulators (Calorie Calculator) section ---

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(

            title = "Kaloriju kalkulators", // Placeholder

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

                    contentDescription = "Kaloriju kalkulators", // Placeholder

                    modifier = Modifier.padding(end = 8.dp)

                )

                Text("Saskaitīt kalorijas") // Placeholder

            }



            Button(

                onClick = { navController.navigate("calorie_history") },

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(vertical = 8.dp)

            ) {

                Text("Kalorijas vēsture") // Placeholder

            }

        }







// --- Paziņojumu iestatījumi (Notification Settings) section ---

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(

            title = "Paziņojumu iestatījumi", // Placeholder

            icon = Icons.Default.PrivacyTip,

            expanded = privacySettingsExpanded,

            onToggle = { privacySettingsExpanded = !privacySettingsExpanded }

        ) {

            NotificationPermissionButton()

        }

    }

}


// --- SettingsSection Composable (remains unchanged) ---

@Composable

fun SettingsSection(

    title: String,

    icon: ImageVector,

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

                        contentDescription = if (expanded) "Свернуть" else "Развернуть", // Placeholder

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