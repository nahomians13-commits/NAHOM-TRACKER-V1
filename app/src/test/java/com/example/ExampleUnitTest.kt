package com.example

import com.example.data.PositionType
import com.example.data.Trade
import com.example.data.TradeResult
import com.example.viewmodel.DayOutcome
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ExampleUnitTest {
    @Test
    fun `test win rate and loss rate calculation`() {
        val total = 10
        val wins = 7
        val losses = 2
        val be = 1

        val winRate = (wins.toDouble() / total) * 100.0
        val lossRate = (losses.toDouble() / total) * 100.0
        val beRate = (be.toDouble() / total) * 100.0
        val ratioText = String.format(Locale.US, "%.1f : 1", wins.toDouble() / losses)

        assertEquals(70.0, winRate, 0.001)
        assertEquals(20.0, lossRate, 0.001)
        assertEquals(10.0, beRate, 0.001)
        assertEquals("3.5 : 1", ratioText)
    }

    @Test
    fun `test overall PnL summation`() {
        val trades = listOf(
            Trade(id = 1, asset = "EUR/USD", positionType = PositionType.LONG, result = TradeResult.WIN, pnl = 500.0, dateEpochMillis = 1000L),
            Trade(id = 2, asset = "GBP/JPY", positionType = PositionType.SHORT, result = TradeResult.LOSS, pnl = -200.0, dateEpochMillis = 2000L),
            Trade(id = 3, asset = "XAU/USD", positionType = PositionType.LONG, result = TradeResult.WIN, pnl = 350.0, dateEpochMillis = 3000L),
            Trade(id = 4, asset = "US30", positionType = PositionType.SHORT, result = TradeResult.BREAK_EVEN, pnl = 0.0, dateEpochMillis = 4000L)
        )

        val totalPnL = trades.sumOf { it.pnl }
        val winsCount = trades.count { it.result == TradeResult.WIN }
        val lossesCount = trades.count { it.result == TradeResult.LOSS }
        val beCount = trades.count { it.result == TradeResult.BREAK_EVEN }

        assertEquals(650.0, totalPnL, 0.001)
        assertEquals(2, winsCount)
        assertEquals(1, lossesCount)
        assertEquals(1, beCount)
    }

    @Test
    fun `test day outcome evaluation`() {
        fun evaluateOutcome(pnl: Double, wins: Int, losses: Int, tradesCount: Int): DayOutcome {
            return when {
                tradesCount == 0 -> DayOutcome.NONE
                pnl > 0.0 -> DayOutcome.WIN
                pnl < 0.0 -> DayOutcome.LOSS
                wins > losses -> DayOutcome.WIN
                losses > wins -> DayOutcome.LOSS
                else -> DayOutcome.BREAK_EVEN
            }
        }

        assertEquals(DayOutcome.WIN, evaluateOutcome(450.0, 2, 0, 2))
        assertEquals(DayOutcome.LOSS, evaluateOutcome(-150.0, 0, 1, 1))
        assertEquals(DayOutcome.BREAK_EVEN, evaluateOutcome(0.0, 0, 0, 1))
        assertEquals(DayOutcome.NONE, evaluateOutcome(0.0, 0, 0, 0))
    }

    @Test
    fun `test monthly performance summary metrics`() {
        val monthTrades = listOf(
            Trade(id = 1, asset = "XAU/USD", positionType = PositionType.LONG, result = TradeResult.WIN, pnl = 840.0, riskRewardRatio = 2.8, dateEpochMillis = 1000L),
            Trade(id = 2, asset = "EUR/USD", positionType = PositionType.SHORT, result = TradeResult.WIN, pnl = 420.5, riskRewardRatio = 3.0, dateEpochMillis = 2000L),
            Trade(id = 3, asset = "BTC/USDT", positionType = PositionType.LONG, result = TradeResult.LOSS, pnl = -310.0, riskRewardRatio = 1.0, dateEpochMillis = 3000L),
            Trade(id = 4, asset = "NAS100", positionType = PositionType.LONG, result = TradeResult.WIN, pnl = 680.0, riskRewardRatio = 2.5, dateEpochMillis = 4000L),
            Trade(id = 5, asset = "US30", positionType = PositionType.SHORT, result = TradeResult.BREAK_EVEN, pnl = 0.0, riskRewardRatio = 1.5, dateEpochMillis = 5000L)
        )

        val totalPnL = monthTrades.sumOf { it.pnl }
        val tradesTaken = monthTrades.size
        val avgRR = monthTrades.map { it.riskRewardRatio }.average()
        val avgRRText = String.format(Locale.US, "1 : %.1f", avgRR)

        assertEquals(1630.5, totalPnL, 0.001)
        assertEquals(5, tradesTaken)
        assertEquals(2.16, avgRR, 0.01)
        assertEquals("1 : 2.2", avgRRText)
    }
}
