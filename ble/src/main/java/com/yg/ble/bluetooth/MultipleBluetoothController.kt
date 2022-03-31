package com.yg.ble.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import com.yg.ble.BleManager
import com.yg.ble.data.BleDevice
import com.yg.ble.utils.BleLruHashMap
import java.util.*
import kotlin.Comparator

@SuppressLint("MissingPermission")
class MultipleBluetoothController {
    private var bleLruHashMap =
        BleLruHashMap<String, BleBluetooth>(BleManager.DEFAULT_MAX_MULTIPLE_DEVICE)
    private var bleTempHashMap = HashMap<String, BleBluetooth>()

    @Synchronized
    fun buildConnectingBle(bleDevice: BleDevice): BleBluetooth {
        val bleBluetooth = BleBluetooth(bleDevice)
        if (!bleTempHashMap.containsKey(bleBluetooth.getDeviceKey())) {
            bleTempHashMap[bleBluetooth.getDeviceKey()] = bleBluetooth
        }
        return bleBluetooth
    }

    @Synchronized
    fun removeConnectingBle(bleBluetooth: BleBluetooth?) {
        if (bleBluetooth == null) {
            return
        }
        if (bleTempHashMap.containsKey(bleBluetooth.getDeviceKey())) {
            bleTempHashMap.remove(bleBluetooth.getDeviceKey())
        }
    }

    @Synchronized
    fun addBleBluetooth(bleBluetooth: BleBluetooth?) {
        if (bleBluetooth == null) {
            return
        }
        if (!bleLruHashMap.containsKey(bleBluetooth.getDeviceKey())) {
            bleLruHashMap[bleBluetooth.getDeviceKey()] = bleBluetooth
        }
    }

    @Synchronized
    fun removeBleBluetooth(bleBluetooth: BleBluetooth?) {
        if (bleBluetooth == null) {
            return
        }
        if (bleLruHashMap.containsKey(bleBluetooth.getDeviceKey())) {
            bleLruHashMap.remove(bleBluetooth.getDeviceKey())
        }
    }

    @Synchronized
    fun isContainDevice(bleDevice: BleDevice?): Boolean {
        return bleDevice != null && bleLruHashMap.containsKey(bleDevice.getKey())
    }

    @Synchronized
    fun isContainDevice(bluetoothDevice: BluetoothDevice?): Boolean {
        return bluetoothDevice != null && bleLruHashMap.containsKey(bluetoothDevice.name + bluetoothDevice.address)
    }

    @Synchronized
    fun getBleBluetooth(bleDevice: BleDevice?): BleBluetooth? {
        if (bleDevice != null) {
            if (bleLruHashMap.containsKey(bleDevice.getKey())) {
                return bleLruHashMap[bleDevice.getKey()]
            }
        }
        return null
    }

    @Synchronized
    fun disconnect(bleDevice: BleDevice?) {
        if (isContainDevice(bleDevice)) {
            getBleBluetooth(bleDevice)?.disconnect()
        }
    }

    @Synchronized
    fun disconnectAllDevice() {
        for ((_, value) in bleLruHashMap) {
            value.disconnect()
        }
        bleLruHashMap.clear()
    }

    @Synchronized
    fun destroy() {
        for ((_, value) in bleLruHashMap) {
            value.destroy()
        }
        bleLruHashMap.clear()
        for ((_, value) in bleTempHashMap) {
            value.destroy()
        }
        bleTempHashMap.clear()
    }

    @Synchronized
    fun getBleBluetoothList(): List<BleBluetooth> {
        val bleBluetoothList = ArrayList(bleLruHashMap.values)
        bleBluetoothList.sortWith(Comparator<BleBluetooth> { lhs, rhs ->
            lhs.getDeviceKey().compareTo(rhs.getDeviceKey(), ignoreCase = true)
        })
        return bleBluetoothList
    }

    @Synchronized
    fun getDeviceList(): List<BleDevice> {
        refreshConnectedDevice()
        val deviceList = ArrayList<BleDevice>()
        for (BleBluetooth in getBleBluetoothList()) {
            if (BleBluetooth != null) {
                deviceList.add(BleBluetooth.getDevice())
            }
        }
        return deviceList
    }

    fun refreshConnectedDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val bluetoothList: List<BleBluetooth> = getBleBluetoothList()
            var i = 0
            while (bluetoothList != null && i < bluetoothList.size) {
                val bleBluetooth: BleBluetooth = bluetoothList[i]
                if (!BleManager.instance
                        .isConnected(bleBluetooth.getDevice())
                ) {
                    removeBleBluetooth(bleBluetooth)
                }
                i++
            }
        }
    }
}