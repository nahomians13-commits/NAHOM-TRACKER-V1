package com.example.data

import androidx.room.TypeConverter

class TradeConverters {
    @TypeConverter
    fun fromPositionType(type: PositionType): String = type.name

    @TypeConverter
    fun toPositionType(value: String): PositionType {
        return try {
            PositionType.valueOf(value)
        } catch (_: Exception) {
            PositionType.LONG
        }
    }

    @TypeConverter
    fun fromTradeResult(result: TradeResult): String = result.name

    @TypeConverter
    fun toTradeResult(value: String): TradeResult {
        return try {
            TradeResult.valueOf(value)
        } catch (_: Exception) {
            TradeResult.WIN
        }
    }
}
