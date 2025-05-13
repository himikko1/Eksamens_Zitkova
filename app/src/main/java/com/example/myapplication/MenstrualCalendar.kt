package com.example.myapplication.components

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.* // THIS IMPORTS ALL ESSENTIAL COMPOSE RUNTIME COMPONENTS, INCLUDING getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.MenstrualCalendarViewModel
import kotlinx.coroutines.flow.StateFlow // Explicitly import StateFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// Define a sealed class or enum for the selection mode
sealed class SelectionMode {
    object None : SelectionMode() // No selection action expected on click
    object SelectingStartDate : SelectionMode() // Next click sets start date
    object SelectingEndDate : SelectionMode() // Next click sets end date
    // You can optionally add MarkingIndividualDate if you want a separate mode for it
    // object MarkingIndividualDate : SelectionMode() // Next click toggles individual mark
}

@Composable
fun MenstrualCalendar(
    viewModel: MenstrualCalendarViewModel,
    modifier: Modifier = Modifier
) {
    // Ensure you have 'import androidx.compose.runtime.getValue' or 'import androidx.compose.runtime.*'
    val cycleData by viewModel.cycleData.collectAsState()

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    // Ensure Locale.getDefault() is appropriate for your target language/region for day names
    val daysOfWeek = remember { DayOfWeek.values().map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) } }

    // UI State for Selection Mode
    var selectionMode by remember { mutableStateOf<SelectionMode>(SelectionMode.None) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий месяц")
            }

            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM uuuu", Locale.getDefault())), // Use Locale for month name
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий месяц")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days of the week header
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = false
        ) {
            items(daysOfWeek) { day ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Calendar Grid
        CalendarGrid(
            currentMonth = currentMonth,
            viewModel = viewModel,
            selectionMode = selectionMode, // Pass the selection mode
            onDateClick = { date -> // Define the onDateClick logic based on selectionMode
                when (selectionMode) {
                    is SelectionMode.SelectingStartDate -> {
                        viewModel.setStartDate(date)
                        selectionMode = SelectionMode.None // Reset mode after selection
                    }
                    is SelectionMode.SelectingEndDate -> {
                        viewModel.setEndDate(date)
                        selectionMode = SelectionMode.None // Reset mode after selection
                    }
                    is SelectionMode.None -> {
                        // Default click action when not in a specific selection mode
                        viewModel.toggleDateMark(date)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Обозначения:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Period
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFFE57373), CircleShape) // Период
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Период")
            }

            // Marked day (individual, not part of period)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .border(BorderStroke(2.dp, Color.Red), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Отмеченный день (вне цикла)") // Clarified legend for marked day
            }

            // Today's date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LocalDate.now().dayOfMonth.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Сегодня")
            }

            // Cycle Start/End date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .border(BorderStroke(2.dp, Color.Red), CircleShape), // Red border for start/end
                    contentAlignment = Alignment.Center
                ) {
                    // No text, visual style indicates it
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Начало/Конец цикла")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons to set cycle start/end
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    selectionMode = SelectionMode.SelectingStartDate
                    // Optional: Show a snackbar or toast to guide the user
                    // scaffoldState.snackbarHostState.showSnackbar("Выберите дату начала цикла")
                },
                modifier = Modifier.weight(1f),
                colors = if (selectionMode is SelectionMode.SelectingStartDate)
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                else
                    ButtonDefaults.buttonColors() // Default colors
            ) {
                Text("Cikla sākums")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    selectionMode = SelectionMode.SelectingEndDate
                    // Optional: Show a snackbar or toast to guide the user
                    // scaffoldState.snackbarHostState.showSnackbar("Выберите дату конца цикла")
                },
                modifier = Modifier.weight(1f),
                colors = if (selectionMode is SelectionMode.SelectingEndDate)
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                else
                    ButtonDefaults.buttonColors() // Default colors
            ) {
                Text("Cikla beigas")
            }
        }

        // Button to cancel current selection mode
        Button(
            onClick = { selectionMode = SelectionMode.None },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = if (selectionMode != SelectionMode.None)
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            else
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
        ) {
            Text("Отменить выбор даты")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current cycle information
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Информация о цикле",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Начало цикла: ${cycleData.startDate?.let {
                        LocalDate.parse(it, DateTimeFormatter.ISO_DATE).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    } ?: "Не установлено"}",
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "Конец цикла: ${cycleData.endDate?.let {
                        LocalDate.parse(it, DateTimeFormatter.ISO_DATE).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    } ?: "Не установлено"}",
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "Отмеченных дней: ${cycleData.markedDates.size}",
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Display current selection mode for user feedback (optional but recommended)
                Text(
                    text = "Режим выбора: ${
                        when(selectionMode) {
                            is SelectionMode.None -> "Нет"
                            is SelectionMode.SelectingStartDate -> "Выбор начала"
                            is SelectionMode.SelectingEndDate -> "Выбор конца"
                        }
                    }",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = if (selectionMode != SelectionMode.None) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    viewModel: MenstrualCalendarViewModel,
    selectionMode: SelectionMode,
    onDateClick: (LocalDate) -> Unit // Lambda to handle date clicks
) {
    // Calculate days for the grid
    val calendarDays = remember(currentMonth) {
        val firstDayOfMonth = currentMonth.atDay(1)
        val lastDayOfMonth = currentMonth.atEndOfMonth()

        // Assuming Monday is the first day of the week for grid display
        val firstDayOfWeekValue = DayOfWeek.MONDAY.value
        val firstDayOfGrid = firstDayOfMonth.minusDays(
            (firstDayOfMonth.dayOfWeek.value - firstDayOfWeekValue + 7) % 7.toLong()
        )

        // Generate 42 days for a consistent 6-week grid
        (0 until 42).map { firstDayOfGrid.plusDays(it.toLong()) }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        userScrollEnabled = false
    ) {
        items(calendarDays) { date ->
            DayCell(
                date = date,
                isCurrentMonth = date.month == currentMonth.month && date.year == currentMonth.year,
                isMarked = viewModel.isDateMarked(date),
                isInPeriod = viewModel.isInPeriod(date),
                // Pass the received onDateClick lambda to DayCell
                onDateClick = { onDateClick(date) },
                viewModel = viewModel // ViewModel is passed down for direct state checks
            )
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isMarked: Boolean,
    isInPeriod: Boolean,
    onDateClick: () -> Unit,
    viewModel: MenstrualCalendarViewModel // ViewModel used for isStartDate/isEndDate
) {
    val isToday = date == LocalDate.now()
    val isStart = viewModel.isStartDate(date)
    val isEnd = viewModel.isEndDate(date)

    val bgColor = when {
        isInPeriod -> Color(0xFFE57373) // Period dates
        isStart || isEnd -> MaterialTheme.colorScheme.surfaceVariant // Start/End dates
        isMarked -> MaterialTheme.colorScheme.surfaceVariant // Individually marked dates
        else -> Color.Transparent
    }

    // Border for Start or End date
    val border = if (isStart || isEnd) BorderStroke(2.dp, Color.Red) else null

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(bgColor, CircleShape)
            .then(if (border != null) Modifier.border(border, CircleShape) else Modifier)
            .clickable { onDateClick() }, // Calls the lambda passed from CalendarGrid

        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = when {
                !isCurrentMonth -> Color.Gray // Dates outside current month
                isInPeriod -> MaterialTheme.colorScheme.onPrimary // Text for period dates
                isStart || isEnd || isMarked -> MaterialTheme.colorScheme.onSurfaceVariant // Text for special marked dates
                else -> MaterialTheme.colorScheme.onSurface // Default text color
            },
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// Helper function to observe StateFlows as Compose State.
// Make sure this helper is in a file accessible by your Composables.
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun <T> StateFlow<T>.collectAsState(): State<T> {
    // This uses the built-in collectAsState from androidx.compose.runtime.livedata or runtime-rxjava2.
    // Ensure you have the appropriate dependency (e.g., 'androidx.compose.runtime:runtime-livedata')
    // or just 'androidx.compose.runtime:runtime-ktx' might be sufficient depending on Compose version.
    return collectAsState(initial = value)
}