package com.github.jetbrains.rssreader.app

import com.github.jetbrains.rssreader.core.CurrencyReader
import com.github.jetbrains.rssreader.core.datasource.algorithm.CompressAlgorithm
import com.github.jetbrains.rssreader.core.datasource.network.CurrencyParser.Companion.toCurrency
import com.github.jetbrains.rssreader.core.entity.CompressResult
import com.github.jetbrains.rssreader.core.entity.CompressType
import com.github.jetbrains.rssreader.core.entity.Currency
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

data class CurrencyState(
    val progress: Boolean,
    val results: List<CompressResult>,
    val selectedCurrency: Currency? = null, //null means selected all
    val lastTime: Long = 0
) : State

sealed class CurrencyAction : Action {
    data class Refresh(val symbol: String, val interval: Int) : CurrencyAction()
    data class Data(val results: List<CompressResult>) : CurrencyAction()
    data class UpdateLastTime(val lastTime: Long) : CurrencyAction()
    data class ReadFileData(val strings: List<String>) : CurrencyAction()
    data class Error(val error: Exception) : CurrencyAction()
}

sealed class CurrencySideEffect : Effect {
    data class Error(val error: Exception) : CurrencySideEffect()
}

class CurrencyStore(
    private val currencyReader: CurrencyReader
) : Store<CurrencyState, CurrencyAction, CurrencySideEffect>,
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val state = MutableStateFlow(CurrencyState(false, emptyList()))
    private val sideEffect = MutableSharedFlow<CurrencySideEffect>()
    private val allCurrencies = MutableStateFlow<List<Currency>>(emptyList())
    private val currenciesInDay = MutableStateFlow<List<Currency>>(emptyList())

    init {
        combine(
            allCurrencies.filter { it.isNotEmpty() },
            currenciesInDay.filter { it.isNotEmpty() },
        ) { all, day ->
            val results = CompressAlgorithm.compress(all, day, CompressType.HIGH)
            dispatch(CurrencyAction.Data(results = results))
        }.launchIn(this)
    }

    override fun observeState(): StateFlow<CurrencyState> = state

    override fun observeSideEffect(): Flow<CurrencySideEffect> = sideEffect

    override fun dispatch(action: CurrencyAction) {
        Napier.d(tag = "CurrencyStore", message = "Action: $action")
        val oldState = state.value

        val newState = when (action) {
            is CurrencyAction.Refresh -> {
                if (oldState.progress) {
                    launch { sideEffect.emit(CurrencySideEffect.Error(Exception("In progress"))) }
                    oldState
                } else {
                    launch { loadAllCurrency(action.symbol, action.interval) }
                    oldState.copy(progress = true)
                }
            }

            is CurrencyAction.ReadFileData -> {
                launch { prepareDataFromFile(action.strings) }
                oldState
            }

            is CurrencyAction.Data -> {
                if (oldState.progress) {
                    CurrencyState(false, action.results, null, oldState.lastTime)
                } else {
                    launch { sideEffect.emit(CurrencySideEffect.Error(Exception("Unexpected action"))) }
                    oldState
                }
            }

            is CurrencyAction.UpdateLastTime -> {
                oldState.copy(lastTime = action.lastTime)
            }

            is CurrencyAction.Error -> {
                if (oldState.progress) {
                    launch { sideEffect.emit(CurrencySideEffect.Error(action.error)) }
                    CurrencyState(false, oldState.results)
                } else {
                    launch { sideEffect.emit(CurrencySideEffect.Error(Exception("Unexpected action"))) }
                    oldState
                }
            }
        }

        if (newState != oldState) {
            Napier.d(tag = "FeedStore", message = "NewState: $newState")
            state.value = newState
        }
    }

    private fun prepareDataFromFile(strings: List<String>) {
        allCurrencies.value = strings.map { it.drop(1).dropLast(1).split(",").toCurrency() }
    }

    private suspend fun loadAllCurrency(symbol: String, interval: Int) {
        try {
            val currencies = currencyReader.getCurrencyInDay(symbol, interval)
            currenciesInDay.value = currencies
            dispatch(CurrencyAction.UpdateLastTime(currencies.last().openTime))
        } catch (e: Exception) {
            dispatch(CurrencyAction.Error(e))
        }
    }
}