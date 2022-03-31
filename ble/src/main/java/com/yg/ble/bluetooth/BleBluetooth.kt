package com.yg.ble.bluetooth

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.*
import android.os.*
import com.yg.ble.BleManager
import com.yg.ble.callback.*
import com.yg.ble.data.BleConnectStateParameter
import com.yg.ble.data.BleDevice
import com.yg.ble.data.BleMsg
import com.yg.ble.exception.ConnectException
import com.yg.ble.exception.OtherException
import com.yg.ble.exception.TimeoutException
import com.yg.ble.utils.BleLog

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@SuppressLint("MissingPermission")
class BleBluetooth(device: BleDevice) {
    private var bleGattCallback: BleGattCallback? = null
    private var bleRssiCallback: BleRssiCallback? = null
    private var bleMtuChangedCallback: BleMtuChangedCallback? = null
    private val bleNotifyCallbackHashMap = HashMap<String, BleNotifyCallback>()
    private val bleIndicateCallbackHashMap = HashMap<String, BleIndicateCallback>()
    private val bleWriteCallbackHashMap = HashMap<String, BleWriteCallback>()
    private val bleReadCallbackHashMap = HashMap<String, BleReadCallback>()

    private var lastState: LastState? = null
    private var isActiveDisconnect = false
    private val bleDevice: BleDevice = device
    private var bluetoothGatt: BluetoothGatt? = null
    private val mainHandler: MainHandler = MainHandler(Looper.getMainLooper())
    private var connectRetryCount = 0

    fun newBleConnector(): BleConnector {
        return BleConnector(this)
    }

    @Synchronized
    fun addConnectGattCallback(callback: BleGattCallback) {
        bleGattCallback = callback
    }

    @Synchronized
    fun removeConnectGattCallback() {
        bleGattCallback = null
    }

    @Synchronized
    fun addNotifyCallback(
        uuid: String,
        bleNotifyCallback: BleNotifyCallback
    ) {
        bleNotifyCallbackHashMap[uuid] = bleNotifyCallback
    }

    @Synchronized
    fun addIndicateCallback(
        uuid: String,
        bleIndicateCallback: BleIndicateCallback
    ) {
        bleIndicateCallbackHashMap[uuid] = bleIndicateCallback
    }

    @Synchronized
    fun addWriteCallback(
        uuid: String,
        bleWriteCallback: BleWriteCallback
    ) {
        bleWriteCallbackHashMap[uuid] = bleWriteCallback
    }

    @Synchronized
    fun addReadCallback(uuid: String, bleReadCallback: BleReadCallback) {
        bleReadCallbackHashMap[uuid] = bleReadCallback
    }

    @Synchronized
    fun removeNotifyCallback(uuid: String) {
        if (bleNotifyCallbackHashMap.containsKey(uuid)) bleNotifyCallbackHashMap.remove(uuid)
    }

    @Synchronized
    fun removeIndicateCallback(uuid: String) {
        if (bleIndicateCallbackHashMap.containsKey(uuid)) bleIndicateCallbackHashMap.remove(uuid)
    }

    @Synchronized
    fun removeWriteCallback(uuid: String) {
        if (bleWriteCallbackHashMap.containsKey(uuid)) bleWriteCallbackHashMap.remove(uuid)
    }

    @Synchronized
    fun removeReadCallback(uuid: String) {
        if (bleReadCallbackHashMap.containsKey(uuid)) bleReadCallbackHashMap.remove(uuid)
    }

    @Synchronized
    fun clearCharacterCallback() {
        bleNotifyCallbackHashMap.clear()
        bleIndicateCallbackHashMap.clear()
        bleWriteCallbackHashMap.clear()
        bleReadCallbackHashMap.clear()
    }

    @Synchronized
    fun addRssiCallback(callback: BleRssiCallback) {
        bleRssiCallback = callback
    }

    @Synchronized
    fun removeRssiCallback() {
        bleRssiCallback = null
    }

