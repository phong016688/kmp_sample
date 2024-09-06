package com.github.jetbrains.rssreader.core

import com.github.jetbrains.rssreader.core.datasource.network.CurrencyLoader
import com.github.jetbrains.rssreader.core.datasource.storage.CurrencyStorage
import com.github.jetbrains.rssreader.core.entity.Currency

class CurrencyReader internal constructor(
    private val currencyLoader: CurrencyLoader,
    private val currencyStorage: CurrencyStorage
) {
    @Throws(Exception::class)
    suspend fun getCurrencyInDay(symbol: String, interval: Int, length: Int): List<Currency> {
        return currencyLoader.getCurrencyInDay(symbol, interval, length)
    }

    companion object
}