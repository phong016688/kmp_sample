package com.github.jetbrains.rssreader.core.datasource.storage

import com.russhwolf.settings.Settings

class CurrencyStorage(private val settings: Settings) {
    private companion object {
        private const val KEY_FAVORITE_CURRENCY = "KEY_FAVORITE_CURRENCY"
    }
}