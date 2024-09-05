package com.github.jetbrains.rssreader.core

import android.content.Context
import com.github.jetbrains.rssreader.core.datasource.network.CurrencyLoader
import com.github.jetbrains.rssreader.core.datasource.storage.CurrencyStorage
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun CurrencyReader.Companion.create(ctx: Context, withLog: Boolean) = CurrencyReader(
    CurrencyLoader(
        AndroidHttpClient(withLog)
    ),
    CurrencyStorage(
        SharedPreferencesSettings(ctx.getSharedPreferences("currency_rader_pref", Context.MODE_PRIVATE)),
    )
).also {
    if (withLog) Napier.base(DebugAntilog())
}