package com.appfitness.app.reward

import com.appfitness.app.domain.RewardEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * App-wide transient bus for reward celebrations. Any activity can emit a reward
 * here; the top-level UI observes it to show a snackbar and trigger haptics, so
 * the dopamine feedback is consistent everywhere.
 */
object RewardCenter {
    private val _events = MutableSharedFlow<RewardEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<RewardEvent> = _events.asSharedFlow()

    fun award(event: RewardEvent) {
        _events.tryEmit(event)
    }
}
