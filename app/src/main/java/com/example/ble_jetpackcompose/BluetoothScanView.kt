import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_jetpackcompose.BLEDevice
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

    fun startScan() {
        // Check Bluetooth availability and permissions
        if (!hasBluetoothPermissions()) {
            Log.w("BluetoothScan", "Permissions not granted")
            return



        }



        viewModelScope.launch {
            try {
                bluetoothLeScanner?.startScan(
                    listOf(),

                    ScanSettings.Builder().build(),

                    scanCallback
                )
            } catch (e: SecurityException) {
                Log.e("BluetoothScan", "Security exception during scan", e)
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasBluetoothPermissions(): Boolean {
        val permissions = arrayOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            try {
                val device = result.device
                val existingDeviceIndex = _devices.value.indexOfFirst { it.address == device.address }

                val bleDevice = BLEDevice(
                    name = device.name ?: "Unknown Device",
                    address = device.address,
                    rssi = result.rssi.toString()
                )

                val updatedDevices = if (existingDeviceIndex != -1) {
                    _devices.value.toMutableList().apply {
                        this[existingDeviceIndex] = bleDevice
                    }
                } else {
                    _devices.value + bleDevice
                }

                _devices.value = updatedDevices
            } catch (e: SecurityException) {
                Log.e("BluetoothScan", "Permission error in scan callback", e)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BluetoothScan", "Scan failed with error code: $errorCode")
        }
    }

    fun stopScan() {
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            Log.e("BluetoothScan", "Error stopping scan", e)
        }
    }
}