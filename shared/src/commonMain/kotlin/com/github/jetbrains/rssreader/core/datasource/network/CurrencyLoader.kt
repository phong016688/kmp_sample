package com.github.jetbrains.rssreader.core.datasource.network

import com.github.jetbrains.rssreader.core.entity.Currency
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path
import io.ktor.http.takeFrom
import kotlinx.datetime.Clock

internal class CurrencyLoader(private val httpClient: HttpClient) {
    suspend fun getCurrencyInDay(symbol: String, interval: Int): List<Currency> {
        return httpClient.get { getKLine(symbol, interval) }
            .body<List<List<Double>>>()
            .map { it.toCurrency() }
    }

    private fun HttpRequestBuilder.getKLine(symbol: String, interval: Int) {
        val startTime = Clock.System.now().toEpochMilliseconds() - 24 * 60 * 60 * 1000
        url {
            takeFrom("https://api.binance.com/")
            path("api/v3/klines")
            parameter("symbol", symbol)
            parameter("interval", "${interval}h")
            parameter("startTime", startTime)
            parameter("limit", 1000)
        }
    }

    private fun List<Double>.toCurrency() = Currency(
        get(0).toLong(),
        get(1),
        get(2),
        get(3),
        get(4),
        get(5),
        get(6).toLong(),
        get(7),
        get(8),
        get(9),
        get(10),
        get(11),
    )
}