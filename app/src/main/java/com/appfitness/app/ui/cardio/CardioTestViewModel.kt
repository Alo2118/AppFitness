package com.appfitness.app.ui.cardio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfitness.app.ble.HeartRateMonitor
import com.appfitness.app.ble.HrConnectionState
import com.appfitness.app.data.FitnessRepository
import com.appfitness.app.data.entity.CardioAssessment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Steps of the guided heart-rate test. */
enum class CardioPhase(val label: String) {
    SETUP("Preparazione"),
    RESTING("Riposo — stai fermo e rilassati"),
    EFFORT("Sforzo — muoviti! (jumping jack / step)"),
    RECOVERY("Recupero — fermati e respira"),
    RESULT("Risultato"),
}

class CardioTestViewModel(
    private val repository: FitnessRepository,
    private val monitor: HeartRateMonitor,
) : ViewModel() {

    // Re-expose the monitor streams for the UI.
    val connectionState: StateFlow<HrConnectionState> = monitor.state
    val heartRate: StateFlow<Int?> = monitor.heartRate
    val deviceName: StateFlow<String?> = monitor.deviceName

    private val _phase = MutableStateFlow(CardioPhase.SETUP)
    val phase: StateFlow<CardioPhase> = _phase.asStateFlow()

    private val _secondsLeft = MutableStateFlow(0)
    val secondsLeft: StateFlow<Int> = _secondsLeft.asStateFlow()

    private val _age = MutableStateFlow(30)
    val age: StateFlow<Int> = _age.asStateFlow()

    private val _result = MutableStateFlow<CardioAssessment?>(null)
    val result: StateFlow<CardioAssessment?> = _result.asStateFlow()

    private var runner: Job? = null

    init {
        viewModelScope.launch {
            repository.latestCardioAssessment.first()?.let { _age.value = it.age }
        }
    }

    fun setAge(value: Int) { _age.value = value.coerceIn(10, 100) }

    fun hasPermissions(): Boolean = monitor.hasPermissions()

    fun isBluetoothEnabled(): Boolean = monitor.isBluetoothEnabled()

    fun connect() = monitor.connect()

    fun canStart(): Boolean = connectionState.value == HrConnectionState.CONNECTED

    /** Runs the rest → effort → recovery protocol, then stores the result. */
    fun startTest() {
        if (!canStart() || runner?.isActive == true) return
        _result.value = null
        runner = viewModelScope.launch {
            var resting = Int.MAX_VALUE
            var peak = 0
            var last = 0

            _phase.value = CardioPhase.RESTING
            countdown(REST_SEC) {
                heartRate.value?.let { resting = minOf(resting, it); last = it }
            }

            _phase.value = CardioPhase.EFFORT
            countdown(EFFORT_SEC) {
                heartRate.value?.let { peak = maxOf(peak, it); last = it }
            }

            _phase.value = CardioPhase.RECOVERY
            countdown(RECOVERY_SEC) {
                heartRate.value?.let { last = it }
            }
            val recovery = last

            val restingHr = if (resting == Int.MAX_VALUE) 0 else resting
            _result.value = repository.saveCardioAssessment(
                age = _age.value,
                restingHr = restingHr,
                peakHr = peak,
                recoveryHr = recovery,
                deviceName = deviceName.value.orEmpty(),
            )
            _phase.value = CardioPhase.RESULT
        }
    }

    private suspend fun countdown(seconds: Int, onTick: () -> Unit) {
        for (s in seconds downTo 1) {
            _secondsLeft.value = s
            onTick()
            delay(1000)
        }
        _secondsLeft.value = 0
    }

    fun reset() {
        runner?.cancel()
        _phase.value = CardioPhase.SETUP
        _result.value = null
        _secondsLeft.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        runner?.cancel()
        monitor.disconnect()
    }

    companion object {
        private const val REST_SEC = 30
        private const val EFFORT_SEC = 120
        private const val RECOVERY_SEC = 60
    }
}
