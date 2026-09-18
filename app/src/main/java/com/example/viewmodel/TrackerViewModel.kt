package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PositionType
import com.example.data.Trade
import com.example.data.TradeRepository
import com.example.data.TradeResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayTradeSummary(
    val dayOfMonth: Int,
    val dateString: String, // "YYYY-MM-DD"
    val dateEpochMillis: Long,
    val tradesCount: Int,
    val winsCount: Int,
    val lossesCount: Int,
    val breakEvenCount: Int,
    val netPnL: Double,
    val outcome: DayOutcome
)

enum class DayOutcome {
    NONE,
    WIN,
    LOSS,
    BREAK_EVEN
}

data class PerformanceStats(
    val totalTrades: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val breakEvens: Int = 0,
    val winRatePercent: Double = 0.0,
    val lossRatePercent: Double = 0.0,
    val breakEvenRatePercent: Double = 0.0,
    val winLossRatioText: String = "0 : 0",
    val overallPnL: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalLoss: Double = 0.0,
    val profitFactor: Double = 0.0,
    val avgTradePnL: Double = 0.0
)

data class CalendarMonth(
    val year: Int,
    val month: Int, // 0-based: 0 = January, 11 = December
    val monthTitle: String, // e.g. "September 2026"
    val days: List<DayTradeSummary?>, // includes null padding for offset
    val monthNetPnL: Double,
    val monthTradesCount: Int
)

