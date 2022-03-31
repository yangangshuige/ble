package com.yg.ble

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import com.didi.bike.applicationholder.AppContextHolder
import com.yg.ble.bluetooth.BleBluetooth
import com.yg.ble.bluetooth.MultipleBluetoothController
import com.yg.ble.bluetooth.SplitWriter
import com.yg.ble.callback.*
import com.yg.ble.data.BleDevice
import com.yg.ble.data.BleScanState
import com.yg.ble.exception.OtherException
import com.yg.ble.scan.BleScanRuleConfig
import com.yg.ble.scan.BleScanner
import com.yg.ble.utils.BleLog
import java.lang.Exception
import java.util.*

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@SuppressLint("MissingPermission")
class BleManager {
    var maxConnectCount: Int = DEFAULT_MAX_MULTIPLE_DEVICE
    var operateTimeout: Int = DEFAULT_OPERATE_TIME
    var reConnectCount: Int = DEFAULT_CONNECT_RETRY_COUNT
    var reConnectInterval: Long = DEFAULT_CONNECT_RETRY_INTERVAL.toLong()
    var splitWriteNum: Int = DEFAULT_WRITE_DATA_SPLIT_COUNT
    var connectOverTime: Long = DEFAULT_CONNECT_OVER_TIME.toLong()

    var bleScanRuleConfig = BleScanRuleConfig()
    val context: Application
        get() {
            return AppContextHolder.applicationContext()
        }
    private val bluetoothManager: BluetoothManager
        get() {
            return context!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }
    val bluetoothAdapter: BluetoothAdapter?
        get() {
            return bluetoothManager?.adapter
        }
    var multipleBluetoothController = MultipleBluetoothController()

    /**
     * Configure scan and connection properties
     *
     * @param config
     */
    fun initScanRule(config: BleScanRuleConfig) {
        bleScanRuleConfig = config
    }

    /**
     * Set the maximum number of connections
     *
     * @param count
     * @return BleManager
     */
    fun setMaxConnectCount(count: Int): BleManager {
        var count = count
        if (count > DEFAULT_MAX_MULTIPLE_DEVICE) count = DEFAULT_MAX_MULTIPLE_DEVICE
        maxConnectCount = count
        return this
    }

    /**
     * Set operate timeout
     *
     * @param count
     * @return BleManager
     */
    fun setOperateTimeout(count: Int): BleManager {
        operateTimeout = count
        return this
    }

    /**
     * Set connect retry count and interval
     *
     * @param count
     * @return BleManager
     */
    fun setReConnectCount(count: Int): BleManager {
        return setReConnectCount(count,
            DEFAULT_CONNECT_RETRY_INTERVAL.toLong())
    }

    /**
     * Set connect retry count and interval
     *
     * @param count
     * @return BleManager
     */
    fun setReConnectCount(count: Int, interval: Long): BleManager {
        var count = count
        var interval = interval
        if (count > 10) count = 10
        if (interval < 0) interval = 0
        reConnectCount = count
        reConnectInterval = interval
        return this
    }

    /**
     * Set split Writ eNum
     *
     * @param num
     * @return BleManager
     */
    fun setSplitWriteNum(num: Int): BleManager {
        if (num > 0) {
            splitWriteNum = num
        }
        return this
    }

    /**
     * Set connect Over Time
     *
     * @param time
     * @return BleManager
     */
    fun setConnectOverTime(time: Long): BleManager {
        var time = time
        if (time <= 0) {
            time = 100
        }
        connectOverTime = time
        return this
    }

    /**
     * print log?
     *
     * @param enable
     * @return BleManager
     */
    fun enableLog(enable: Boolean): BleManager {
        BleLog.isPrint = enable
        return this
    }

    /**
     * scan device around
     *
     * @param callback
     */
    fun scan(callback: BleScanCallback) {
        if (!isBlueEnable()) {
            BleLog.e("Bluetooth not enable!")
            callback.onScanStarted(false)
            return
        }
        val serviceUuids: ArrayList<UUID> = bleScanRuleConfig.mServiceUuids
        val deviceNames: ArrayList<String> = bleScanRuleConfig.mDeviceNames
        val deviceMac: String = bleScanRuleConfig.mDeviceMac
        val fuzzy: Boolean = bleScanRuleConfig.mFuzzy
        val timeOut: Long = bleScanRuleConfig.mScanTimeOut
        BleScanner.instance.scan(serviceUuids, deviceNames, deviceMac, fuzzy, timeOut, callback)
    }

