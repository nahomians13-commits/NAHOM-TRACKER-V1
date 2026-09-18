package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notes
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.PositionType
import com.example.data.Trade
import com.example.data.TradeResult
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TradeItemCard(
    trade: Trade,
    onDeleteTrade: (Trade) -> Unit,
    onViewScreenshot: (String) -> Unit,
    onTradeClick: (Trade) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val resultColor = when (trade.result) {
        TradeResult.WIN -> NeonEmerald
        TradeResult.LOSS -> CrimsonLoss
        TradeResult.BREAK_EVEN -> BreakEvenAmber
    }

    val resultBg = when (trade.result) {
        TradeResult.WIN -> EmeraldDark
        TradeResult.LOSS -> CrimsonDark
        TradeResult.BREAK_EVEN -> BreakEvenDark
    }

    val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US)
    val formattedDate = sdf.format(Date(trade.dateEpochMillis))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTradeClick(trade) }
            .testTag("trade_card_${trade.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Asset, Position Type, Result Pill, PnL, Delete Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = trade.asset,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )

                    // Position Type Badge (LONG / SHORT)
                    val isLong = trade.positionType == PositionType.LONG
                    val posColor = if (isLong) NeonEmerald else CrimsonLoss
                    val posBg = if (isLong) EmeraldDark.copy(alpha = 0.6f) else CrimsonDark.copy(alpha = 0.6f)

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(posBg)
                            .border(1.dp, posColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLong) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = posColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = trade.positionType.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = posColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Result Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(resultBg)
                            .border(1.dp, resultColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = trade.result.name.replace("_", " "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = resultColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // R:R Pill
                    val rrFormatted = if (trade.riskRewardRatio > 0.0) "1:${trade.riskRewardRatio}" else "1:2.0"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF131D2E))
                            .border(1.dp, CyanAccent.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = rrFormatted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val pnlFormatted = String.format(
                        Locale.US,
                        "%s$%,.2f",
                        if (trade.pnl > 0) "+" else if (trade.pnl < 0) "-" else "",
                        kotlin.math.abs(trade.pnl)
                    )
                    Text(
                        text = pnlFormatted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = resultColor,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { onDeleteTrade(trade) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_trade_${trade.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Trade",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Date / Timestamp
            Text(
                text = formattedDate,
                fontSize = 11.sp,
                color = TextTertiary,
                fontFamily = FontFamily.Monospace
            )

            // Setup Notes / Confluence Box
            if (trade.setupNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = trade.setupNotes,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Screenshot Preview (if attached)
            if (!trade.screenshotPath.isNullOrBlank()) {
                val file = File(trade.screenshotPath)
                if (file.exists()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .clickable { onViewScreenshot(trade.screenshotPath) }
                            .testTag("screenshot_thumbnail_${trade.id}")
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(file)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Trade Chart Screenshot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                        )

                        // Floating Tap to Enlarge Chip
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "TAP TO EXPAND CHART",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
