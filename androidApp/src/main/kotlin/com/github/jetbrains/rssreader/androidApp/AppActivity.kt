package com.github.jetbrains.rssreader.androidApp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.Navigator
import com.github.jetbrains.rssreader.R
import com.github.jetbrains.rssreader.androidApp.composeui.AppTheme
import com.github.jetbrains.rssreader.androidApp.composeui.MainScreen
import com.github.jetbrains.rssreader.androidApp.utils.readTextFileFromRaw
import com.github.jetbrains.rssreader.app.CurrencyAction
import com.github.jetbrains.rssreader.app.CurrencySideEffect
import com.github.jetbrains.rssreader.app.CurrencyStore
import kotlinx.coroutines.flow.filterIsInstance
import org.koin.android.ext.android.inject

class AppActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val currencyStore: CurrencyStore by inject()
                val scaffoldState = rememberScaffoldState()
                val context = LocalContext.current
                val error = currencyStore.observeSideEffect()
                    .filterIsInstance<CurrencySideEffect.Error>()
                    .collectAsState(null)
                val reload = currencyStore.observeSideEffect()
                    .filterIsInstance<CurrencySideEffect.ReloadSampleData>()
                    .collectAsState(null)
                LaunchedEffect(error.value) {
                    error.value?.let {
                        scaffoldState.snackbarHostState.showSnackbar(
                            it.error.message.toString()
                        )
                    }
                }
                LaunchedEffect(reload.value) {
                    reload.value?.let {
                        val data = loadSampleData(it.interval, context)
                        currencyStore.dispatch(CurrencyAction.SampleDataChange(data))
                    }
                }
                Box(
                    Modifier.padding(
                        WindowInsets.systemBars
                            .only(WindowInsetsSides.Start + WindowInsetsSides.End)
                            .asPaddingValues()
                    )
                ) {
                    Scaffold(
                        scaffoldState = scaffoldState,
                        snackbarHost = { hostState ->
                            SnackbarHost(
                                hostState = hostState,
                                modifier = Modifier.padding(
                                    WindowInsets.systemBars
                                        .only(WindowInsetsSides.Bottom)
                                        .asPaddingValues()
                                )
                            )
                        }
                    ) {
                        Navigator(MainScreen())
                    }
                }
            }
        }
    }

    private fun loadSampleData(interval: Int, context: Context): List<String> {
        return when (interval) {
            1 -> readTextFileFromRaw(context, R.raw.candle_sticks_1h)
            4 -> readTextFileFromRaw(context, R.raw.candle_sticks_4h)
            else -> emptyList()
        }
    }
}
