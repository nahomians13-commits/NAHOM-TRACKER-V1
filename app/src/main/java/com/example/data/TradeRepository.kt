package com.example.data

import kotlinx.coroutines.flow.Flow

class TradeRepository(private val tradeDao: TradeDao) {
    val allTrades: Flow<List<Trade>> = tradeDao.getAllTrades()

    suspend fun insertTrade(trade: Trade): Long = tradeDao.insertTrade(trade)

    suspend fun updateTrade(trade: Trade) = tradeDao.updateTrade(trade)

    suspend fun deleteTrade(trade: Trade) = tradeDao.deleteTrade(trade)

    suspend fun deleteTradeById(id: Long) = tradeDao.deleteTradeById(id)

    suspend fun getTradeCount(): Int = tradeDao.getTradeCount()
}
