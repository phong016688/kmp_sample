package com.github.jetbrains.rssreader.app

import com.github.jetbrains.rssreader.core.CurrencyReader
import com.github.jetbrains.rssreader.core.datasource.algorithm.CompressAlgorithm
import com.github.jetbrains.rssreader.core.datasource.network.CurrencyParser.Companion.toCurrency
import com.github.jetbrains.rssreader.core.entity.CalculatorSetting
import com.github.jetbrains.rssreader.core.entity.CompressResult
import com.github.jetbrains.rssreader.core.entity.CompressSetting
import com.github.jetbrains.rssreader.core.entity.CompressType
import com.github.jetbrains.rssreader.core.entity.Currency
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class CurrencyState(
    val progress: Boolean,
    val results: List<CompressResult>,
    val lastTime: Long = 0,
    val setting: CompressSetting,
    val calcSetting: CalculatorSetting
) : State {
    companion object {
        val Initial = CurrencyState(
            false,
            emptyList(),
            0,
            CompressSetting("BTCUSDT", 1, 12),
            CalculatorSetting(CompressType.HIGH)
        )
    }
}

sealed class CurrencyAction : Action {
    data class Refresh(val setting: CompressSetting) : CurrencyAction()
    data class SampleDataChange(val strings: List<String>) : CurrencyAction()
    data class CalcSettingChange(val calcSetting: CalculatorSetting) : CurrencyAction()
}

sealed class CurrencySideEffect : Effect {
    data class Error(val error: Exception) : CurrencySideEffect()
    data class ReloadSampleData(val interval: Int) : CurrencySideEffect()
}

class CurrencyStore(
    private val currencyReader: CurrencyReader
) : Store<CurrencyState, CurrencyAction, CurrencySideEffect>,
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val state = MutableStateFlow(CurrencyState.Initial)
    private val sideEffect = MutableSharedFlow<CurrencySideEffect>()
    private val actionState = MutableSharedFlow<Action>()

    init {
        actionState.filterIsInstance<CurrencyAction.Refresh>()
            .map { it.setting.interval }
            .distinctUntilChanged()
            .onEach {
                emitSideEffect(CurrencySideEffect.ReloadSampleData(it))
                Napier.d(tag = "#####2ReloadSampleData", message = "interval: ${it}")
            }.launchIn(this)
        val allCurrencies = actionState.filterIsInstance<CurrencyAction.SampleDataChange>()
            .map { it.strings }
            .distinctUntilChanged()
            .map { prepareDataFromFile(it) }
            .filter { it.isNotEmpty() }
            .onEach { Napier.d(tag = "#####2allCurrencies", message = "size: ${it.size}") }
        val currenciesInDay = actionState.filterIsInstance<CurrencyAction.Refresh>()
            .map { it.setting }
            .distinctUntilChanged()
            .onEach { processState { copy(setting = it) } }
            .map { loadAllCurrency(it) }
            .filter { it.isNotEmpty() }
            .onEach { processState { copy(lastTime = it.first().openTime) } }
            .catch { emitSideEffect(CurrencySideEffect.Error(Exception(it))) }
            .onEach { Napier.d(tag = "#####2currenciesInDay", message = "size: ${it.size}") }
        val typeChange = actionState.filterIsInstance<CurrencyAction.CalcSettingChange>()
            .onEach { processState { copy(calcSetting = it.calcSetting) } }
            .map { it.calcSetting.type }
            .distinctUntilChanged()
            .onEach { Napier.d(tag = "#####2typeChange", message = "type: ${it}") }
        combine(allCurrencies, currenciesInDay, typeChange) { all, day, type ->
            runCatching {
                val results = CompressAlgorithm.compress(all, day, type)
                processState { CurrencyState(false, results, lastTime, setting, calcSetting) }
                Napier.d(tag = "#####2combine", message = "results: ${results.size}")
            }.onFailure {
                emitSideEffect(CurrencySideEffect.Error(Exception(it)))
            }
        }.launchIn(this)
    }

    override fun observeState(): StateFlow<CurrencyState> = state

    override fun observeSideEffect(): Flow<CurrencySideEffect> = sideEffect

    override fun dispatch(action: CurrencyAction) {
        Napier.d(tag = "CurrencyStore", message = "Action: $action")
        launch { actionState.emit(action) }
    }

    private fun processState(onUpdateState: CurrencyState.() -> CurrencyState) {
        val oldState = state.value
        val newState = onUpdateState(oldState)
        if (newState != oldState) {
            Napier.d(tag = "processState", message = "newState: ${newState.calcSetting}")
            state.value = newState
        }
    }

    private suspend fun emitSideEffect(effect: CurrencySideEffect) {
        if (effect is CurrencySideEffect.Error) {
            processState { copy(progress = false) }
        }
        sideEffect.emit(effect)
    }

    private fun prepareDataFromFile(strings: List<String>): List<Currency> {
        return strings.map { it.drop(1).dropLast(1).split(",").toCurrency() }
    }

    private suspend fun loadAllCurrency(setting: CompressSetting): List<Currency> {
        if (state.value.progress) throw Throwable("In Process")
        processState { copy(progress = true) }
        val currencies = currencyReader.getCurrencyInDay(
            setting.symbol,
            setting.interval,
            setting.length
        )
        return currencies
    }
}