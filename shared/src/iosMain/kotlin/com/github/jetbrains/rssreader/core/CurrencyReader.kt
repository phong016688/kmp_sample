package com.github.jetbrains.rssreader.core

import com.github.jetbrains.rssreader.core.datasource.network.CurrencyLoader
import com.github.jetbrains.rssreader.core.datasource.storage.CurrencyStorage
import com.russhwolf.settings.NSUserDefaultsSettings
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import platform.Foundation.NSUserDefaults

fun CurrencyReader.Companion.create(withLog: Boolean) = CurrencyReader(
    CurrencyLoader(
        IosHttpClient(withLog)
    ),
    CurrencyStorage(
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults()),
    )
).also {
    if (withLog) Napier.base(DebugAntilog())
}