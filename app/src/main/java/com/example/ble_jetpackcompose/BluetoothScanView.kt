import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_jetpackcompose.BLEDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BluetoothScanViewModel(private val context: Context) : ViewModel() {
    private val _devices = MutableStateFlow<List<BLEDevice>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError = _scanError.asStateFlow()

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter?.bluetoothLeScanner
    }

    private val scanDuration = 30000L // 10 seconds

    fun startScan(activity: Activity) {
        checkAndEnableBluetoothAndLocation(activity)
        viewModelScope.launch {
            // Start scan
            try {
                bluetoothLeScanner?.startScan(
                    listOf(),
                    ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build(),
                    scanCallback
                )

                // Auto stop after duration
                delay(scanDuration)
                stopScan()
            } catch (e: SecurityException) {

            }
        }
        // Check and enable Bluetooth and location services
//        if (!checkAndEnableBluetoothAndLocation(activity)) {
//            _scanError.value = "Bluetooth or Location services not available"
//            return
//        }

        // Check Bluetooth permissions
        if (!hasBluetoothPermissions()) {
            _scanError.value = "Required permissions not granted"
            return
        }

        viewModelScope.launch {
            try {
                // Set scanning state to true before starting scan
                _isScanning.value = true

                // Configure scan settings for better device discovery
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0) // Real-time reporting
                    .build()

                bluetoothLeScanner?.startScan(
                    listOf(), // Add filters if needed
                    settings,
                    scanCallback
                ) ?: run {
                    _scanError.value = "Bluetooth scanner not available"
                    _isScanning.value = false
                }
            } catch (e: SecurityException) {
                _scanError.value = "Security error while scanning: ${e.message}"
                _isScanning.value = false
            } catch (e: Exception) {
                _scanError.value = "Error starting scan: ${e.message}"
                _isScanning.value = false
            }
        }
    }

    private fun checkAndEnableBluetoothAndLocation(activity: Activity) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // Check Bluetooth availability
        if (bluetoothAdapter == null) {
            return
        }

        // Check and request to enable Bluetooth if disabled
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Check location permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Check if location services are enabled
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isLocationEnabled) {
            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity.startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION)
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            TODO("VERSION.SDK_INT < S")
        }

        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            try {
                val device = result.device
                if (device == null) {
                    return
                }

                // Filter out devices with empty names and addresses
                if (device.address.isNullOrEmpty()) {
                    return
                }

                val bleDevice = BLEDevice(
                    name = device.name ?: "Unknown Device",
                    address = device.address,
                    rssi = result.rssi.toString()
                )

                viewModelScope.launch {
                    val currentDevices = _devices.value
                    val existingDeviceIndex = currentDevices.indexOfFirst { it.address == device.address }

                    val updatedDevices = if (existingDeviceIndex != -1) {
                        currentDevices.toMutableList().apply {
                            this[existingDeviceIndex] = bleDevice
                        }
                    } else {
                        currentDevices + bleDevice
                    }

                    _devices.value = updatedDevices
                }
            } catch (e: SecurityException) {
                Log.e("BluetoothScan", "Permission error in scan callback", e)
                _scanError.value = "Permission error during scanning"
            } catch (e: Exception) {
                Log.e("BluetoothScan", "Error processing scan result", e)
                _scanError.value = "Error processing scan result"
            }
        }

        override fun onScanFailed(errorCode: Int) {
            val errorMessage = when (errorCode) {
                ScanCallback.SCAN_FAILED_ALREADY_STARTED -> "Scan already started"
                ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "Application registration failed"
                ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> "BLE scan not supported"
                ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> "Internal scan error"
                else -> "Scan failed with error code: $errorCode"
            }
            Log.e("BluetoothScan", errorMessage)
            _scanError.value = errorMessage
            _isScanning.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }

    fun stopScan() {
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
            _isScanning.value = false
        } catch (e: SecurityException) {
            Log.e("BluetoothScan", "Error stopping scan", e)
            _scanError.value = "Error stopping scan"
        } finally {
            _isScanning.value = false
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_ENABLE_LOCATION = 2
    }
}