data class MonthlyPerformanceSummary(
    val year: Int = 2026,
    val month: Int = 8,
    val monthTitle: String = "SEPTEMBER 2026",
    val totalPnL: Double = 0.0,
    val tradesTaken: Int = 0,
    val winsCount: Int = 0,
    val lossesCount: Int = 0,
    val breakEvenCount: Int = 0,
    val winRatePercent: Double = 0.0,
    val averageRiskReward: Double = 0.0,
    val averageRiskRewardText: String = "1 : 0.0",
    val profitFactor: Double = 0.0,
    val bestTradePnL: Double = 0.0,
    val worstTradePnL: Double = 0.0
)

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TradeRepository
    val allTrades: StateFlow<List<Trade>>

    private val _selectedDateString = MutableStateFlow<String?>(null)
    val selectedDateString: StateFlow<String?> = _selectedDateString

    // Filter by Result: ALL, WIN, LOSS, BREAK_EVEN
    private val _resultFilter = MutableStateFlow<TradeResult?>(null)
    val resultFilter: StateFlow<TradeResult?> = _resultFilter

    // Current Year and Month for Calendar View
    private val _currentCalendar = MutableStateFlow(Calendar.getInstance())
    val currentCalendar: StateFlow<Calendar> = _currentCalendar

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TradeRepository(database.tradeDao())
        allTrades = repository.allTrades.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        seedInitialTradesIfEmpty()
    }

    private fun seedInitialTradesIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.getTradeCount()
            if (count == 0) {
                val cal = Calendar.getInstance()
                val currentYear = cal.get(Calendar.YEAR)
                val currentMonth = cal.get(Calendar.MONTH)
                val currentDay = cal.get(Calendar.DAY_OF_MONTH)

                fun makeTime(day: Int, hour: Int): Long {
                    val c = Calendar.getInstance()
                    c.set(currentYear, currentMonth, day.coerceIn(1, 28), hour, 15, 0)
                    return c.timeInMillis
                }

                val sampleTrades = listOf(
                    Trade(
                        asset = "XAU/USD",
                        positionType = PositionType.LONG,
                        result = TradeResult.WIN,
                        pnl = 840.00,
                        riskRewardRatio = 2.8,
                        dateEpochMillis = makeTime(currentDay, 10),
                        setupNotes = "4H Bullish FVG tap with London session liquidity sweep. Confirmation on 5m market structure shift with strong displacement.",
                        screenshotPath = null
                    ),
                    Trade(
                        asset = "EUR/USD",
                        positionType = PositionType.SHORT,
                        result = TradeResult.WIN,
                        pnl = 420.50,
                        riskRewardRatio = 3.0,
                        dateEpochMillis = makeTime(currentDay, 14),
                        setupNotes = "New York Killzone bearish order block rejection at 1.09200 key psychological level. Target 1:3 RR at sell-side liquidity.",
                        screenshotPath = null
                    ),
                    Trade(
                        asset = "BTC/USDT",
                        positionType = PositionType.LONG,
                        result = TradeResult.LOSS,
                        pnl = -310.00,
                        riskRewardRatio = 1.0,
                        dateEpochMillis = makeTime((currentDay - 1).coerceAtLeast(1), 16),
                        setupNotes = "Attempted breaker block bounce during weekend low volume. Trapped by false breakout. Cut loss strictly at stop loss.",
                        screenshotPath = null
                    ),
                    Trade(
                        asset = "NAS100",
                        positionType = PositionType.LONG,
                        result = TradeResult.WIN,
                        pnl = 680.00,
                        riskRewardRatio = 2.5,
                        dateEpochMillis = makeTime((currentDay - 2).coerceAtLeast(1), 9),
                        setupNotes = "Opening bell gap fill into 15m bullish order block. Heavy confluence with 200 EMA support and rising volume.",
                        screenshotPath = null
                    ),
                    Trade(
                        asset = "GBP/JPY",
                        positionType = PositionType.SHORT,
                        result = TradeResult.WIN,
                        pnl = 510.00,
                        riskRewardRatio = 2.2,
                        dateEpochMillis = makeTime((currentDay - 3).coerceAtLeast(1), 11),
                        setupNotes = "Asian high sweep followed by clean change of character (CHoCH). Clean runner took profit at previous week low.",
                        screenshotPath = null
                    ),
                    Trade(
                        asset = "US30",
                        positionType = PositionType.SHORT,
                        result = TradeResult.BREAK_EVEN,
                        pnl = 0.00,
                        riskRewardRatio = 1.5,
                        dateEpochMillis = makeTime((currentDay - 4).coerceAtLeast(1), 13),
                        setupNotes = "CPI volatility mitigation. Took 50% off at 1:2 and moved stop loss to breakeven. Reversed and tagged BE.",
                        screenshotPath = null
                    ),
                    Trade(
                        asset = "ETH/USDT",
                        positionType = PositionType.SHORT,
                        result = TradeResult.LOSS,
                        pnl = -240.00,
                        riskRewardRatio = 1.0,
                        dateEpochMillis = makeTime((currentDay - 5).coerceAtLeast(1), 15),
                        setupNotes = "Resistance trendline rejection. Miscalculated Ethereum correlation with BTC breakout, hit stop loss as planned.",
                        screenshotPath = null
                    )
                )

                for (trade in sampleTrades) {
                    repository.insertTrade(trade)
                }
            }
        }
    }

    // Performance Stats computation
    val performanceStats: StateFlow<PerformanceStats> = allTrades.combine(_selectedDateString) { trades, selectedDate ->
        val filtered = if (selectedDate != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            trades.filter { sdf.format(Date(it.dateEpochMillis)) == selectedDate }
        } else {
            trades
        }

        val total = filtered.size
        val wins = filtered.count { it.result == TradeResult.WIN }
        val losses = filtered.count { it.result == TradeResult.LOSS }
        val breakEvens = filtered.count { it.result == TradeResult.BREAK_EVEN }

        val winRate = if (total > 0) (wins.toDouble() / total) * 100.0 else 0.0
        val lossRate = if (total > 0) (losses.toDouble() / total) * 100.0 else 0.0
        val breakEvenRate = if (total > 0) (breakEvens.toDouble() / total) * 100.0 else 0.0

        val ratioText = when {
            total == 0 -> "0 : 0"
            losses == 0 -> "$wins : 0"
            else -> String.format(Locale.US, "%.1f : 1", wins.toDouble() / losses)
        }

        val overallPnL = filtered.sumOf { it.pnl }
        val totalProfit = filtered.filter { it.pnl > 0 }.sumOf { it.pnl }
        val totalLoss = filtered.filter { it.pnl < 0 }.sumOf { it.pnl }

        val profitFactor = if (kotlin.math.abs(totalLoss) > 0.0001) {
            totalProfit / kotlin.math.abs(totalLoss)
        } else if (totalProfit > 0) {
            totalProfit
        } else {
            0.0
        }

        val avgPnL = if (total > 0) overallPnL / total else 0.0

        PerformanceStats(
            totalTrades = total,
            wins = wins,
            losses = losses,
            breakEvens = breakEvens,
            winRatePercent = winRate,
            lossRatePercent = lossRate,
            breakEvenRatePercent = breakEvenRate,
            winLossRatioText = ratioText,
            overallPnL = overallPnL,
            totalProfit = totalProfit,
            totalLoss = totalLoss,
            profitFactor = profitFactor,
            avgTradePnL = avgPnL
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PerformanceStats()
    )

    // Calendar month grid calculation
    val calendarMonthState: StateFlow<CalendarMonth> = combine(allTrades, _currentCalendar) { trades, cal ->
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)

        val monthTitleFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val monthTitle = monthTitleFormat.format(cal.time)

        // Clone to inspect days in month
        val inspectCal = cal.clone() as Calendar
        inspectCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = inspectCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 7 = Saturday
        val daysInMonth = inspectCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // Map trades by date string
        val tradesByDate = trades.groupBy { sdf.format(Date(it.dateEpochMillis)) }

        val daysList = mutableListOf<DayTradeSummary?>()

        // Padding before 1st day of month (Sunday-first grid)
        for (i in 1 until firstDayOfWeek) {
            daysList.add(null)
        }

        var monthNetPnL = 0.0
        var monthTradesCount = 0

        for (day in 1..daysInMonth) {
            inspectCal.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = sdf.format(inspectCal.time)
            val dayTrades = tradesByDate[dateStr] ?: emptyList()

            val dayWins = dayTrades.count { it.result == TradeResult.WIN }
            val dayLosses = dayTrades.count { it.result == TradeResult.LOSS }
            val dayBE = dayTrades.count { it.result == TradeResult.BREAK_EVEN }
            val dayPnL = dayTrades.sumOf { it.pnl }

            if (dayTrades.isNotEmpty()) {
                monthNetPnL += dayPnL
                monthTradesCount += dayTrades.size
            }

            val outcome = when {
                dayTrades.isEmpty() -> DayOutcome.NONE
                dayPnL > 0.0 -> DayOutcome.WIN
                dayPnL < 0.0 -> DayOutcome.LOSS
                dayWins > dayLosses -> DayOutcome.WIN
                dayLosses > dayWins -> DayOutcome.LOSS
                else -> DayOutcome.BREAK_EVEN
            }

            daysList.add(
                DayTradeSummary(
                    dayOfMonth = day,
                    dateString = dateStr,
                    dateEpochMillis = inspectCal.timeInMillis,
                    tradesCount = dayTrades.size,
                    winsCount = dayWins,
                    lossesCount = dayLosses,
                    breakEvenCount = dayBE,
                    netPnL = dayPnL,
                    outcome = outcome
                )
            )
        }

        CalendarMonth(
            year = year,
            month = month,
            monthTitle = monthTitle,
            days = daysList,
            monthNetPnL = monthNetPnL,
            monthTradesCount = monthTradesCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarMonth(
            year = 2026,
            month = 8,
            monthTitle = "September 2026",
            days = emptyList(),
            monthNetPnL = 0.0,
            monthTradesCount = 0
        )
    )

    // Monthly Performance Summary State for the currently viewed calendar month
    val monthlySummary: StateFlow<MonthlyPerformanceSummary> = combine(allTrades, _currentCalendar) { trades, cal ->
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time).uppercase(Locale.US)

        val monthCal = Calendar.getInstance()
        val monthTrades = trades.filter { trade ->
            monthCal.timeInMillis = trade.dateEpochMillis
            monthCal.get(Calendar.YEAR) == year && monthCal.get(Calendar.MONTH) == month
        }

        val tradesTaken = monthTrades.size
        val totalPnL = monthTrades.sumOf { it.pnl }
        val wins = monthTrades.count { it.result == TradeResult.WIN }
        val losses = monthTrades.count { it.result == TradeResult.LOSS }
        val breakEvens = monthTrades.count { it.result == TradeResult.BREAK_EVEN }
        val winRate = if (tradesTaken > 0) (wins.toDouble() / tradesTaken) * 100.0 else 0.0

        // Calculate average Risk-to-Reward (R:R) for the current calendar month
        val validRRList = monthTrades.map { trade ->
            if (trade.riskRewardRatio > 0.0) {
                trade.riskRewardRatio
            } else if (trade.result == TradeResult.WIN && trade.pnl > 0.0) {
                2.0
            } else if (trade.result == TradeResult.LOSS) {
                1.0
            } else {
                0.0
            }
        }.filter { it > 0.0 }

        val avgRR = if (validRRList.isNotEmpty()) validRRList.average() else 0.0
        val avgRRText = if (avgRR > 0.0) String.format(Locale.US, "1 : %.1f", avgRR) else "1 : 0.0"

        val totalProfit = monthTrades.filter { it.pnl > 0 }.sumOf { it.pnl }
        val totalLoss = monthTrades.filter { it.pnl < 0 }.sumOf { it.pnl }
        val profitFactor = if (kotlin.math.abs(totalLoss) > 0.001) {
            totalProfit / kotlin.math.abs(totalLoss)
        } else if (totalProfit > 0) totalProfit else 0.0

        val bestTrade = monthTrades.maxOfOrNull { it.pnl } ?: 0.0
        val worstTrade = monthTrades.minOfOrNull { it.pnl } ?: 0.0

        MonthlyPerformanceSummary(
            year = year,
            month = month,
            monthTitle = monthTitle,
            totalPnL = totalPnL,
            tradesTaken = tradesTaken,
            winsCount = wins,
            lossesCount = losses,
            breakEvenCount = breakEvens,
            winRatePercent = winRate,
            averageRiskReward = avgRR,
            averageRiskRewardText = avgRRText,
            profitFactor = profitFactor,
            bestTradePnL = bestTrade,
            worstTradePnL = worstTrade
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyPerformanceSummary()
    )

    // Filtered trades list for the UI
    val displayedTrades: StateFlow<List<Trade>> = combine(allTrades, _selectedDateString, _resultFilter) { trades, selectedDate, resultFilter ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        trades.filter { trade ->
            val matchesDate = if (selectedDate != null) {
                sdf.format(Date(trade.dateEpochMillis)) == selectedDate
            } else true

            val matchesResult = if (resultFilter != null) {
                trade.result == resultFilter
            } else true

            matchesDate && matchesResult
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun navigateMonth(delta: Int) {
        val cal = _currentCalendar.value.clone() as Calendar
        cal.add(Calendar.MONTH, delta)
        _currentCalendar.value = cal
    }

    fun selectDate(dateString: String?) {
        if (_selectedDateString.value == dateString) {
            _selectedDateString.value = null // toggle off if re-clicked
        } else {
            _selectedDateString.value = dateString
        }
    }

    fun clearDateFilter() {
        _selectedDateString.value = null
    }

    fun setResultFilter(filter: TradeResult?) {
        _resultFilter.value = if (_resultFilter.value == filter) null else filter
    }

    fun saveTrade(
        id: Long = 0L,
        asset: String,
        positionType: PositionType,
        result: TradeResult,
        pnl: Double,
        riskRewardRatio: Double = 2.0,
        dateEpochMillis: Long,
        screenshotPath: String?,
        setupNotes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val trade = Trade(
                id = id,
                asset = asset.trim().uppercase(Locale.US),
                positionType = positionType,
                result = result,
                pnl = pnl,
                riskRewardRatio = riskRewardRatio,
                dateEpochMillis = dateEpochMillis,
                screenshotPath = screenshotPath,
                setupNotes = setupNotes.trim()
            )
            if (id == 0L) {
                repository.insertTrade(trade)
            } else {
                repository.updateTrade(trade)
            }
        }
    }

    fun updateTrade(trade: Trade) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTrade(trade)
        }
    }

    fun deleteTrade(trade: Trade) {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete screenshot file if exists
            trade.screenshotPath?.let { path ->
                try {
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (_: Exception) {}
            }
            repository.deleteTrade(trade)
        }
    }

    suspend fun saveScreenshotLocally(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val directory = File(context.filesDir, "trade_screenshots")
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val filename = "trade_${System.currentTimeMillis()}.jpg"
                val destFile = File(directory, filename)

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(destFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                destFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
