package com.github.jetbrains.rssreader.androidApp.composeui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.jetbrains.rssreader.app.CurrencyAction
import com.github.jetbrains.rssreader.app.CurrencyStore
import com.github.jetbrains.rssreader.core.entity.CalculatorSetting
import com.github.jetbrains.rssreader.core.entity.CompressResult
import com.github.jetbrains.rssreader.core.entity.CompressSetting
import com.github.jetbrains.rssreader.core.entity.CompressType

@Composable
fun MainFeed(
    store: CurrencyStore,
    onCompressResultClick: (CompressResult) -> Unit,
) {
    val state = store.observeState().collectAsState()
    val compressResult = remember(state.value.results) {
        state.value.results
    }
    val setting = remember(state.value.setting) {
        state.value.setting
    }
    val calcSetting = remember(state.value.calcSetting) {
        state.value.calcSetting
    }
    val lastTime = remember(state.value.lastTime) {
        state.value.lastTime
    }
    val showSettingDialog = remember { mutableStateOf<SettingIcons?>(null) }
    Box {
        Column {
            val listState = rememberLazyListState()
            CompressResultList(
                modifier = Modifier.weight(1f),
                compressResult = compressResult,
                listState = listState,
                lastTime = lastTime
            ) { currency -> onCompressResultClick(currency) }
            MainFeedBottomBar(
                setting = setting,
                calcSetting = calcSetting,
                onSettingClick = { showSettingDialog.value = it }
            )
            Spacer(
                Modifier
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .fillMaxWidth()
            )
        }
        if (showSettingDialog.value != null) {
            CompressSettingDialog(
                data = showSettingDialog.value?.strings() ?: emptyList(),
                onSelected = { index ->
                    when (val item = showSettingDialog.value) {
                        is SettingIcons.Symbol -> {
                            store.dispatch(CurrencyAction.Refresh(setting.copy(symbol = item.data[index])))
                        }

                        is SettingIcons.Interval -> {
                            store.dispatch(CurrencyAction.Refresh(setting.copy(interval = item.data[index])))
                        }

                        is SettingIcons.Type -> {
                            store.dispatch(CurrencyAction.CalcSettingChange(calcSetting.copy(type = item.data[index])))
                        }

                        is SettingIcons.Length -> {
                            store.dispatch(CurrencyAction.Refresh(setting.copy(length = item.data[index])))
                        }

                        else -> {}
                    }
                },
                onDismiss = {
                    showSettingDialog.value = null
                }
            )
        }
    }
}

sealed class SettingIcons {
    abstract fun strings(): List<String>
    data class Symbol(val data: List<String> = listOf("BTCUSDT")) : SettingIcons() {
        override fun strings(): List<String> {
            return data
        }
    }

    data class Interval(val data: List<Int> = listOf(1, 4)) : SettingIcons() {
        override fun strings(): List<String> {
            return data.map { "${it}h" }
        }
    }

    data class Type(val data: List<CompressType> = CompressType.entries) : SettingIcons() {
        override fun strings(): List<String> {
            return data.map { it.name }
        }
    }

    data class Length(val data: List<Int> = listOf(12, 24, 36, 48, 60, 72)) : SettingIcons() {
        override fun strings(): List<String> {
            return data.map { it.toString() }
        }
    }
}

@Composable
fun MainFeedBottomBar(
    calcSetting: CalculatorSetting,
    setting: CompressSetting,
    onSettingClick: (SettingIcons) -> Unit,
) {
    val items = buildList {
        add(SettingIcons.Symbol())
        add(SettingIcons.Interval())
        add(SettingIcons.Type())
        add(SettingIcons.Length())
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        items.map { item ->
            when (item) {
                is SettingIcons.Symbol -> CompressSettingIcon(
                    modifier = Modifier.weight(1f),
                    selected = setting.symbol,
                    onClick = { onSettingClick(item) }
                )

                is SettingIcons.Interval -> CompressSettingIcon(
                    modifier = Modifier.weight(1f),
                    selected = "${setting.interval}h",
                    onClick = { onSettingClick(item) }
                )

                is SettingIcons.Type -> CompressSettingIcon(
                    modifier = Modifier.weight(1f),
                    selected = calcSetting.type.name,
                    onClick = { onSettingClick(item) }
                )

                is SettingIcons.Length -> CompressSettingIcon(
                    modifier = Modifier.weight(1f),
                    selected = setting.length.toString(),
                    onClick = { onSettingClick(item) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun CompressSettingIconPreview() {
    AppTheme {

    }
}