    @Synchronized
    fun addMtuChangedCallback(callback: BleMtuChangedCallback) {
        bleMtuChangedCallback = callback
    }

    @Synchronized
    fun removeMtuChangedCallback() {
        bleMtuChangedCallback = null
    }


    fun getDeviceKey(): String {
        return bleDevice.getKey()
    }

    fun getDevice(): BleDevice {
        return bleDevice
    }

    fun getBluetoothGatt(): BluetoothGatt? {
        return bluetoothGatt
    }

    @Synchronized
    fun connect(
        bleDevice: BleDevice,
        autoConnect: Boolean,
        callback: BleGattCallback
    ): BluetoothGatt? {
        return connect(bleDevice, autoConnect, callback, 0)
    }


    @Synchronized
    fun connect(
        bleDevice: BleDevice,
        autoConnect: Boolean,
        callback: BleGattCallback?,
        connectRetryCount: Int
    ): BluetoothGatt? {
        BleLog.i("""
    connect device: ${bleDevice.getName()}
    mac: ${bleDevice.getMac()}
    autoConnect: $autoConnect
    currentThread: ${Thread.currentThread().id}
    connectCount:${connectRetryCount + 1}
    """.trimIndent())
        if (connectRetryCount == 0) {
            this.connectRetryCount = 0
        }
        if (callback != null)
            addConnectGattCallback(callback)
        lastState = LastState.CONNECT_CONNECTING
        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bleDevice.getDevice()?.connectGatt(BleManager.instance.context,
                autoConnect, coreGattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            bleDevice.getDevice()?.connectGatt(BleManager.instance.context,
                autoConnect, coreGattCallback)
        }
        if (bluetoothGatt != null) {
            bleGattCallback!!.onStartConnect()
            val message: Message = mainHandler.obtainMessage()
            message.what = BleMsg.MSG_CONNECT_OVER_TIME
            mainHandler.sendMessageDelayed(message,
                BleManager.instance.connectOverTime)
        } else {
            disconnectGatt()
            refreshDeviceCache()
            closeBluetoothGatt()
            lastState = LastState.CONNECT_FAILURE
            BleManager.instance.multipleBluetoothController
                ?.removeConnectingBle(this@BleBluetooth)
            bleGattCallback!!.onConnectFail(bleDevice,
                OtherException("GATT connect exception occurred!"))
        }
        return bluetoothGatt
    }

    @Synchronized
    fun disconnect() {
        isActiveDisconnect = true
        disconnectGatt()
    }

