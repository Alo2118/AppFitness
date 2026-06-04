package com.appfitness.app.tracking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.appfitness.app.AppFitnessApplication
import com.appfitness.app.R
import com.appfitness.app.audio.SpeechCoach
import com.appfitness.app.ble.HeartRateMonitor
import com.appfitness.app.data.entity.GpsActivity
import com.appfitness.app.data.entity.GpsPoint
import com.appfitness.app.data.model.GpsActivityType
import com.appfitness.app.domain.Ghost
import com.appfitness.app.domain.GhostComparator
import com.appfitness.app.domain.Ghosts
import com.appfitness.app.domain.HeartRateZone
import com.appfitness.app.domain.HeartRateZoneEvaluator
import com.appfitness.app.domain.RewardEngine
import com.appfitness.app.domain.RewardEvent
import com.appfitness.app.domain.RewardTier
import com.appfitness.app.domain.RouteSample
import com.appfitness.app.domain.RouteTracker
import com.appfitness.app.domain.RunCoachEngine
import com.appfitness.app.domain.RunRewardState
import com.appfitness.app.domain.RunState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground service that records a GPS run/ride with [LocationManager], so
 * tracking continues with the screen off. It updates [TrackingState] live and,
 * on stop, persists the activity and its route points.
 */
class TrackingService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val tracker = RouteTracker()

    private lateinit var locationManager: LocationManager
    private var ghost: Ghost? = null
    private var startTime = 0L
    private var type = GpsActivityType.RUN
    private var tickJob: Job? = null

    private val coach = RunCoachEngine()
    private val rewards = RewardEngine()
    private var speech: SpeechCoach? = null
    private var monitor: HeartRateMonitor? = null
    private var vibrator: Vibrator? = null
    private var targetPaceSecPerKm: Double? = null
    private var userAge: Int = 30
    private var voiceEnabled: Boolean = true

    private fun say(text: String) {
        if (voiceEnabled) speech?.speak(text)
    }

    private val listener = LocationListener { location -> onLocation(location) }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        speech = SpeechCoach(this)
        monitor = (application as AppFitnessApplication).container.heartRateMonitor
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopTracking()
            else -> startTracking(intent)
        }
        return START_STICKY
    }

    private fun startTracking(intent: Intent?) {
        type = runCatching {
            GpsActivityType.valueOf(intent?.getStringExtra(EXTRA_TYPE) ?: GpsActivityType.RUN.name)
        }.getOrDefault(GpsActivityType.RUN)

        TrackingState.resetForStart(type)
        startTime = System.currentTimeMillis()
        coach.reset()
        rewards.reset()
        configureGhost(intent)
        startForegroundNotification()
        requestUpdates()
        scope.launch {
            val container = (application as AppFitnessApplication).container
            userAge = container.repository.latestCardioAssessment.first()?.age ?: 30
            voiceEnabled = container.settingsRepository.preferences.first().voiceCoach
            say(coach.startPhrase(type, targetPaceSecPerKm))
        }

        tickJob?.cancel()
        tickJob = scope.launch {
            while (isActive) {
                delay(1000)
                val elapsed = elapsedSec()
                TrackingState.elapsedSec.value = elapsed
                updateGhostLead(elapsed)
                speakCoaching(elapsed)
                rewardStep(elapsed)
            }
        }
    }

    /** Computes performance rewards and celebrates them (score + voice + haptics). */
    private fun rewardStep(elapsed: Int) {
        val hr = monitor?.heartRate?.value
        val inZone = hr != null &&
            HeartRateZoneEvaluator.zoneFor(hr, userAge) == HeartRateZone.AEROBICA
        val event = rewards.onUpdate(
            RunRewardState(
                distanceM = tracker.totalDistanceM,
                elapsedSec = elapsed,
                ghostLeadM = TrackingState.ghostLeadM.value,
                hrInTargetZone = inZone,
            )
        ) ?: return
        celebrate(event)
    }

    private fun celebrate(event: RewardEvent) {
        TrackingState.score.value = rewards.totalScore
        TrackingState.lastReward.value = event
        TrackingState.rewardCount.value += 1
        vibrate(event.tier)
        // Speak the bigger wins; small ones stay haptic + visual to avoid chatter.
        if (event.tier != RewardTier.SMALL) say(event.title)
    }

    private fun vibrate(tier: RewardTier) {
        val v = vibrator ?: return
        val pattern = when (tier) {
            RewardTier.SMALL -> longArrayOf(0, 80)
            RewardTier.MEDIUM -> longArrayOf(0, 120, 80, 120)
            RewardTier.EPIC -> longArrayOf(0, 200, 100, 200, 100, 300)
        }
        runCatching { v.vibrate(VibrationEffect.createWaveform(pattern, -1)) }
    }

    private fun configureGhost(intent: Intent?) {
        val mode = runCatching {
            GhostMode.valueOf(intent?.getStringExtra(EXTRA_GHOST_MODE) ?: GhostMode.NONE.name)
        }.getOrDefault(GhostMode.NONE)

        targetPaceSecPerKm = null
        ghost = when (mode) {
            GhostMode.NONE -> null
            GhostMode.PACE -> {
                val pace = intent?.getDoubleExtra(EXTRA_PACE, 0.0) ?: 0.0
                targetPaceSecPerKm = pace.takeIf { it > 0 }
                Ghosts.pace(pace)
            }
            GhostMode.TARGET_TIME -> {
                val distanceM = intent?.getDoubleExtra(EXTRA_TARGET_DISTANCE, 0.0) ?: 0.0
                val totalSec = intent?.getIntExtra(EXTRA_TARGET_TIME, 0) ?: 0
                if (distanceM > 0) targetPaceSecPerKm = totalSec / (distanceM / 1000.0)
                Ghosts.targetTime(distanceM, totalSec)
            }
            GhostMode.REPLAY -> {
                val id = intent?.getLongExtra(EXTRA_REPLAY_ID, -1L) ?: -1L
                loadReplayGhost(id)
                null // set asynchronously once points are loaded
            }
        }
    }

    /** Builds the current run state and speaks any coaching cue. */
    private fun speakCoaching(elapsed: Int) {
        val cue = coach.onUpdate(
            RunState(
                activityType = type,
                elapsedSec = elapsed,
                distanceM = tracker.totalDistanceM,
                currentPaceSecPerKm = tracker.currentPaceSecPerKm(),
                targetPaceSecPerKm = targetPaceSecPerKm,
                ghostLeadM = TrackingState.ghostLeadM.value,
                heartRate = monitor?.heartRate?.value,
                age = userAge,
                targetZone = HeartRateZone.AEROBICA,
            )
        )
        cue?.let { say(it) }
    }

    private fun loadReplayGhost(activityId: Long) {
        if (activityId <= 0) return
        scope.launch {
            val repo = (application as AppFitnessApplication).container.repository
            repo.getActivityWithPoints(activityId)?.let { awp ->
                ghost = Ghosts.replay(
                    awp.points.map { RouteSample(it.elapsedSec, it.cumulativeDistanceM, it.lat, it.lon) }
                )
            }
        }
    }

    private fun requestUpdates() {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000L, 0f, listener, Looper.getMainLooper(),
            )
        } catch (e: SecurityException) {
            // Permission revoked mid-flight; stop gracefully.
        }
    }

    private fun onLocation(location: Location) {
        val elapsed = elapsedSec()
        tracker.onLocation(location.latitude, location.longitude, elapsed)
        TrackingState.hasGpsFix.value = true
        TrackingState.distanceM.value = tracker.totalDistanceM
        TrackingState.paceSecPerKm.value = tracker.currentPaceSecPerKm()
        TrackingState.path.value = TrackingState.path.value + (location.latitude to location.longitude)
        updateGhostLead(elapsed)
        speakCoaching(elapsed)
        rewardStep(elapsed)
    }

    private fun updateGhostLead(elapsed: Int) {
        ghost?.let {
            TrackingState.ghostLeadM.value =
                GhostComparator.leadMeters(it, tracker.totalDistanceM, elapsed)
        }
    }

    private fun elapsedSec(): Int = ((System.currentTimeMillis() - startTime) / 1000).toInt()

    private fun stopTracking() {
        tickJob?.cancel()
        runCatching { locationManager.removeUpdates(listener) }

        val endTime = System.currentTimeMillis()
        val elapsed = ((endTime - startTime) / 1000).toInt()
        val distance = tracker.totalDistanceM
        val samples = tracker.samples.toList()
        say("Sessione completata. ${com.appfitness.app.domain.GeoUtils.formatKm(distance)} chilometri. Ottimo lavoro!")

        scope.launch {
            val repo = (application as AppFitnessApplication).container.repository

            // Personal record: longest distance for this activity type so far.
            val previousBest = repo.gpsActivities.first()
                .filter { it.type == type }
                .maxOfOrNull { it.distanceM } ?: 0f
            if (distance > previousBest && distance > 0) {
                val event = rewards.personalRecord(
                    "🏆 Nuovo record di distanza: ${com.appfitness.app.domain.GeoUtils.formatKm(distance)} km!",
                )
                celebrate(event)
            }

            val savedId = if (distance > 0) {
                repo.saveGpsActivity(
                    activity = GpsActivity(
                        type = type,
                        title = "${type.emoji} ${type.label}",
                        startedAt = startTime,
                        endedAt = endTime,
                        durationSec = elapsed,
                        distanceM = distance.toFloat(),
                        avgPaceSecPerKm = tracker.averagePaceSecPerKm().toFloat(),
                    ),
                    points = samples.map {
                        GpsPoint(
                            activityId = 0,
                            elapsedSec = it.elapsedSec,
                            cumulativeDistanceM = it.cumulativeDistanceM,
                            lat = it.lat,
                            lon = it.lon,
                        )
                    },
                )
            } else null

            // Fold the session's in-session score into the app-wide reward ledger.
            if (distance > 0) {
                repo.addReward("gps", com.appfitness.app.domain.ActivityRewardRules.gpsSession(rewards.totalScore))
            }

            TrackingState.lastSavedActivityId.value = savedId
            TrackingState.isTracking.value = false
            delay(2500) // let the spoken summary finish before tearing down TTS
            ServiceCompat.stopForeground(this@TrackingService, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun startForegroundNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Tracciamento GPS", NotificationManager.IMPORTANCE_LOW,
            )
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AppFitness")
            .setContentText("Tracciamento ${type.label} in corso…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, fgsType)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        runCatching { locationManager.removeUpdates(listener) }
        speech?.shutdown()
        scope.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "gps_tracking"
        private const val NOTIFICATION_ID = 42

        const val ACTION_START = "com.appfitness.app.action.START_TRACKING"
        const val ACTION_STOP = "com.appfitness.app.action.STOP_TRACKING"

        const val EXTRA_TYPE = "type"
        const val EXTRA_GHOST_MODE = "ghost_mode"
        const val EXTRA_PACE = "pace"
        const val EXTRA_TARGET_DISTANCE = "target_distance"
        const val EXTRA_TARGET_TIME = "target_time"
        const val EXTRA_REPLAY_ID = "replay_id"

        /** Builds the intent that starts a tracking session. */
        fun startIntent(context: Context, type: GpsActivityType, ghost: GhostConfig): Intent =
            Intent(context, TrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TYPE, type.name)
                putExtra(EXTRA_GHOST_MODE, ghost.mode.name)
                putExtra(EXTRA_PACE, ghost.paceSecPerKm)
                putExtra(EXTRA_TARGET_DISTANCE, ghost.targetDistanceM)
                putExtra(EXTRA_TARGET_TIME, ghost.targetTimeSec)
                putExtra(EXTRA_REPLAY_ID, ghost.replayActivityId)
            }

        fun stopIntent(context: Context): Intent =
            Intent(context, TrackingService::class.java).apply { action = ACTION_STOP }
    }
}
