package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.example.viewmodel.MonthlyPerformanceSummary
import java.util.Locale

@Composable
fun MonthlyPerformanceCard(
    summary: MonthlyPerformanceSummary,
    modifier: Modifier = Modifier
) {
    val isProfitable = summary.totalPnL >= 0.0
    val pnlColor = if (isProfitable) NeonEmerald else CrimsonLoss
    val pnlBg = if (isProfitable) EmeraldDark.copy(alpha = 0.45f) else CrimsonDark.copy(alpha = 0.45f)

    val pnlFormatted = String.format(
        Locale.US,
        "%s$%,.2f",
        if (summary.totalPnL > 0) "+" else if (summary.totalPnL < 0) "-" else "",
        kotlin.math.abs(summary.totalPnL)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_performance_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Section Title & Calendar Month Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(1.dp, CyanAccent.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "MONTHLY PERFORMANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = summary.monthTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Status Pill: Month-to-date summary
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(pnlBg)
                        .border(1.dp, pnlColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isProfitable) "NET PROFITABLE" else "DRAWDOWN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PRIMARY METRIC: Total Profit / Loss (Large Highlight Box)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DarkSurfaceElevated,
                                DarkSurfaceElevated.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .border(1.dp, pnlColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
                    .testTag("monthly_total_pnl")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL MONTHLY PnL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextTertiary,
                            letterSpacing = 1.1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pnlFormatted,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = pnlColor,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Win Rate: ${String.format(Locale.US, "%.1f%%", summary.winRatePercent)} • ${summary.winsCount}W / ${summary.lossesCount}L",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Indicator Circle Icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(pnlBg)
                            .border(1.dp, pnlColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isProfitable) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = pnlColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SECONDARY DUAL METRIC ROW: Trades Taken & Average R:R
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 2: Trades Taken
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                        .testTag("monthly_trades_taken")
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "TRADES TAKEN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextTertiary,
                                letterSpacing = 0.8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${summary.tradesTaken}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Mini breakdown dots
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${summary.winsCount}W",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(text = "•", fontSize = 10.sp, color = TextTertiary)
                            Text(
                                text = "${summary.lossesCount}L",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonLoss,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(text = "•", fontSize = 10.sp, color = TextTertiary)
                            Text(
                                text = "${summary.breakEvenCount}BE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BreakEvenAmber,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Metric 3: Average R:R (Risk-to-Reward)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                        .testTag("monthly_avg_rr")
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Scale,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "AVERAGE R:R",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextTertiary,
                                letterSpacing = 0.8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = summary.averageRiskRewardText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanAccent,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (summary.averageRiskReward >= 2.0) "Optimal (≥ 1:2.0)" else "Realized R:R Ratio",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOTTOM SUMMARY STRIP: Profit Factor & Best Trade
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF131722))
                    .border(1.dp, DarkBorderSubtle, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profit Factor
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Profit Factor: ",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f", summary.profitFactor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.profitFactor >= 1.5) NeonEmerald else TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Best Trade in Month
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Best Trade: ",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "%s$%,.2f",
                            if (summary.bestTradePnL > 0) "+" else "",
                            summary.bestTradePnL
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.bestTradePnL > 0) NeonEmerald else TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
