package com.github.jetbrains.rssreader.app

import com.github.jetbrains.rssreader.core.CurrencyReader
import com.github.jetbrains.rssreader.core.entity.Currency
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CurrencyState(
    val progress: Boolean,
    val currencies: List<Currency>,
    val selectedCurrency: Currency? = null //null means selected all
) : State

sealed class CurrencyAction : Action {
    data class Refresh(val symbol: String, val interval: Int) : CurrencyAction()
    data class SelectCurrency(val currency: Currency?) : CurrencyAction()
    data class Data(val currencies: List<Currency>) : CurrencyAction()
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

            is CurrencyAction.SelectCurrency -> {
                if (action.currency == null || oldState.currencies.contains(action.currency)) {
                    oldState.copy(selectedCurrency = action.currency)
                } else {
                    launch { sideEffect.emit(CurrencySideEffect.Error(Exception("Unknown feed"))) }
                    oldState
                }
            }

            is CurrencyAction.Data -> {
                if (oldState.progress) {
                    val selected = oldState.selectedCurrency?.let {
                        if (action.currencies.contains(it)) it else null
                    }
                    CurrencyState(false, action.currencies, selected)
                } else {
                    launch { sideEffect.emit(CurrencySideEffect.Error(Exception("Unexpected action"))) }
                    oldState
                }
            }

            is CurrencyAction.Error -> {
                if (oldState.progress) {
                    launch { sideEffect.emit(CurrencySideEffect.Error(action.error)) }
                    CurrencyState(false, oldState.currencies)
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

    private suspend fun loadAllCurrency(symbol: String, interval: Int) {
        try {
            val allCurrencies = currencyReader.getCurrencyInDay(symbol, interval)
            dispatch(CurrencyAction.Data(allCurrencies))
        } catch (e: Exception) {
            dispatch(CurrencyAction.Error(e))
        }
    }
}