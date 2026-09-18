package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Trade
import com.example.data.TradeResult
import com.example.ui.components.HeaderBar
import com.example.ui.components.InteractiveCalendarView
import com.example.ui.components.MonthlyPerformanceCard
import com.example.ui.components.PerformanceDashboardCard
import com.example.ui.components.ScreenshotViewerDialog
import com.example.ui.components.TradeDetailSheet
import com.example.ui.components.TradeEntryBottomSheet
import com.example.ui.components.TradeItemCard
import com.example.ui.theme.BreakEvenAmber
import com.example.ui.theme.CrimsonLoss
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.TrackerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.performanceStats.collectAsStateWithLifecycle()
    val monthlySummary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val calendarMonth by viewModel.calendarMonthState.collectAsStateWithLifecycle()
    val displayedTrades by viewModel.displayedTrades.collectAsStateWithLifecycle()
    val selectedDateString by viewModel.selectedDateString.collectAsStateWithLifecycle()
    val resultFilter by viewModel.resultFilter.collectAsStateWithLifecycle()

    var showTradeEntrySheet by remember { mutableStateOf(false) }
    var selectedTradeForDetail by remember { mutableStateOf<Trade?>(null) }
    var viewingScreenshotPath by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        topBar = {
            HeaderBar(
                onAddTradeClick = { showTradeEntrySheet = true },
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Performance Dashboard & Analytics Card (Overall Win/Loss Donut Chart)
            item {
                PerformanceDashboardCard(
                    stats = stats,
                    isFilteredByDate = selectedDateString != null,
                    selectedDate = selectedDateString,
                    onClearDateFilter = { viewModel.clearDateFilter() }
                )
            }

            // 2. Monthly Performance Summary Card (Total PnL, Trades Taken, Average R:R for Calendar Month)
            item {
                MonthlyPerformanceCard(
                    summary = monthlySummary
                )
            }

            // 3. Interactive Calendar View with Color-Coded Days
            item {
                InteractiveCalendarView(
                    calendarMonth = calendarMonth,
                    selectedDateString = selectedDateString,
                    onDateSelected = { dateStr ->
                        viewModel.selectDate(dateStr)
                    },
                    onPreviousMonth = { viewModel.navigateMonth(-1) },
                    onNextMonth = { viewModel.navigateMonth(1) }
                )
            }

            // 3. Trade History Section Header & Filters
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TRADE LOGS & SETUPS",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "${displayedTrades.size} RECORDED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Result Filter Chips (ALL, WINS, LOSSES, B/E)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            label = "ALL",
                            isSelected = resultFilter == null,
                            accentColor = CyanAccent,
                            onClick = { viewModel.setResultFilter(null) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            label = "WINS",
                            isSelected = resultFilter == TradeResult.WIN,
                            accentColor = NeonEmerald,
                            onClick = { viewModel.setResultFilter(TradeResult.WIN) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            label = "LOSSES",
                            isSelected = resultFilter == TradeResult.LOSS,
                            accentColor = CrimsonLoss,
                            onClick = { viewModel.setResultFilter(TradeResult.LOSS) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            label = "B/E",
                            isSelected = resultFilter == TradeResult.BREAK_EVEN,
                            accentColor = BreakEvenAmber,
                            onClick = { viewModel.setResultFilter(TradeResult.BREAK_EVEN) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Trade Items List
            if (displayedTrades.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
                            .padding(vertical = 36.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "NO TRADES FOUND",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedDateString != null) {
                                    "No executions recorded on $selectedDateString"
                                } else {
                                    "No trades match your active filter criteria"
                                },
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showTradeEntrySheet = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonEmerald,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LOG A TRADE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                items(displayedTrades, key = { it.id }) { trade ->
                    TradeItemCard(
                        trade = trade,
                        onDeleteTrade = { tradeToDelete ->
                            viewModel.deleteTrade(tradeToDelete)
                        },
                        onViewScreenshot = { path ->
                            viewingScreenshotPath = path
                        },
                        onTradeClick = { clickedTrade ->
                            selectedTradeForDetail = clickedTrade
                        }
                    )
                }
            }
        }
    }

    // Trade Detail Bottom Sheet (Dedicated Screenshot Picker/Preview & Trading Idea Notes Box)
    selectedTradeForDetail?.let { currentTrade ->
        TradeDetailSheet(
            trade = currentTrade,
            sheetState = detailSheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    detailSheetState.hide()
                    selectedTradeForDetail = null
                }
            },
            onUpdateTrade = { updatedTrade ->
                viewModel.updateTrade(updatedTrade)
                selectedTradeForDetail = updatedTrade
            },
            onDeleteTrade = { tradeToDelete ->
                viewModel.deleteTrade(tradeToDelete)
                selectedTradeForDetail = null
            },
            onViewFullscreenScreenshot = { path ->
                viewingScreenshotPath = path
            },
            onSaveScreenshotLocally = { uri ->
                viewModel.saveScreenshotLocally(uri)
            }
        )
    }

    // Trade Entry Bottom Sheet
    if (showTradeEntrySheet) {
        TradeEntryBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    showTradeEntrySheet = false
                }
            },
            onSaveTrade = { asset, posType, result, pnl, riskRewardRatio, dateEpochMillis, screenshotPath, setupNotes ->
                viewModel.saveTrade(
                    asset = asset,
                    positionType = posType,
                    result = result,
                    pnl = pnl,
                    riskRewardRatio = riskRewardRatio,
                    dateEpochMillis = dateEpochMillis,
                    screenshotPath = screenshotPath,
                    setupNotes = setupNotes
                )
                coroutineScope.launch {
                    sheetState.hide()
                    showTradeEntrySheet = false
                }
            },
            onSaveScreenshotLocally = { uri ->
                viewModel.saveScreenshotLocally(uri)
            }
        )
    }

    // Screenshot Fullscreen Viewer Dialog
    viewingScreenshotPath?.let { path ->
        ScreenshotViewerDialog(
            screenshotPath = path,
            onDismissRequest = { viewingScreenshotPath = null }
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.16f) else DarkSurface)
            .border(
                1.dp,
                if (isSelected) accentColor else DarkBorderSubtle,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) accentColor else TextSecondary,
            fontFamily = FontFamily.Monospace
        )
    }
}
