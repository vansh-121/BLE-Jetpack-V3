import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
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

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter?.bluetoothLeScanner
    }
    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()


    init {
        // Initialize _devices with any previously stored devices
        _devices.value = persistentDevices
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
    private val scanDuration = 10000L // 10 seconds


    fun startScan(activity: Activity) {
        checkAndEnableBluetoothAndLocation(activity)

        // Check Bluetooth permissions
        if (!hasBluetoothPermissions()) {
            Log.w("BluetoothScan", "Permissions not granted")
            return
        }

        viewModelScope.launch {
            try {
                val scanFilters = listOf(
                    ScanFilter.Builder()
                        .build()
                )

                val scanSettings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                    .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                    .setReportDelay(0L)
                    .setLegacy(false)
                    .build()



                bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
                _isScanning.value = true

                delay(scanDuration)
                stopScan()

            } catch (e: SecurityException) {
                _isScanning.value = false
            }
        }
    }


    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            try {
                val device = result.device ?: return

                // Filter out devices with empty names and addresses
                if (device.address.isNullOrEmpty() || device.name.isNullOrEmpty()) {
                    return
                }

                val bleDevice = BLEDevice(
                    name = device.name ?: "Unknown Device",
                    address = device.address,
                    rssi = result.rssi.toString()
                )

                viewModelScope.launch {
                    val currentDevices = _devices.value
                    val existingDeviceIndex =
                        currentDevices.indexOfFirst { it.address == device.address }

                    val updatedDevices = if (existingDeviceIndex != -1) {
                        currentDevices.toMutableList().apply {
                            this[existingDeviceIndex] = bleDevice
                        }
                    } else {
                        currentDevices + bleDevice
                    }

                    _devices.value = updatedDevices
                    persistentDevices = updatedDevices  // Update the persistent storage
                }
            } catch (e: SecurityException) {
                Log.e("BluetoothScan", "Permission error in scan callback", e)
            } catch (e: Exception) {
                Log.e("BluetoothScan", "Error processing scan result", e)
            }
        }
    }

    fun stopScan() {
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            Log.e("BluetoothScan", "Error stopping scan", e)
        }
    }

    fun clearDevices() {
        _devices.value = emptyList()
        persistentDevices = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_ENABLE_LOCATION = 2
        private var persistentDevices = listOf<BLEDevice>()
    }
}