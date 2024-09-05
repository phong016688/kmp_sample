package com.github.jetbrains.rssreader.core.datasource.algorithm

import com.github.jetbrains.rssreader.core.entity.CompressItem
import com.github.jetbrains.rssreader.core.entity.CompressResult
import com.github.jetbrains.rssreader.core.entity.CompressType
import com.github.jetbrains.rssreader.core.entity.Currency
import kotlin.math.pow
import kotlin.math.sqrt

object CompressAlgorithm {

    private fun euclideanDistance(
        sequenceA: List<CompressItem>,
        sequenceB: List<CompressItem>
    ): Double {
        require(sequenceA.size == sequenceB.size) { "Both sequences must have the same length." }
        return sqrt(sequenceA.zip(sequenceB) { a, b -> (a.price - b.price).pow(2) }.sum())
    }

    private fun cosineSimilarity(
        sequenceA: List<CompressItem>,
        sequenceB: List<CompressItem>
    ): Double {
        require(sequenceA.size == sequenceB.size) { "Both sequences must have the same length." }
        val dotProduct = sequenceA.zip(sequenceB) { a, b -> a.price * b.price }.sum()
        val magnitudeA = sqrt(sequenceA.sumOf { it.price.pow(2) })
        val magnitudeB = sqrt(sequenceB.sumOf { it.price.pow(2) })
        return dotProduct / (magnitudeA * magnitudeB)
    }

    fun compress(
        allCurrencies: List<Currency>,
        dayCurrencies: List<Currency>,
        compressType: CompressType
    ): List<CompressResult> {
        if (dayCurrencies.isEmpty() || allCurrencies.isEmpty()) return emptyList()
        val startPoint = dayCurrencies.first()
        val originalPrice = startPoint.lowPrice
        val frameSize = dayCurrencies.size
        val currenciesFrame = allCurrencies
            .windowed(size = frameSize, step = 1)
            .map { it.standardizationData(originalPrice) }
        val allItemsFrame = currenciesFrame.map { it.mapToCompressItem(compressType) }
        val dayItemFrame = dayCurrencies.mapToCompressItem(compressType)
        val results = allItemsFrame.map { compressTwoFrame(it, dayItemFrame) }
        return results.sortedBy { it.distance }.take(100)
    }

    private fun List<Currency>.standardizationData(originalPrice: Double): List<Currency> {
        val changePrice = originalPrice - first().lowPrice
        return map {
            it.copy(
                openPrice = it.openPrice + changePrice,
                closePrice = it.closePrice + changePrice,
                highPrice = it.highPrice + changePrice,
                lowPrice = it.lowPrice + changePrice,
            )
        }
    }

    private fun List<Currency>.mapToCompressItem(compressType: CompressType): List<CompressItem> {
        return when (compressType) {
            CompressType.HIGH -> map { CompressItem(it.openTime, it.highPrice) }
            CompressType.LOW -> map { CompressItem(it.openTime, it.lowPrice) }
            CompressType.OPEN -> map { CompressItem(it.openTime, it.openPrice) }
            CompressType.CLOSE -> map { CompressItem(it.openTime, it.closePrice) }
        }
    }

    private fun compressTwoFrame(
        frameA: List<CompressItem>,
        frameB: List<CompressItem>
    ): CompressResult {
        return CompressResult(
            distance = euclideanDistance(frameA, frameB),
            cosine = cosineSimilarity(frameA, frameB),
            currencyId = frameA.first().id,
        )
    }
}