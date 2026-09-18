package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BreakEvenAmber
import com.example.ui.theme.BreakEvenDark
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonLoss
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.CalendarMonth
import com.example.viewmodel.DayOutcome
import com.example.viewmodel.DayTradeSummary
import java.util.Locale

@Composable
fun InteractiveCalendarView(
    calendarMonth: CalendarMonth,
    selectedDateString: String?,
    onDateSelected: (String) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_calendar_view"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month Header with Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = calendarMonth.monthTitle.uppercase(Locale.US),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = TextPrimary
                        )
                        val mtdFormatted = String.format(
                            Locale.US,
                            "MTD: %s$%,.2f (%d trades)",
                            if (calendarMonth.monthNetPnL >= 0) "+" else "-",
                            kotlin.math.abs(calendarMonth.monthNetPnL),
                            calendarMonth.monthTradesCount
                        )
                        Text(
                            text = mtdFormatted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (calendarMonth.monthNetPnL >= 0) NeonEmerald else CrimsonLoss,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Month Switch Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorderSubtle, CircleShape)
                            .testTag("calendar_prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorderSubtle, CircleShape)
                            .testTag("calendar_next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day of Week Labels (SUN .. SAT)
            val dayOfWeekNames = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayOfWeekNames.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid (7 columns per row)
            val days = calendarMonth.days
            val rows = (days.size + 6) / 7

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (rowIndex in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (colIndex in 0 until 7) {
                            val itemIndex = rowIndex * 7 + colIndex
                            val daySummary = days.getOrNull(itemIndex)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            ) {
                                if (daySummary != null) {
                                    CalendarDayCell(
                                        summary = daySummary,
                                        isSelected = daySummary.dateString == selectedDateString,
                                        onClick = { onDateSelected(daySummary.dateString) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Color-Code Legend and Instructions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = NeonEmerald, label = "Win Day")
                LegendItem(color = CrimsonLoss, label = "Loss Day")
                LegendItem(color = BreakEvenAmber, label = "Break-Even")
                Text(
                    text = "Tap day to filter",
                    fontSize = 10.sp,
                    color = TextTertiary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    summary: DayTradeSummary,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val hasTrades = summary.tradesCount > 0

    // Outcome background and border styling
    val (bgColor, borderColor, textNumberColor) = when (summary.outcome) {
        DayOutcome.WIN -> Triple(EmeraldDark, NeonEmerald, NeonEmerald)
        DayOutcome.LOSS -> Triple(CrimsonDark, CrimsonLoss, CrimsonLoss)
        DayOutcome.BREAK_EVEN -> Triple(BreakEvenDark, BreakEvenAmber, BreakEvenAmber)
        DayOutcome.NONE -> Triple(Color(0xFF141A24), Color.Transparent, TextSecondary)
    }

    val finalBorderColor = if (isSelected) CyanAccent else borderColor
    val borderWidth = if (isSelected) 2.dp else if (hasTrades) 1.dp else 0.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, finalBorderColor, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .clickable { onClick() }
            .padding(2.dp)
            .testTag("calendar_day_${summary.dayOfMonth}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${summary.dayOfMonth}",
                fontSize = 12.sp,
                fontWeight = if (hasTrades || isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) CyanAccent else textNumberColor,
                fontFamily = FontFamily.Monospace
            )

            if (hasTrades) {
                // Indicator dots / trade count
                Row(
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(
                                when (summary.outcome) {
                                    DayOutcome.WIN -> NeonEmerald
                                    DayOutcome.LOSS -> CrimsonLoss
                                    DayOutcome.BREAK_EVEN -> BreakEvenAmber
                                    else -> TextSecondary
                                }
                            )
                    )
                    if (summary.tradesCount > 1) {
                        Text(
                            text = "${summary.tradesCount}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}