    @Synchronized
    fun destroy() {
        lastState = LastState.CONNECT_IDLE
        disconnectGatt()
        refreshDeviceCache()
        closeBluetoothGatt()
        removeConnectGattCallback()
        removeRssiCallback()
        removeMtuChangedCallback()
        clearCharacterCallback()
        mainHandler.removeCallbacksAndMessages(null)
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    private fun disconnectGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt!!.disconnect()
        }
    }

    @Synchronized
    private fun refreshDeviceCache() {
        try {
            val refresh = BluetoothGatt::class.java.getMethod("refresh")
            if (refresh != null && bluetoothGatt != null) {
                val success = refresh.invoke(bluetoothGatt) as Boolean
                BleLog.i("refreshDeviceCache, is success:  $success")
            }
        } catch (e: Exception) {
            BleLog.i("exception occur while refreshing device: " + e.message)
            e.printStackTrace()
        }
    }


    @SuppressLint("MissingPermission")
    @Synchronized
    private fun closeBluetoothGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt!!.close()
        }
    }

    inner class MainHandler constructor(looper: Looper) : Handler(looper) {
        @SuppressLint("MissingPermission")
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BleMsg.MSG_CONNECT_FAIL -> {
                    disconnectGatt()
                    refreshDeviceCache()
                    closeBluetoothGatt()
                    if (connectRetryCount < BleManager.instance
                            .reConnectCount
                    ) {
                        BleLog.e("Connect fail, try reconnect " + BleManager.instance
                            .reConnectInterval + " millisecond later")
                        ++connectRetryCount
                        val message: Message = mainHandler.obtainMessage()
                        message.what = BleMsg.MSG_RECONNECT
                        mainHandler.sendMessageDelayed(message,
                            BleManager.instance.reConnectInterval)
                    } else {
                        lastState = LastState.CONNECT_FAILURE
                        BleManager.instance.multipleBluetoothController?.removeConnectingBle(this@BleBluetooth)
                        val para: BleConnectStateParameter =
                            msg.obj as BleConnectStateParameter
                        val status: Int = para.status
                        bleGattCallback?.onConnectFail(bleDevice,
                            ConnectException())
                    }
                }
                BleMsg.MSG_DISCONNECTED -> {
                    lastState = LastState.CONNECT_DISCONNECT
                    BleManager.instance.multipleBluetoothController
                        ?.removeBleBluetooth(this@BleBluetooth)
                    disconnect()
                    refreshDeviceCache()
                    closeBluetoothGatt()
                    removeRssiCallback()
                    removeMtuChangedCallback()
                    clearCharacterCallback()
                    mainHandler.removeCallbacksAndMessages(null)
                    val para: BleConnectStateParameter =
                        msg.obj as BleConnectStateParameter
                    val isActive: Boolean = para.isActive
                    val status: Int = para.status
                    bleGattCallback?.onDisConnected(isActive,
                        bleDevice,
                        bluetoothGatt,
                        status)
                }
                BleMsg.MSG_RECONNECT -> {
                    connect(bleDevice, false, bleGattCallback, connectRetryCount)
                }
                BleMsg.MSG_CONNECT_OVER_TIME -> {
                    disconnectGatt()
                    refreshDeviceCache()
                    closeBluetoothGatt()
                    lastState = LastState.CONNECT_FAILURE
                    BleManager.instance.multipleBluetoothController
                        ?.removeConnectingBle(this@BleBluetooth)
                    bleGattCallback?.onConnectFail(bleDevice,
                        TimeoutException())
                }
                BleMsg.MSG_DISCOVER_SERVICES -> {
                    if (bluetoothGatt != null) {
                        val discoverServiceResult: Boolean = bluetoothGatt!!.discoverServices()
                        if (!discoverServiceResult) {
                            val message: Message = mainHandler.obtainMessage()
                            message.what = BleMsg.MSG_DISCOVER_FAIL
                            mainHandler.sendMessage(message)
                        }
                    } else {
                        val message: Message = mainHandler.obtainMessage()
                        message.what = BleMsg.MSG_DISCOVER_FAIL
                        mainHandler.sendMessage(message)
                    }
                }
                BleMsg.MSG_DISCOVER_FAIL -> {
                    disconnectGatt()
                    refreshDeviceCache()
                    closeBluetoothGatt()
                    lastState = LastState.CONNECT_FAILURE
                    BleManager.instance.multipleBluetoothController
                        ?.removeConnectingBle(this@BleBluetooth)
                    bleGattCallback?.onConnectFail(bleDevice,
                        OtherException("GATT discover services exception occurred!"))
                }
                BleMsg.MSG_DISCOVER_SUCCESS -> {
                    lastState = LastState.CONNECT_CONNECTED
                    isActiveDisconnect = false
                    BleManager.instance.multipleBluetoothController
                        ?.removeConnectingBle(this@BleBluetooth)
                    BleManager.instance.multipleBluetoothController
                        ?.addBleBluetooth(this@BleBluetooth)
                    val para: BleConnectStateParameter =
                        msg.obj as BleConnectStateParameter
                    val status: Int = para.status
                    bleGattCallback?.onConnectSuccess(bleDevice,
                        bluetoothGatt,
                        status)
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private val coreGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            BleLog.i("""
    BluetoothGattCallback：onConnectionStateChange 
    status: $status
    newState: $newState
    currentThread: ${Thread.currentThread().id}
    """.trimIndent())
            bluetoothGatt = gatt
            mainHandler.removeMessages(BleMsg.MSG_CONNECT_OVER_TIME)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                val message = mainHandler.obtainMessage()
                message.what = BleMsg.MSG_DISCOVER_SERVICES
                mainHandler.sendMessageDelayed(message, 500)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (lastState == LastState.CONNECT_CONNECTING) {
                    val message = mainHandler.obtainMessage()
                    message.what = BleMsg.MSG_CONNECT_FAIL
                    message.obj = BleConnectStateParameter(status)
                    mainHandler.sendMessage(message)
                } else if (lastState == LastState.CONNECT_CONNECTED) {
                    val message = mainHandler.obtainMessage()
                    message.what = BleMsg.MSG_DISCONNECTED
                    val para = BleConnectStateParameter(status)
                    para.isActive = isActiveDisconnect
                    message.obj = para
                    mainHandler.sendMessage(message)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            BleLog.i("""
    BluetoothGattCallback：onServicesDiscovered 
    status: $status
    currentThread: ${Thread.currentThread().id}
    """.trimIndent())
            bluetoothGatt = gatt
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val message = mainHandler.obtainMessage()
                message.what = BleMsg.MSG_DISCOVER_SUCCESS
                message.obj = BleConnectStateParameter(status)
                mainHandler.sendMessage(message)
            } else {
                val message = mainHandler.obtainMessage()
                message.what = BleMsg.MSG_DISCOVER_FAIL
                mainHandler.sendMessage(message)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            var iterator: Iterator<*> = bleNotifyCallbackHashMap.entries.iterator()
            while (iterator.hasNext()) {
                val (_, value) = iterator.next() as Map.Entry<*, *>
                val callback = value!!
                if (callback is BleNotifyCallback) {
                    if (characteristic.uuid.toString()
                            .equals(callback.key, ignoreCase = true)
                    ) {
                        val handler: Handler? = callback.handler
                        if (handler != null) {
                            val message = handler.obtainMessage()
                            message.what = BleMsg.MSG_CHA_NOTIFY_DATA_CHANGE
                            message.obj = callback
                            val bundle = Bundle()
                            bundle.putByteArray(BleMsg.KEY_NOTIFY_BUNDLE_VALUE,
                                characteristic.value)
                            message.data = bundle
                            handler.sendMessage(message)
                        }
                    }
                }
            }
            iterator = bleIndicateCallbackHashMap.entries.iterator()
            while (iterator.hasNext()) {
                val (_, value) = iterator.next()
                val callback = value!!
                if (callback is BleIndicateCallback) {
                    if (characteristic.uuid.toString()
                            .equals(callback.key, ignoreCase = true)
                    ) {
                        val handler: Handler? = callback.handler
                        if (handler != null) {
                            val message = handler.obtainMessage()
                            message.what = BleMsg.MSG_CHA_INDICATE_DATA_CHANGE
                            message.obj = callback
                            val bundle = Bundle()
                            bundle.putByteArray(BleMsg.KEY_INDICATE_BUNDLE_VALUE,
                                characteristic.value)
                            message.data = bundle
                            handler.sendMessage(message)
                        }
                    }
                }
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            var iterator: Iterator<*> = bleNotifyCallbackHashMap.entries.iterator()
            while (iterator.hasNext()) {
                val (_, value) = iterator.next() as Map.Entry<*, *>
                val callback = value!!
                if (callback is BleNotifyCallback) {
                    if (descriptor.characteristic.uuid.toString()
                            .equals(callback.key, ignoreCase = true)
                    ) {
                        val handler: Handler? = callback.handler
                        if (handler != null) {
                            val message = handler.obtainMessage()
                            message.what = BleMsg.MSG_CHA_NOTIFY_RESULT
                            message.obj = callback
                            val bundle = Bundle()
                            bundle.putInt(BleMsg.KEY_NOTIFY_BUNDLE_STATUS,
                                status)
                            message.data = bundle
                            handler.sendMessage(message)
                        }
                    }
                }
            }
            iterator = bleIndicateCallbackHashMap.entries.iterator()
            while (iterator.hasNext()) {
                val (_, value) = iterator.next() as Map.Entry<*, *>
                val callback = value!!
                if (callback is BleIndicateCallback) {
                    if (descriptor.characteristic.uuid.toString()
                            .equals(callback.key, ignoreCase = true)
                    ) {
                        val handler: Handler? = callback.handler
                        if (handler != null) {
                            val message = handler.obtainMessage()
                            message.what = BleMsg.MSG_CHA_INDICATE_RESULT
                            message.obj = callback
                            val bundle = Bundle()
                            bundle.putInt(BleMsg.KEY_INDICATE_BUNDLE_STATUS,
                                status)
                            message.data = bundle
                            handler.sendMessage(message)
                        }
                    }
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            val iterator: Iterator<*> = bleWriteCallbackHashMap.entries.iterator()
            while (iterator.hasNext()) {
                val (_, value) = iterator.next() as Map.Entry<*, *>
                val callback = value!!
                if (callback is BleWriteCallback) {
                    if (characteristic.uuid.toString()
                            .equals(callback.key, ignoreCase = true)
                    ) {
                        val handler: Handler? = callback.handler
                        if (handler != null) {
                            val message = handler.obtainMessage()
                            message.what = BleMsg.MSG_CHA_WRITE_RESULT
                            message.obj = callback
                            val bundle = Bundle()
                            bundle.putInt(BleMsg.KEY_WRITE_BUNDLE_STATUS,
                                status)
                            bundle.putByteArray(BleMsg.KEY_WRITE_BUNDLE_VALUE,
                                characteristic.value)
                            message.data = bundle
                            handler.sendMessage(message)
                        }
                    }
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            val iterator: Iterator<*> = bleReadCallbackHashMap.entries.iterator()
            while (iterator.hasNext()) {
                val (_, value) = iterator.next() as Map.Entry<*, *>
                val callback = value!!
                if (callback is BleReadCallback) {
                    if (characteristic.uuid.toString()
                            .equals(callback.key, ignoreCase = true)
                    ) {
                        val handler: Handler? = callback.handler
                        if (handler != null) {
                            val message = handler.obtainMessage()
                            message.what = BleMsg.MSG_CHA_READ_RESULT
                            message.obj = callback
                            val bundle = Bundle()
                            bundle.putInt(BleMsg.KEY_READ_BUNDLE_STATUS,
                                status)
                            bundle.putByteArray(BleMsg.KEY_READ_BUNDLE_VALUE,
                                characteristic.value)
                            message.data = bundle
                            handler.sendMessage(message)
                        }
                    }
                }
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            val handler: Handler? = bleRssiCallback?.handler
            if (handler != null) {
                val message = handler.obtainMessage()
                message.what = BleMsg.MSG_READ_RSSI_RESULT
                message.obj = bleRssiCallback
                val bundle = Bundle()
                bundle.putInt(BleMsg.KEY_READ_RSSI_BUNDLE_STATUS, status)
                bundle.putInt(BleMsg.KEY_READ_RSSI_BUNDLE_VALUE, rssi)
                message.data = bundle
                handler.sendMessage(message)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            val handler: Handler? = bleMtuChangedCallback?.handler
            if (handler != null) {
                val message = handler.obtainMessage()
                message.what = BleMsg.MSG_SET_MTU_RESULT
                message.obj = bleMtuChangedCallback
                val bundle = Bundle()
                bundle.putInt(BleMsg.KEY_SET_MTU_BUNDLE_STATUS, status)
                bundle.putInt(BleMsg.KEY_SET_MTU_BUNDLE_VALUE, mtu)
                message.data = bundle
                handler.sendMessage(message)
            }
        }
    }

    internal enum class LastState {
        CONNECT_IDLE, CONNECT_CONNECTING, CONNECT_CONNECTED, CONNECT_FAILURE, CONNECT_DISCONNECT
    }
}