    /**
     * scan device then connect
     *
     * @param callback
     */
    fun scanAndConnect(callback: BleScanAndConnectCallback) {
        if (!isBlueEnable()) {
            BleLog.e("Bluetooth not enable!")
            callback.onScanStarted(false)
            return
        }
        val serviceUuids: ArrayList<UUID> = bleScanRuleConfig.mServiceUuids
        val deviceNames: ArrayList<String> = bleScanRuleConfig.mDeviceNames
        val deviceMac: String = bleScanRuleConfig.mDeviceMac
        val fuzzy: Boolean = bleScanRuleConfig.mFuzzy
        val timeOut: Long = bleScanRuleConfig.mScanTimeOut
        BleScanner.instance
            .scanAndConnect(serviceUuids, deviceNames, deviceMac, fuzzy, timeOut, callback)
    }

    /**
     * connect a known device
     *
     * @param bleDevice
     * @param bleGattCallback
     * @return
     */
    fun connect(
        bleDevice: BleDevice?,
        bleGattCallback: BleGattCallback,
    ): BluetoothGatt? {
        if (!isBlueEnable()) {
            BleLog.e("Bluetooth not enable!")
            bleGattCallback.onConnectFail(bleDevice,
                OtherException("Bluetooth not enable!"))
            return null
        }
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
            BleLog.w("Be careful: currentThread is not MainThread!")
        }
        if (bleDevice?.getDevice() == null) {
            bleGattCallback.onConnectFail(bleDevice,
                OtherException("Not Found Device Exception Occurred!"))
        } else {
            val bleBluetooth: BleBluetooth =
                multipleBluetoothController.buildConnectingBle(bleDevice)
            val autoConnect: Boolean = bleScanRuleConfig.mAutoConnect
            return bleBluetooth.connect(bleDevice, autoConnect, bleGattCallback)
        }
        return null
    }

    /**
     * connect a device through its mac without scan,whether or not it has been connected
     *
     * @param mac
     * @param bleGattCallback
     * @return
     */
    fun connect(
        mac: String,
        bleGattCallback: BleGattCallback,
    ): BluetoothGatt? {
        val bluetoothDevice: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(mac)
        val bleDevice = BleDevice(bluetoothDevice, 0, byteArrayOf(), 0)
        return connect(bleDevice, bleGattCallback)
    }

    /**
     * Cancel scan
     */
    fun cancelScan() {
        BleScanner.instance.stopLeScan()
    }

    /**
     * notify
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_notify
     * @param useCharacteristicDescriptor
     * @param callback
     */
    @JvmOverloads
    fun notify(
        bleDevice: BleDevice?,
        uuid_service: String,
        uuid_notify: String,
        useCharacteristicDescriptor: Boolean = false,
        callback: BleNotifyCallback,
    ) {
        val bleBluetooth: BleBluetooth? =
            multipleBluetoothController.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onNotifyFailure(OtherException("This device not connect!"))
        } else {
            bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_notify)
                ?.enableCharacteristicNotify(callback, uuid_notify, useCharacteristicDescriptor)
        }
    }

    /**
     * indicate
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_indicate
     * @param useCharacteristicDescriptor
     * @param callback
     */
    fun indicate(
        bleDevice: BleDevice?,
        uuid_service: String,
        uuid_indicate: String,
        useCharacteristicDescriptor: Boolean = false,
        callback: BleIndicateCallback,
    ) {
        val bleBluetooth: BleBluetooth? =
            multipleBluetoothController.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onIndicateFailure(OtherException("This device not connect!"))
        } else {
            bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_indicate)
                ?.enableCharacteristicIndicate(callback, uuid_indicate, useCharacteristicDescriptor)
        }
    }

    /**
     * stop notify, remove callback
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_notify
     * @param useCharacteristicDescriptor
     * @return
     */
    fun stopNotify(
        bleDevice: BleDevice?,
        uuid_service: String,
        uuid_notify: String,
        useCharacteristicDescriptor: Boolean,
    ): Boolean {
        val bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice)
            ?: return false
        val success = bleBluetooth.newBleConnector()
            .withUUIDString(uuid_service, uuid_notify)
            ?.disableCharacteristicNotify(useCharacteristicDescriptor)
        if (success == true) {
            bleBluetooth.removeNotifyCallback(uuid_notify)
        }
        return success == true
    }

    /**
     * stop indicate, remove callback
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_indicate
     * @param useCharacteristicDescriptor
     * @return
     */
    fun stopIndicate(
        bleDevice: BleDevice?,
        uuid_service: String,
        uuid_indicate: String,
        useCharacteristicDescriptor: Boolean = false,
    ): Boolean {
        val bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice)
            ?: return false
        val success = bleBluetooth.newBleConnector()
            .withUUIDString(uuid_service, uuid_indicate)
            ?.disableCharacteristicIndicate(useCharacteristicDescriptor)
        if (success == true) {
            bleBluetooth.removeIndicateCallback(uuid_indicate)
        }
        return success == true
    }

    /**
     * write
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_write
     * @param data
     * @param split
     * @param sendNextWhenLastSuccess
     * @param intervalBetweenTwoPackage
     * @param callback
     */
    fun write(
        bleDevice: BleDevice?,
        uuid_service: String,
        uuid_write: String,
        data: ByteArray?,
        split: Boolean = true,
        sendNextWhenLastSuccess: Boolean = true,
        intervalBetweenTwoPackage: Long = 0L,
        callback: BleWriteCallback,
    ) {
        if (data == null) {
            BleLog.e("data is Null!")
            callback.onWriteFailure(OtherException("data is Null!"))
            return
        }
        if (data.size > 20 && !split) {
            BleLog.w("Be careful: data's length beyond 20! Ensure MTU higher than 23, or use spilt write!")
        }
        val bleBluetooth: BleBluetooth? =
            multipleBluetoothController.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onWriteFailure(OtherException("This device not connect!"))
        } else {
            if (split && data.size > splitWriteNum) {
                SplitWriter()
                    .splitWrite(bleBluetooth, uuid_service, uuid_write, data,
                        sendNextWhenLastSuccess, intervalBetweenTwoPackage, callback)
            } else {
                bleBluetooth.newBleConnector()
                    .withUUIDString(uuid_service, uuid_write)
                    ?.writeCharacteristic(data, callback, uuid_write)
            }
        }
    }

    /**
     * read
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_read
     * @param callback
     */
    fun read(
        bleDevice: BleDevice?,
        uuid_service: String,
        uuid_read: String,
        callback: BleReadCallback,
    ) {
        val bleBluetooth: BleBluetooth? =
            multipleBluetoothController.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onReadFailure(OtherException("This device is not connected!"))
        } else {
            bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_read)
                ?.readCharacteristic(callback, uuid_read)
        }
    }

    /**
     * read Rssi
     *
     * @param bleDevice
     * @param callback
     */
    fun readRssi(
        bleDevice: BleDevice?,
        callback: BleRssiCallback,
    ) {
        val bleBluetooth: BleBluetooth? =
            multipleBluetoothController.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onRssiFailure(OtherException("This device is not connected!"))
        } else {
            bleBluetooth.newBleConnector().readRemoteRssi(callback)
        }
    }

    /**
     * set Mtu
     *
     * @param bleDevice
     * @param mtu
     * @param callback
     */
    fun setMtu(
        bleDevice: BleDevice?,
        mtu: Int,
        callback: BleMtuChangedCallback,
    ) {
        if (mtu > DEFAULT_MAX_MTU) {
            BleLog.e("requiredMtu should lower than 512 !")
            callback.onSetMTUFailure(OtherException("requiredMtu should lower than 512 !"))
            return
        }
        if (mtu < DEFAULT_MTU) {
            BleLog.e("requiredMtu should higher than 23 !")
            callback.onSetMTUFailure(OtherException("requiredMtu should higher than 23 !"))
            return
        }
        val bleBluetooth: BleBluetooth? =
            multipleBluetoothController.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onSetMTUFailure(OtherException("This device is not connected!"))
        } else {
            bleBluetooth.newBleConnector().setMtu(mtu, callback)
        }
    }

    /**
     * requestConnectionPriority
     *
     * @param connectionPriority Request a specific connection priority. Must be one of
     * [BluetoothGatt.CONNECTION_PRIORITY_BALANCED],
     * [BluetoothGatt.CONNECTION_PRIORITY_HIGH]
     * or [BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER].
     * @throws IllegalArgumentException If the parameters are outside of their
     * specified range.
     */
    fun requestConnectionPriority(
        bleDevice: BleDevice?,
        connectionPriority: Int,
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val bleBluetooth: BleBluetooth? =
                multipleBluetoothController.getBleBluetooth(bleDevice)
            return bleBluetooth?.newBleConnector()?.requestConnectionPriority(connectionPriority)
                ?: false
        }
        return false
    }

    /**
     * is support ble?
     *
     * @return
     */
    fun isSupportBle(): Boolean {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && context.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
    }

    /**
     * Open bluetooth
     */
    fun enableBluetooth() {
        bluetoothAdapter?.enable()
    }

    /**
     * Disable bluetooth
     */
    fun disableBluetooth() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter!!.isEnabled) bluetoothAdapter!!.disable()
        }
    }

    /**
     * judge Bluetooth is enable
     *
     * @return
     */
    fun isBlueEnable(): Boolean {
        return bluetoothAdapter != null && bluetoothAdapter!!.isEnabled
    }

    fun convertBleDevice(bluetoothDevice: BluetoothDevice?): BleDevice? {
        return BleDevice(bluetoothDevice)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun convertBleDevice(scanResult: ScanResult): BleDevice? {
        val bluetoothDevice = scanResult.device
        val rssi = scanResult.rssi
        val scanRecord = scanResult.scanRecord
        var bytes: ByteArray = byteArrayOf()
        if (scanRecord != null) bytes = scanRecord.bytes
        val timestampNanos = scanResult.timestampNanos
        return BleDevice(bluetoothDevice, rssi, bytes, timestampNanos)
    }

    fun getBleBluetooth(bleDevice: BleDevice?): BleBluetooth? {
        return if (multipleBluetoothController != null) {
            multipleBluetoothController.getBleBluetooth(bleDevice)
        } else null
    }

    fun getBluetoothGatt(bleDevice: BleDevice?): BluetoothGatt? {
        return getBleBluetooth(bleDevice)?.getBluetoothGatt()
    }

    fun getBluetoothGattServices(bleDevice: BleDevice?): List<BluetoothGattService?>? {
        val gatt = getBluetoothGatt(bleDevice)
        return gatt?.services
    }

    fun getBluetoothGattCharacteristics(service: BluetoothGattService): List<BluetoothGattCharacteristic?>? {
        return service.characteristics
    }

    fun removeConnectGattCallback(bleDevice: BleDevice?) {
        getBleBluetooth(bleDevice)?.removeConnectGattCallback()
    }

    fun removeRssiCallback(bleDevice: BleDevice?) {
        getBleBluetooth(bleDevice)?.removeRssiCallback()
    }

    fun removeMtuChangedCallback(bleDevice: BleDevice?) {
        getBleBluetooth(bleDevice)?.removeMtuChangedCallback()
    }

    fun removeNotifyCallback(bleDevice: BleDevice?, uuid_notify: String) {
        getBleBluetooth(bleDevice)?.removeNotifyCallback(uuid_notify)
    }

    fun removeIndicateCallback(bleDevice: BleDevice?, uuid_indicate: String) {
        getBleBluetooth(bleDevice)?.removeIndicateCallback(uuid_indicate)
    }

    fun removeWriteCallback(bleDevice: BleDevice?, uuid_write: String) {
        getBleBluetooth(bleDevice)?.removeWriteCallback(uuid_write)
    }

    fun removeReadCallback(bleDevice: BleDevice?, uuid_read: String) {
        getBleBluetooth(bleDevice)?.removeReadCallback(uuid_read)
    }

    fun clearCharacterCallback(bleDevice: BleDevice?) {
        getBleBluetooth(bleDevice)?.clearCharacterCallback()
    }

    fun getScanSate(): BleScanState? {
        return BleScanner.instance.mBleScanState
    }

    fun getAllConnectedDevice(): List<BleDevice?>? {
        return if (multipleBluetoothController == null) null else multipleBluetoothController.getDeviceList()
    }

    /**
     * @param bleDevice
     * @return State of the profile connection. One of
     * [BluetoothProfile.STATE_CONNECTED],
     * [BluetoothProfile.STATE_CONNECTING],
     * [BluetoothProfile.STATE_DISCONNECTED],
     * [BluetoothProfile.STATE_DISCONNECTING]
     */
    private fun getConnectState(bleDevice: BleDevice?): Int {
        return if (bleDevice != null) {
            bluetoothManager!!.getConnectionState(bleDevice.getDevice(), BluetoothProfile.GATT)
        } else {
            BluetoothProfile.STATE_DISCONNECTED
        }
    }

    fun isConnected(bleDevice: BleDevice?): Boolean {
        return getConnectState(bleDevice) == BluetoothProfile.STATE_CONNECTED
    }

    fun isConnected(mac: String): Boolean {
        val list: List<BleDevice?>? = getAllConnectedDevice()
        if (list != null) {
            for (bleDevice in list) {
                if (bleDevice != null) {
                    if (bleDevice.getMac() == mac) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun disconnect(bleDevice: BleDevice?) {
        multipleBluetoothController?.disconnect(bleDevice)
    }

    fun disconnectAllDevice() {
        multipleBluetoothController?.disconnectAllDevice()
    }

    fun destroy() {
        multipleBluetoothController?.destroy()
    }

    fun removeBondWithDevice(device: BluetoothDevice): Boolean {
        return try {
            val method = BluetoothDevice::class.java.getMethod("removeBond")
            method.invoke(device)

            return true
        } catch (ignore: Exception) {
            false
        }
    }

    companion object {
        const val DEFAULT_SCAN_TIME = 10000
        const val DEFAULT_MAX_MULTIPLE_DEVICE = 7
        const val DEFAULT_OPERATE_TIME = 5000
        const val DEFAULT_CONNECT_RETRY_COUNT = 0
        const val DEFAULT_CONNECT_RETRY_INTERVAL = 5000
        const val DEFAULT_MTU = 23
        const val DEFAULT_MAX_MTU = 512
        const val DEFAULT_WRITE_DATA_SPLIT_COUNT = 20
        const val DEFAULT_CONNECT_OVER_TIME = 10000
        val instance: BleManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            BleManager()
        }
    }
}