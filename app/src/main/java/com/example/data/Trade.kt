package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PositionType {
    LONG,
    SHORT
}

enum class TradeResult {
    WIN,
    LOSS,
    BREAK_EVEN
}

@Entity(tableName = "trades")
data class Trade(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val asset: String,                      // e.g. "EUR/USD", "XAU/USD", "BTC/USD"
    val positionType: PositionType,         // LONG or SHORT
    val result: TradeResult,                // WIN, LOSS, BREAK_EVEN
    val pnl: Double,                        // +350.0, -120.0, 0.0
    val riskRewardRatio: Double = 2.0,      // Planned/realized Risk-to-Reward ratio (e.g. 2.5 for 1:2.5)
    val dateEpochMillis: Long,              // timestamp representing trade execution date
    val screenshotPath: String? = null,     // stored internal file path for chart screenshot
    val setupNotes: String = "",            // confluence, setup rules, execution logic
    val createdAt: Long = System.currentTimeMillis()
)
