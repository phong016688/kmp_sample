package com.github.jetbrains.rssreader.androidApp.composeui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.github.jetbrains.rssreader.app.CurrencyAction
import com.github.jetbrains.rssreader.app.CurrencyStore
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
                onPostClick = { currency ->

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