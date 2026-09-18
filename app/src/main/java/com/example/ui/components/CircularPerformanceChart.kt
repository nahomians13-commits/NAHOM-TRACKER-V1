package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.PerformanceStats
import java.util.Locale

@Composable
fun CircularPerformanceChart(
    stats: PerformanceStats,
    modifier: Modifier = Modifier
) {
    val total = stats.totalTrades

    // Proportions
    val winFraction = if (total > 0) (stats.wins.toFloat() / total.toFloat()) else 0f
    val lossFraction = if (total > 0) (stats.losses.toFloat() / total.toFloat()) else 0f
    val beFraction = if (total > 0) (stats.breakEvens.toFloat() / total.toFloat()) else 0f

    // Animated progress
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(stats.totalTrades, stats.wins, stats.losses, stats.breakEvens) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("circular_performance_chart")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OUTCOME DISTRIBUTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF263346))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "W:L ${stats.winLossRatioText}",
                        color = CyanAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Donut Chart
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(122.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                        // Empty Track
                        drawArc(
                            color = Color(0xFF192231),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        if (total > 0) {
                            var currentStartAngle = -90f
                            val progress = animationProgress.value

                            // Win Segment (Green)
                            val winSweep = (winFraction * 360f) * progress
                            if (winSweep > 0.5f) {
                                drawArc(
                                    color = NeonEmerald,
                                    startAngle = currentStartAngle,
                                    sweepAngle = (winSweep - if (lossFraction > 0 || beFraction > 0) 3f else 0f).coerceAtLeast(1f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                currentStartAngle += winSweep
                            }

                            // Loss Segment (Red)
                            val lossSweep = (lossFraction * 360f) * progress
                            if (lossSweep > 0.5f) {
                                drawArc(
                                    color = CrimsonLoss,
                                    startAngle = currentStartAngle,
                                    sweepAngle = (lossSweep - if (winFraction > 0 || beFraction > 0) 3f else 0f).coerceAtLeast(1f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                currentStartAngle += lossSweep
                            }

                            // Break-Even Segment (Amber)
                            val beSweep = (beFraction * 360f) * progress
                            if (beSweep > 0.5f) {
                                drawArc(
                                    color = BreakEvenAmber,
                                    startAngle = currentStartAngle,
                                    sweepAngle = (beSweep - if (winFraction > 0 || lossFraction > 0) 3f else 0f).coerceAtLeast(1f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                        }
                    }

                    // Donut Center Text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.0f%%", stats.winRatePercent),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = if (stats.winRatePercent >= 50) NeonEmerald else TextPrimary,
                            letterSpacing = (-0.5).sp,
                            modifier = Modifier.testTag("circular_win_rate_text")
                        )
                        Text(
                            text = "WIN RATE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Breakdown Legend
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutcomeLegendRow(
                        label = "WINS",
                        percent = stats.winRatePercent,
                        count = stats.wins,
                        accentColor = NeonEmerald,
                        bgColor = EmeraldDark,
                        testTag = "circular_legend_wins"
                    )

                    OutcomeLegendRow(
                        label = "LOSSES",
                        percent = stats.lossRatePercent,
                        count = stats.losses,
                        accentColor = CrimsonLoss,
                        bgColor = CrimsonDark,
                        testTag = "circular_legend_losses"
                    )

                    OutcomeLegendRow(
                        label = "BREAK-EVEN",
                        percent = stats.breakEvenRatePercent,
                        count = stats.breakEvens,
                        accentColor = BreakEvenAmber,
                        bgColor = BreakEvenDark,
                        testTag = "circular_legend_break_even"
                    )
                }
            }
        }
    }
}

@Composable
private fun OutcomeLegendRow(
    label: String,
    percent: Double,
    count: Int,
    accentColor: Color,
    bgColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF131A26))
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = String.format(Locale.US, "%.1f%%", percent),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "($count)",
                fontSize = 11.sp,
                color = TextTertiary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
