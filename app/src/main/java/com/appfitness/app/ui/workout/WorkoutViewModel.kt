package com.appfitness.app.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfitness.app.data.FitnessRepository
import com.appfitness.app.data.entity.Exercise
import com.appfitness.app.data.entity.SetLog
import com.appfitness.app.data.relation.SessionWithSets
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Countdown state for the rest timer between sets. */
data class RestTimerState(
    val isRunning: Boolean = false,
    val remainingSec: Int = 0,
    val totalSec: Int = 0,
) {
    val progress: Float get() = if (totalSec == 0) 0f else remainingSec.toFloat() / totalSec
}

class WorkoutViewModel(
    private val repository: FitnessRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val sessionId: Long = checkNotNull(savedStateHandle["sessionId"])

    val session: StateFlow<SessionWithSets?> =
        repository.observeSession(sessionId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private val _elapsedSec = MutableStateFlow(0)
    val elapsedSec: StateFlow<Int> = _elapsedSec.asStateFlow()

    private val _rest = MutableStateFlow(RestTimerState())
    val rest: StateFlow<RestTimerState> = _rest.asStateFlow()

    /** User's age (from the latest cardio test) for heart-rate zone calculations. */
    val userAge: StateFlow<Int> = repository.latestCardioAssessment
        .map { it?.age ?: 30 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 30)

    private var restJob: Job? = null
    private var startedAt: Long = 0L

    init {
        viewModelScope.launch {
            startedAt = repository.getSession(sessionId)?.startedAt ?: System.currentTimeMillis()
            while (true) {
                _elapsedSec.value = ((System.currentTimeMillis() - startedAt) / 1000).toInt()
                delay(1000)
            }
        }
    }

    /** Adds an exercise to the session, creating one row per default set. */
    fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            repeat(exercise.defaultSets) { index ->
                repository.addSet(
                    SetLog(
                        sessionId = sessionId,
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        setNumber = index + 1,
                        reps = if (exercise.isTimeBased) 0 else exercise.defaultReps,
                        durationSec = if (exercise.isTimeBased) exercise.defaultDurationSec else 0,
                        restSec = exercise.defaultRestSec,
                        isTimeBased = exercise.isTimeBased,
                    )
                )
            }
        }
    }

    fun updateSet(set: SetLog) {
        viewModelScope.launch { repository.updateSet(set) }
    }

    fun deleteSet(id: Long) {
        viewModelScope.launch { repository.deleteSet(id) }
    }

    /** Toggle a set's completed state; starts the rest timer when completing. */
    fun toggleCompleted(set: SetLog) {
        val updated = set.copy(completed = !set.completed)
        updateSet(updated)
        if (updated.completed && updated.restSec > 0) {
            startRest(updated.restSec)
        }
    }

    fun startRest(seconds: Int) {
        restJob?.cancel()
        _rest.value = RestTimerState(isRunning = true, remainingSec = seconds, totalSec = seconds)
        restJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _rest.value = _rest.value.copy(remainingSec = remaining)
            }
            _rest.value = _rest.value.copy(isRunning = false)
        }
    }

    fun adjustRest(deltaSec: Int) {
        val current = _rest.value
        if (!current.isRunning) return
        val newRemaining = (current.remainingSec + deltaSec).coerceAtLeast(0)
        _rest.value = current.copy(
            remainingSec = newRemaining,
            totalSec = maxOf(current.totalSec, newRemaining),
        )
    }

    fun stopRest() {
        restJob?.cancel()
        _rest.value = RestTimerState()
    }

    /** Finalises the session with the post-workout emotional check-in. */
    fun finishWorkout(moodAfter: Int?, energyAfter: Int?, note: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val current = repository.getSession(sessionId) ?: return@launch
            repository.updateSession(
                current.copy(
                    endedAt = System.currentTimeMillis(),
                    totalDurationSec = ((System.currentTimeMillis() - current.startedAt) / 1000).toInt(),
                    moodAfter = moodAfter,
                    energyAfter = energyAfter,
                    note = note,
                )
            )
            // Adaptive programming: if this session belongs to a program, adjust
            // its load based on completion + post-workout energy.
            current.programId?.let { repository.applyAdaptiveUpdate(it, sessionId) }

            // Reward the completed workout (app-wide dopamine system).
            val sets = repository.getSessionWithSets(sessionId)?.sets.orEmpty()
            val completed = sets.count { it.completed }
            val volume = sets.filter { it.completed }
                .sumOf { (it.reps * it.weightKg).toDouble() }.toFloat()
            val moodDelta = if (moodAfter != null && current.moodBefore != null) {
                moodAfter - current.moodBefore
            } else null
            repository.addReward(
                "workout",
                com.appfitness.app.domain.ActivityRewardRules.workout(completed, volume, moodDelta),
            )
            onDone()
        }
    }

    /** Rewards a guided set when it ends (immediate dopamine during strength work). */
    fun awardGuidedSet(reps: Int, target: Int) {
        viewModelScope.launch {
            repository.addReward("guided", com.appfitness.app.domain.ActivityRewardRules.guidedSet(reps, target))
        }
    }

    /** Discards an empty/abandoned session. */
    fun discard(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            onDone()
        }
    }
}
