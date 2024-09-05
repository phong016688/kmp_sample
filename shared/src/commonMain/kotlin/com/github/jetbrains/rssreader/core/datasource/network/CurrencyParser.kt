package com.github.jetbrains.rssreader.core.datasource.network

import com.github.jetbrains.rssreader.core.entity.Currency

interface CurrencyParser {
    companion object {
        fun List<String>.toCurrency() = Currency(
            get(0).trim().toDouble().toLong(),
            get(1).trim().toDouble(),
            get(2).trim().toDouble(),
            get(3).trim().toDouble(),
            get(4).trim().toDouble(),
            get(5).trim().toDouble(),
            get(6).trim().toDouble().toLong(),
            get(7).trim().toDouble(),
            get(8).trim().toDouble(),
            get(9).trim().toDouble(),
            get(10).trim().toDouble(),
            get(11).trim().toDouble(),
        )
    }
}