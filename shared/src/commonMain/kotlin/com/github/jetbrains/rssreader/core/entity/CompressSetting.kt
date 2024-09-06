package com.github.jetbrains.rssreader.core.entity

data class CompressSetting(
    val symbol: String,
    val interval: Int,
    val length: Int
)

data class CalculatorSetting(
    val type: CompressType
)