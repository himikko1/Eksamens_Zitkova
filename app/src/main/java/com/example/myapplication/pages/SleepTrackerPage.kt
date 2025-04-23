//package com.example.myapplication.pages
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.DateRange
//import androidx.compose.material.icons.filled.NightsStay
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.fragment.app.FragmentManager
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.example.myapplication.models.SleepViewModel
//import com.example.myapplication.models.StatsPeriod
//import com.example.myapplication.models.SleepRecord
//import com.example.myapplication.utils.*
//import com.google.android.material.datepicker.MaterialDatePicker
//import com.google.android.material.timepicker.MaterialTimePicker
//import com.google.android.material.timepicker.TimeFormat
//import kotlinx.datetime.*
//import java.time.format.DateTimeFormatter
//import java.time.LocalTime
//import java.time.LocalDate
//
//// Объявляем форматтеры для отображения времени и даты
//val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
//val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
//val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM HH:mm")
//
//@Composable
//fun SleepTrackerPage(
//    navController: NavController,
//    fragmentManager: FragmentManager,
//    sleepViewModel: SleepViewModel = viewModel()
//) {
//    // Собираем состояние из ViewModel
//    val selectedDate by sleepViewModel.selectedDate.collectAsState()
//    val startTime by sleepViewModel.startTime.collectAsState()
//    val endTime by sleepViewModel.endTime.collectAsState()
//    val durationText by sleepViewModel.durationText.collectAsState()
//    val stats by sleepViewModel.sleepStats.collectAsState()
//    val isLoading by sleepViewModel.isLoading.collectAsState()
//    val errorMessage by sleepViewModel.errorMessage.collectAsState()
//
//    var selectedStatsPeriod by remember { mutableStateOf(StatsPeriod.WEEK) }
//
//    // Загрузка статистики при первом открытии экрана или смене периода
//    LaunchedEffect(selectedStatsPeriod) {
//        sleepViewModel.fetchSleepData(selectedStatsPeriod)
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Отслеживание сна", style = MaterialTheme.typography.headlineMedium)
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // --- Блок ввода данных ---
//        Card(modifier = Modifier.fillMaxWidth()) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                // Выбор даты
//                DateSelector(
//                    selectedDate = selectedDate,
//                    fragmentManager = fragmentManager,
//                    onDateSelected = { millis ->
//                        val instant = Instant.fromEpochMilliseconds(millis)
//                        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
//                        sleepViewModel.onDateChange(localDate)
//                    }
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Выбор времени начала и конца
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceAround
//                ) {
//                    TimeSelector(
//                        label = "Лег спать",
//                        selectedTime = startTime,
//                        fragmentManager = fragmentManager,
//                        onTimeSelected = { hour, minute ->
//                            sleepViewModel.onStartTimeChange(kotlinx.datetime.LocalTime(hour, minute))
//                        }
//                    )
//                    TimeSelector(
//                        label = "Проснулся",
//                        selectedTime = endTime,
//                        fragmentManager = fragmentManager,
//                        onTimeSelected = { hour, minute ->
//                            sleepViewModel.onEndTimeChange(kotlinx.datetime.LocalTime(hour, minute))
//                        }
//                    )
//                }
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Отображение длительности
//                durationText?.let {
//                    Text(it, style = MaterialTheme.typography.bodyLarge)
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//
//                // Кнопка сохранения
//                Button(
//                    onClick = { sleepViewModel.saveSleepRecord() },
//                    enabled = startTime != null && endTime != null && !isLoading
//                ) {
//                    // Показываем прогресс только во время операции сохранения
//                    val isSaving = isLoading && startTime != null && endTime != null
//                    if (isSaving) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(24.dp),
//                            color = MaterialTheme.colorScheme.onPrimary,
//                            strokeWidth = 2.dp
//                        )
//                    } else {
//                        Text("Сохранить")
//                    }
//                }
//
//                // Отображение ошибок ввода/сохранения
//                errorMessage?.let {
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        it,
//                        color = MaterialTheme.colorScheme.error,
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // --- Блок статистики ---
//        Text("Статистика", style = MaterialTheme.typography.headlineSmall)
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Переключатель периода
//        SegmentedButtonPeriod(
//            selectedPeriod = selectedStatsPeriod,
//            onPeriodSelected = { newPeriod ->
//                if (newPeriod != selectedStatsPeriod) {
//                    selectedStatsPeriod = newPeriod
//                }
//            }
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Отображение статистики или индикатора загрузки/пустого состояния
//        Box(modifier = Modifier.fillMaxSize()) {
//            if (isLoading) {
//                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//            } else if (stats.isEmpty()) {
//                Text("Нет данных за выбранный период.", modifier = Modifier.align(Alignment.Center))
//            } else {
//                LazyColumn(modifier = Modifier.fillMaxWidth()) {
//                    items(stats, key = { it.id }) { record ->
//                        SleepStatItem(record)
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun DateSelector(
//    selectedDate: kotlinx.datetime.LocalDate,
//    fragmentManager: FragmentManager,
//    onDateSelected: (Long) -> Unit
//) {
//    val javaDate = remember(selectedDate) {
//        selectedDate.toJavaLocalDate()
//    }
//    val formattedDate = remember(javaDate) {
//        javaDate.format(dateFormatter)
//    }
//
//    OutlinedButton(onClick = {
//        val picker = MaterialDatePicker.Builder.datePicker()
//            .setTitleText("Выберите дату сна")
//            .setSelection(selectedDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())
//            .build()
//
//        picker.addOnPositiveButtonClickListener { selection ->
//            onDateSelected(selection)
//        }
//        picker.show(fragmentManager, "DATE_PICKER")
//    }) {
//        Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату", modifier = Modifier.size(18.dp))
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(formattedDate)
//    }
//}
//
//@Composable
//fun TimeSelector(
//    label: String,
//    selectedTime: kotlinx.datetime.LocalTime?,
//    fragmentManager: FragmentManager,
//    onTimeSelected: (hour: Int, minute: Int) -> Unit
//) {
//    val formattedTime = remember(selectedTime) {
//        selectedTime?.toJavaLocalTime()?.format(timeFormatter) ?: "Время"
//    }
//
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Text(label, style = MaterialTheme.typography.labelMedium)
//        OutlinedButton(onClick = {
//            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
//            val currentHour = selectedTime?.hour ?: now.hour
//            val currentMinute = selectedTime?.minute ?: now.minute
//            val picker = MaterialTimePicker.Builder()
//                .setTimeFormat(TimeFormat.CLOCK_24H)
//                .setHour(currentHour)
//                .setMinute(currentMinute)
//                .setTitleText(label)
//                .build()
//
//            picker.addOnPositiveButtonClickListener {
//                onTimeSelected(picker.hour, picker.minute)
//            }
//            picker.show(fragmentManager, "TIME_PICKER_$label")
//        }) {
//            Icon(Icons.Default.NightsStay, contentDescription = label, modifier = Modifier.size(18.dp))
//            Spacer(modifier = Modifier.width(8.dp))
//            Text(formattedTime)
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SegmentedButtonPeriod(
//    selectedPeriod: StatsPeriod,
//    onPeriodSelected: (StatsPeriod) -> Unit
//) {
//    val items = StatsPeriod.values()
//    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
//        items.forEachIndexed { index, period ->
//            SegmentedButton(
//                shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size),
//                onClick = { onPeriodSelected(period) },
//                selected = period == selectedPeriod,
//                icon = {}
//            ) {
//                Text(
//                    text = when(period) {
//                        StatsPeriod.WEEK -> "Неделя"
//                        StatsPeriod.MONTH -> "Месяц"
//                        StatsPeriod.YEAR -> "Год"
//                    }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun SleepStatItem(record: SleepRecord) {
//    val zone = TimeZone.currentSystemDefault()
//    val startTimeLocal = remember(record.startTime) {
//        record.startTime.toJavaInstant().atZone(zone.toJavaZoneId()).toLocalDateTime()
//    }
//    val endTimeLocal = remember(record.endTime) {
//        record.endTime.toJavaInstant().atZone(zone.toJavaZoneId()).toLocalDateTime()
//    }
//
//    Card(modifier = Modifier
//        .fillMaxWidth()
//        .padding(vertical = 4.dp)) {
//        Row(
//            modifier = Modifier.padding(12.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    "С: ${startTimeLocal.format(dateTimeFormatter)}",
//                    style = MaterialTheme.typography.bodyMedium
//                )
//                Text(
//                    "До: ${endTimeLocal.format(dateTimeFormatter)}",
//                    style = MaterialTheme.typography.bodyMedium
//                )
//            }
//            Spacer(modifier = Modifier.width(16.dp))
//            Text(
//                "${record.durationMinutes / 60} ч ${record.durationMinutes % 60} м",
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//    }
//}