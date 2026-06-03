package com.appfitness.app.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/** Connection lifecycle of the heart-rate monitor. */
enum class HrConnectionState { IDLE, SCANNING, CONNECTING, CONNECTED, DISCONNECTED, ERROR }

/**
 * Connects to a Bluetooth Low Energy heart-rate monitor (chest strap or watch)
 * using the standard **Heart Rate Service** and streams live BPM.
 *
 * GATT references (Bluetooth SIG):
 * - Heart Rate Service: `0x180D`
 * - Heart Rate Measurement characteristic: `0x2A37`
 * - Client Characteristic Configuration descriptor: `0x2902`
 *
 * The class is permission-defensive: every BLE call is guarded, so a missing
 * runtime permission surfaces as [HrConnectionState.ERROR] instead of a crash.
 * The UI must request permissions (see [requiredPermissions]) before connecting.
 */
class HeartRateMonitor(private val context: Context) {

    private val _state = MutableStateFlow(HrConnectionState.IDLE)
    val state: StateFlow<HrConnectionState> = _state.asStateFlow()

    private val _heartRate = MutableStateFlow<Int?>(null)
    val heartRate: StateFlow<Int?> = _heartRate.asStateFlow()

    private val _deviceName = MutableStateFlow<String?>(null)
    val deviceName: StateFlow<String?> = _deviceName.asStateFlow()

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val adapter: BluetoothAdapter? get() = bluetoothManager?.adapter

    private var gatt: BluetoothGatt? = null
    private var scanning = false

    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true

    fun hasPermissions(): Boolean =
        requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    /** Scans for a heart-rate monitor and connects to the first one found. */
    @SuppressLint("MissingPermission")
    fun connect() {
        if (!hasPermissions()) {
            _state.value = HrConnectionState.ERROR
            return
        }
        val scanner = adapter?.bluetoothLeScanner
        if (adapter?.isEnabled != true || scanner == null) {
            _state.value = HrConnectionState.ERROR
            return
        }
        disconnect()
        _state.value = HrConnectionState.SCANNING

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(HEART_RATE_SERVICE))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        try {
            scanning = true
            scanner.startScan(listOf(filter), settings, scanCallback)
        } catch (e: SecurityException) {
            _state.value = HrConnectionState.ERROR
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        stopScan()
        try {
            gatt?.disconnect()
            gatt?.close()
        } catch (e: SecurityException) {
            // ignore — already tearing down
        }
        gatt = null
        _heartRate.value = null
        if (_state.value != HrConnectionState.ERROR) {
            _state.value = HrConnectionState.DISCONNECTED
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        if (!scanning) return
        scanning = false
        try {
            adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            // ignore
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            stopScan()
            _state.value = HrConnectionState.CONNECTING
            _deviceName.value = try {
                device.name
            } catch (e: SecurityException) {
                null
            }
            try {
                gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice_TRANSPORT_LE)
            } catch (e: SecurityException) {
                _state.value = HrConnectionState.ERROR
            }
        }

        override fun onScanFailed(errorCode: Int) {
            scanning = false
            _state.value = HrConnectionState.ERROR
        }
    }

    private val gattCallback = object : android.bluetooth.BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> try {
                    gatt.discoverServices()
                } catch (e: SecurityException) {
                    _state.value = HrConnectionState.ERROR
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _state.value = HrConnectionState.DISCONNECTED
                    _heartRate.value = null
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val characteristic = gatt
                .getService(HEART_RATE_SERVICE)
                ?.getCharacteristic(HEART_RATE_MEASUREMENT)
            if (characteristic == null) {
                _state.value = HrConnectionState.ERROR
                return
            }
            try {
                gatt.setCharacteristicNotification(characteristic, true)
                val cccd = characteristic.getDescriptor(CCCD)
                @Suppress("DEPRECATION")
                if (cccd != null) {
                    cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(cccd)
                }
                _state.value = HrConnectionState.CONNECTED
            } catch (e: SecurityException) {
                _state.value = HrConnectionState.ERROR
            }
        }

        // Android 13+ delivers the value directly.
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            parseHeartRate(value)?.let { _heartRate.value = it }
        }

        // Legacy callback for Android 12 and below.
        @Suppress("DEPRECATION")
        @Deprecated("Used on API < 33")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            characteristic.value?.let { parseHeartRate(it)?.let { bpm -> _heartRate.value = bpm } }
        }
    }

    /** Parses BPM from a Heart Rate Measurement packet (flags + 8/16-bit value). */
    private fun parseHeartRate(data: ByteArray): Int? {
        if (data.isEmpty()) return null
        val flag = data[0].toInt() and 0xFF
        val is16Bit = flag and 0x01 != 0
        return if (is16Bit) {
            if (data.size < 3) null
            else ((data[2].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
        } else {
            if (data.size < 2) null else data[1].toInt() and 0xFF
        }
    }

    companion object {
        val HEART_RATE_SERVICE: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private const val BluetoothDevice_TRANSPORT_LE = 2 // BluetoothDevice.TRANSPORT_LE

        /** Runtime permissions needed to scan and connect, by API level. */
        val requiredPermissions: Array<String> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }
    }
}
