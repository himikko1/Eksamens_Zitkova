package com.example.myapplication.components

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.MenstrualCalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun MenstrualCalendar(
    viewModel: MenstrualCalendarViewModel,
    modifier: Modifier = Modifier
) {
    val cycleData by viewModel.cycleData.collectAsState()

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val daysOfWeek = remember { DayOfWeek.values().map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий месяц")
            }

            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий месяц")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Дни недели
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

        // Сетка календаря
        CalendarGrid(
            currentMonth = currentMonth,
            viewModel = viewModel
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Легенда
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFFE57373), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Период")
            }

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
                Text("Отмеченный день")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопки для установки начала и конца цикла
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    viewModel.setStartDate(LocalDate.now())
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cikla sākums")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    viewModel.setEndDate(LocalDate.now())
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cikla beigas")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Информация о текущем цикле
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
            }
        }
    }
}



@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    viewModel: MenstrualCalendarViewModel
) {
    // Получаем дни для отображения в сетке
    val calendarDays = remember(currentMonth) {
        val firstDayOfMonth = currentMonth.atDay(1)
        val lastDayOfMonth = currentMonth.atEndOfMonth()

        // Добавляем дни из предыдущего месяца для заполнения первой недели
        val dayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        val prevMonthDays = (0 until dayOfWeek).map {
            firstDayOfMonth.minusDays((dayOfWeek - it).toLong())
        }

        // Дни текущего месяца
        val currentMonthDays = (1..lastDayOfMonth.dayOfMonth).map {
            currentMonth.atDay(it)
        }

        // Добавляем дни следующего месяца для заполнения последней недели
        val daysFromCurrentMonth = prevMonthDays.size + currentMonthDays.size
        val nextMonthDays = (1..42 - daysFromCurrentMonth).map {
            lastDayOfMonth.plusDays(it.toLong())
        }

        prevMonthDays + currentMonthDays + nextMonthDays
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        userScrollEnabled = false
    ) {
        items(calendarDays) { date ->
            DayCell(
                date = date,
                isCurrentMonth = date.month == currentMonth.month,
                isMarked = viewModel.isDateMarked(date),
                isInPeriod = viewModel.isInPeriod(date),
                onDateClick = {
                    viewModel.toggleDateMark(date)
                },
                viewModel = viewModel  // Pass the viewModel to DayCell
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
    viewModel: MenstrualCalendarViewModel
) {
    val isToday = date == LocalDate.now()
    val isStart = viewModel.isStartDate(date)
    val isEnd = viewModel.isEndDate(date)

    val bgColor = when {
        isStart || isEnd -> MaterialTheme.colorScheme.surfaceVariant
        isInPeriod -> Color(0xFFE57373) // Цвет для периода
        isMarked -> MaterialTheme.colorScheme.surfaceVariant // Цвет для отмеченного дня
        else -> Color.Transparent
    }

    val border = if (isStart || isEnd) BorderStroke(2.dp, Color.Red) else null

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onDateClick() }
            .then(if (border != null) Modifier.border(border, CircleShape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = if (!isCurrentMonth) Color.Gray else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}



