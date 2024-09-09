package com.github.jetbrains.rssreader.androidApp.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.github.jetbrains.rssreader.androidApp.utils.candle_stick.CandleFeed
import com.github.jetbrains.rssreader.androidApp.utils.candle_stick.CandlestickChart
import com.github.jetbrains.rssreader.androidApp.utils.candle_stick.TimeData
import com.github.jetbrains.rssreader.app.CurrencyAction
import com.github.jetbrains.rssreader.app.CurrencyStore
import com.github.jetbrains.rssreader.core.entity.CompressResult
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainScreen : Screen, KoinComponent {
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val currencyStore: CurrencyStore by inject()
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val state by currencyStore.observeState().collectAsState()
        val refreshState = rememberPullRefreshState(
            refreshing = state.progress,
            onRefresh = { currencyStore.dispatch(CurrencyAction.Refresh(state.setting)) }
        )
        LaunchedEffect(Unit) {
            currencyStore.dispatch(CurrencyAction.Refresh(state.setting))
            currencyStore.dispatch(CurrencyAction.CalcSettingChange(state.calcSetting))
        }
        Box(modifier = Modifier.pullRefresh(refreshState)) {
            MainFeed(
                store = currencyStore,
                onCompressResultClick = { compressItem ->
                    navigator.push(ChartScreen(compressItem))
                }
            )
            PullRefreshIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
                refreshing = state.progress,
                state = refreshState,
                scale = true //https://github.com/google/accompanist/issues/572
            )
        }
    }
}

class ChartScreen(val compressItem: CompressResult) : Screen, KoinComponent {
    @Composable
    override fun Content() {
        val currencyStore: CurrencyStore by inject()
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val screenHeight = configuration.screenHeightDp
        val state by currencyStore.observeChartState().collectAsState()
        val currentFeed = remember(state.current) {
            state.current.map {
                CandleFeed(
                    it.openPrice.toFloat(),
                    it.closePrice.toFloat(),
                    it.highPrice.toFloat(),
                    it.lowPrice.toFloat(),
                    it.openTime.toString()
                )
            }.toMutableList()
        }
        val nextFeed = remember(state.current) {
            state.next.map {
                CandleFeed(
                    it.openPrice.toFloat(),
                    it.closePrice.toFloat(),
                    it.highPrice.toFloat(),
                    it.lowPrice.toFloat(),
                    it.openTime.toString()
                )
            }.toMutableList()
        }
        LaunchedEffect(Unit) {
            currencyStore.dispatch(CurrencyAction.GetHistoryChartData(compressItem.currencyId))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = Color(41, 49, 51, 255))
        ) {
            Column {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .clickable(onClick = { navigator.pop() })
                        .padding(12.dp),
                    tint = Color.Gray
                )
                CandlestickChart(
                    candleFeed = currentFeed,
                    timeFormat = listOf(
                        TimeData.DAY.index,
                        " ",
                        TimeData.MONTH_SHORT.index,
                        " ",
                        TimeData.YEAR.index
                    ),
                    selectedTimeFormat = listOf(
                        TimeData.DAY.index,
                        " ",
                        TimeData.MONTH_SHORT.index,
                        " ",
                        TimeData.HOUR.index,
                        ":",
                        TimeData.MINUTE.index
                    ),
                    chartWidth = screenWidth.dp,
                    chartHeight = (screenHeight / 2.5).dp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                CandlestickChart(
                    candleFeed = nextFeed,
                    timeFormat = listOf(
                        TimeData.DAY.index,
                        " ",
                        TimeData.MONTH_SHORT.index,
                        " ",
                        TimeData.YEAR.index
                    ),
                    selectedTimeFormat = listOf(
                        TimeData.DAY.index,
                        " ",
                        TimeData.MONTH_SHORT.index,
                        " ",
                        TimeData.HOUR.index,
                        ":",
                        TimeData.MINUTE.index
                    ),
                    chartWidth = screenWidth.dp,
                    chartHeight = (screenHeight / 2.5).dp,
                )
            }
        }
    }
}