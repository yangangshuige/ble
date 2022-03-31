package com.yg.ble.bluetooth

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.yg.ble.BleManager
import com.yg.ble.callback.*
import com.yg.ble.data.BleMsg
import com.yg.ble.data.BleWriteState
import com.yg.ble.exception.GattException
import com.yg.ble.exception.OtherException
import com.yg.ble.exception.TimeoutException
import java.util.*

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@SuppressLint("MissingPermission")
class BleConnector {
    companion object {
        private val UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR =
            "00002902-0000-1000-8000-00805f9b34fb"
    }


    private var mBluetoothGatt: BluetoothGatt? = null
    private var mGattService: BluetoothGattService? = null
    private var mCharacteristic: BluetoothGattCharacteristic? = null
    private var mBleBluetooth: BleBluetooth? = null
    private var mHandler: Handler? = null

    constructor(bleBluetooth: BleBluetooth) {
        mBleBluetooth = bleBluetooth
        mBluetoothGatt = bleBluetooth.getBluetoothGatt()
        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    BleMsg.MSG_CHA_NOTIFY_START -> {
                        (msg.obj as BleNotifyCallback?)?.onNotifyFailure(TimeoutException())
                    }
                    BleMsg.MSG_CHA_NOTIFY_RESULT -> {
                        notifyMsgInit()
                        val notifyCallback: BleNotifyCallback? =
                            msg.obj as BleNotifyCallback?
                        val bundle = msg.data
                        val status =
                            bundle.getInt(BleMsg.KEY_NOTIFY_BUNDLE_STATUS)
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            notifyCallback?.onNotifySuccess()
                        } else {
                            notifyCallback?.onNotifyFailure(GattException())
                        }
                    }
                    BleMsg.MSG_CHA_NOTIFY_DATA_CHANGE -> {
                        val bundle = msg.data
                        val value =
                            bundle.getByteArray(BleMsg.KEY_NOTIFY_BUNDLE_VALUE)
                        (msg.obj as BleNotifyCallback?)?.onCharacteristicChanged(value)
                    }
                    BleMsg.MSG_CHA_INDICATE_START -> {
                        (msg.obj as BleIndicateCallback?)?.onIndicateFailure(
                            TimeoutException())
                    }
                    BleMsg.MSG_CHA_INDICATE_RESULT -> {
                        indicateMsgInit()
                        val indicateCallback: BleIndicateCallback? =
                            msg.obj as BleIndicateCallback?
                        val bundle = msg.data
                        val status =
                            bundle.getInt(BleMsg.KEY_INDICATE_BUNDLE_STATUS)
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            indicateCallback?.onIndicateSuccess()
                        } else {
                            indicateCallback?.onIndicateFailure(GattException())
                        }
                    }
                    BleMsg.MSG_CHA_INDICATE_DATA_CHANGE -> {
                        val bundle = msg.data
                        val value =
                            bundle.getByteArray(BleMsg.KEY_INDICATE_BUNDLE_VALUE)
                        (msg.obj as BleIndicateCallback?)?.onCharacteristicChanged(value)
                    }
                    BleMsg.MSG_CHA_WRITE_START -> {
                        (msg.obj as BleWriteCallback?)?.onWriteFailure(TimeoutException())
                    }
                    BleMsg.MSG_CHA_WRITE_RESULT -> {
                        writeMsgInit()
                        val writeCallback: BleWriteCallback? =
                            msg.obj as BleWriteCallback?
                        val bundle = msg.data
                        val status =
                            bundle.getInt(BleMsg.KEY_WRITE_BUNDLE_STATUS)
                        val value =
                            bundle.getByteArray(BleMsg.KEY_WRITE_BUNDLE_VALUE)?: byteArrayOf()
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            writeCallback?.onWriteSuccess(BleWriteState.DATA_WRITE_SINGLE,
                                BleWriteState.DATA_WRITE_SINGLE,
                                value)
                        } else {
                            writeCallback?.onWriteFailure(GattException())
                        }
                    }
                    BleMsg.MSG_CHA_READ_START -> {
                        (msg.obj as BleReadCallback?)?.onReadFailure(TimeoutException())
                    }
                    BleMsg.MSG_CHA_READ_RESULT -> {
                        readMsgInit()
                        val readCallback: BleReadCallback? =
                            msg.obj as BleReadCallback?
                        val bundle = msg.data
                        val status =
                            bundle.getInt(BleMsg.KEY_READ_BUNDLE_STATUS)
                        val value =
                            bundle.getByteArray(BleMsg.KEY_READ_BUNDLE_VALUE)
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            readCallback?.onReadSuccess(value)
                        } else {
                            readCallback?.onReadFailure(GattException())
                        }
                    }
                    BleMsg.MSG_READ_RSSI_START -> {
                        (msg.obj as BleRssiCallback?)?.onRssiFailure(TimeoutException())
                    }
                    BleMsg.MSG_READ_RSSI_RESULT -> {
                        rssiMsgInit()
                        val rssiCallback: BleRssiCallback? =
                            msg.obj as BleRssiCallback?
                        val bundle = msg.data
                        val status =
                            bundle.getInt(BleMsg.KEY_READ_RSSI_BUNDLE_STATUS)
                        val value =
                            bundle.getInt(BleMsg.KEY_READ_RSSI_BUNDLE_VALUE)
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            rssiCallback?.onRssiSuccess(value)
                        } else {
                            rssiCallback?.onRssiFailure(GattException())
                        }
                    }
                    BleMsg.MSG_SET_MTU_START -> {
                        (msg.obj as BleMtuChangedCallback?)?.onSetMTUFailure(
                            TimeoutException())
                    }
                    BleMsg.MSG_SET_MTU_RESULT -> {
                        mtuChangedMsgInit()
                        val mtuChangedCallback: BleMtuChangedCallback? =
                            msg.obj as BleMtuChangedCallback?
                        val bundle = msg.data
                        val status =
                            bundle.getInt(BleMsg.KEY_SET_MTU_BUNDLE_STATUS)
                        val value =
                            bundle.getInt(BleMsg.KEY_SET_MTU_BUNDLE_VALUE)
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            mtuChangedCallback?.onMtuChanged(value)
                        } else {
                            mtuChangedCallback?.onSetMTUFailure(GattException(
                            ))
                        }
                    }
                }
            }
        }
    }

    private fun withUUID(
        serviceUUID: UUID?,
        characteristicUUID: UUID?
    ): BleConnector? {
        if (serviceUUID != null && mBluetoothGatt != null) {
            mGattService = mBluetoothGatt!!.getService(serviceUUID)
        }
        if (mGattService != null && characteristicUUID != null) {
            mCharacteristic = mGattService!!.getCharacteristic(characteristicUUID)
        }
        return this
    }

    fun withUUIDString(
        serviceUUID: String?,
        characteristicUUID: String?
    ): BleConnector? {
        return withUUID(formUUID(serviceUUID), formUUID(characteristicUUID))
    }

    private fun formUUID(uuid: String?): UUID? {
        return if (uuid == null) null else UUID.fromString(uuid)
    }
    /*------------------------------- main operation ----------------------------------- */
    /**
     * notify
     */
    fun enableCharacteristicNotify(
        bleNotifyCallback: BleNotifyCallback?, uuid_notify: String,
        userCharacteristicDescriptor: Boolean
    ) {
        if (mCharacteristic != null
            && mCharacteristic!!.properties or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0
        ) {
            handleCharacteristicNotifyCallback(bleNotifyCallback, uuid_notify)
            setCharacteristicNotification(mBluetoothGatt,
                mCharacteristic,
                userCharacteristicDescriptor,
                true,
                bleNotifyCallback)
        } else {
            bleNotifyCallback?.onNotifyFailure(OtherException(
                "this characteristic not support notify!"))
        }
    }

    /**
     * stop notify
     */
    fun disableCharacteristicNotify(useCharacteristicDescriptor: Boolean): Boolean {
        return if (mCharacteristic != null
            && mCharacteristic!!.properties or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0
        ) {
            setCharacteristicNotification(mBluetoothGatt, mCharacteristic,
                useCharacteristicDescriptor, false, null)
        } else {
            false
        }
    }

    /**
     * notify setting
     */
    private fun setCharacteristicNotification(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        useCharacteristicDescriptor: Boolean,
        enable: Boolean,
        bleNotifyCallback: BleNotifyCallback?
    ): Boolean {
        if (gatt == null || characteristic == null) {
            notifyMsgInit()
            bleNotifyCallback?.onNotifyFailure(OtherException(
                "gatt or characteristic equal null"))
            return false
        }
        val success1 = gatt.setCharacteristicNotification(characteristic, enable)
        if (!success1) {
            notifyMsgInit()
            bleNotifyCallback?.onNotifyFailure(OtherException(
                "gatt setCharacteristicNotification fail"))
            return false
        }
        val descriptor: BluetoothGattDescriptor? = if (useCharacteristicDescriptor) {
            characteristic.getDescriptor(characteristic.uuid)
        } else {
            characteristic.getDescriptor(formUUID(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR))
        }
        return if (descriptor == null) {
            notifyMsgInit()
            bleNotifyCallback?.onNotifyFailure(OtherException(
                "descriptor equals null"))
            false
        } else {
            descriptor.value =
                if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            val success2 = gatt.writeDescriptor(descriptor)
            if (!success2) {
                notifyMsgInit()
                bleNotifyCallback?.onNotifyFailure(OtherException(
                    "gatt writeDescriptor fail"))
            }
            success2
        }
    }

    /**
     * indicate
     */
    fun enableCharacteristicIndicate(
        bleIndicateCallback: BleIndicateCallback?, uuid_indicate: String,
        useCharacteristicDescriptor: Boolean
    ) {
        if (mCharacteristic != null
            && mCharacteristic!!.properties or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0
        ) {
            handleCharacteristicIndicateCallback(bleIndicateCallback, uuid_indicate)
            setCharacteristicIndication(mBluetoothGatt, mCharacteristic,
                useCharacteristicDescriptor, true, bleIndicateCallback)
        } else {
            bleIndicateCallback?.onIndicateFailure(OtherException(
                "this characteristic not support indicate!"))
        }
    }

    /**
     * stop indicate
     */
    fun disableCharacteristicIndicate(userCharacteristicDescriptor: Boolean): Boolean {
        return if (mCharacteristic != null
            && mCharacteristic!!.properties or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0
        ) {
            setCharacteristicIndication(mBluetoothGatt, mCharacteristic,
                userCharacteristicDescriptor, false, null)
        } else {
            false
        }
    }

    /**
     * indicate setting
     */
    private fun setCharacteristicIndication(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        useCharacteristicDescriptor: Boolean,
        enable: Boolean,
        bleIndicateCallback: BleIndicateCallback?
    ): Boolean {
        if (gatt == null || characteristic == null) {
            indicateMsgInit()
            bleIndicateCallback?.onIndicateFailure(OtherException(
                "gatt or characteristic equal null"))
            return false
        }
        val success1 = gatt.setCharacteristicNotification(characteristic, enable)
        if (!success1) {
            indicateMsgInit()
            bleIndicateCallback?.onIndicateFailure(OtherException(
                "gatt setCharacteristicNotification fail"))
            return false
        }
        val descriptor: BluetoothGattDescriptor? = if (useCharacteristicDescriptor) {
            characteristic.getDescriptor(characteristic.uuid)
        } else {
            characteristic.getDescriptor(formUUID(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR))
        }
        return if (descriptor == null) {
            indicateMsgInit()
            bleIndicateCallback?.onIndicateFailure(OtherException(
                "descriptor equals null"))
            false
        } else {
            descriptor.value =
                if (enable) BluetoothGattDescriptor.ENABLE_INDICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            val success2 = gatt.writeDescriptor(descriptor)
            if (!success2) {
                indicateMsgInit()
                bleIndicateCallback?.onIndicateFailure(OtherException(
                    "gatt writeDescriptor fail"))
            }
            success2
        }
    }

    /**
     * write
     */
    fun writeCharacteristic(
        data: ByteArray?,
        bleWriteCallback: BleWriteCallback?,
        uuid_write: String
    ) {
        if (data == null || data.isEmpty()) {
            bleWriteCallback?.onWriteFailure(OtherException(
                "the data to be written is empty"))
            return
        }
        if (mCharacteristic == null
            || mCharacteristic!!.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0
        ) {
            bleWriteCallback?.onWriteFailure(OtherException(
                "this characteristic not support write!"))
            return
        }
        if (mCharacteristic!!.setValue(data)) {
            handleCharacteristicWriteCallback(bleWriteCallback, uuid_write)
            if (!mBluetoothGatt!!.writeCharacteristic(mCharacteristic)) {
                writeMsgInit()
                bleWriteCallback?.onWriteFailure(OtherException(
                    "gatt writeCharacteristic fail"))
            }
        } else {
            bleWriteCallback?.onWriteFailure(OtherException(
                "Updates the locally stored value of this characteristic fail"))
        }
    }

    /**
     * read
     */
    fun readCharacteristic(
        bleReadCallback: BleReadCallback?,
        uuid_read: String
    ) {
        if (mCharacteristic != null
            && mCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_READ > 0
        ) {
            handleCharacteristicReadCallback(bleReadCallback, uuid_read)
            if (!mBluetoothGatt!!.readCharacteristic(mCharacteristic)) {
                readMsgInit()
                bleReadCallback?.onReadFailure(OtherException(
                    "gatt readCharacteristic fail"))
            }
        } else {
            bleReadCallback?.onReadFailure(OtherException(
                "this characteristic not support read!"))
        }
    }

    /**
     * rssi
     */
    fun readRemoteRssi(bleRssiCallback: BleRssiCallback?) {
        handleRSSIReadCallback(bleRssiCallback)
        if (!mBluetoothGatt!!.readRemoteRssi()) {
            rssiMsgInit()
            bleRssiCallback?.onRssiFailure(OtherException(
                "gatt readRemoteRssi fail"))
        }
    }

    /**
     * set mtu
     */
    @SuppressLint("MissingPermission")
    fun setMtu(
        requiredMtu: Int,
        bleMtuChangedCallback: BleMtuChangedCallback?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            handleSetMtuCallback(bleMtuChangedCallback)
            if (!mBluetoothGatt!!.requestMtu(requiredMtu)) {
                mtuChangedMsgInit()
                bleMtuChangedCallback?.onSetMTUFailure(
                    OtherException(
                        "gatt requestMtu fail"))
            }
        } else {
            bleMtuChangedCallback?.onSetMTUFailure(OtherException(
                "API level lower than 21"))
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
    @SuppressLint("MissingPermission")
    fun requestConnectionPriority(connectionPriority: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothGatt!!.requestConnectionPriority(connectionPriority)
        } else false
    }

    /**************************************** Handle call back ******************************************/

    /**************************************** Handle call back  */
    /**
     * notify
     */
    private fun handleCharacteristicNotifyCallback(
        bleNotifyCallback: BleNotifyCallback?,
        uuid_notify: String
    ) {
        if (bleNotifyCallback != null)
            mBleBluetooth!!.addNotifyCallback(uuid_notify, bleNotifyCallback)
        notifyMsgInit()
        bleNotifyCallback?.key = uuid_notify
        bleNotifyCallback?.handler = mHandler

        mHandler!!.sendMessageDelayed(
            mHandler!!.obtainMessage(BleMsg.MSG_CHA_NOTIFY_START,
                bleNotifyCallback),
            BleManager.instance.operateTimeout.toLong())
    }

    /**
     * indicate
     */
    private fun handleCharacteristicIndicateCallback(
        bleIndicateCallback: BleIndicateCallback?,
        uuid_indicate: String
    ) {
        if (bleIndicateCallback != null)
            mBleBluetooth!!.addIndicateCallback(uuid_indicate, bleIndicateCallback)
        indicateMsgInit()
        bleIndicateCallback?.key = uuid_indicate
        bleIndicateCallback?.handler = mHandler

        mHandler!!.sendMessageDelayed(
            mHandler!!.obtainMessage(BleMsg.MSG_CHA_INDICATE_START,
                bleIndicateCallback),
            BleManager.instance.operateTimeout.toLong())
    }

    /**
     * write
     */
    private fun handleCharacteristicWriteCallback(
        bleWriteCallback: BleWriteCallback?,
        uuid_write: String
    ) {
        if (bleWriteCallback != null)
            mBleBluetooth!!.addWriteCallback(uuid_write, bleWriteCallback)
        writeMsgInit()
        bleWriteCallback?.key = uuid_write
        bleWriteCallback?.handler = mHandler

        mHandler!!.sendMessageDelayed(
            mHandler!!.obtainMessage(BleMsg.MSG_CHA_WRITE_START,
                bleWriteCallback),
            BleManager.instance.operateTimeout.toLong())
    }

    /**
     * read
     */
    private fun handleCharacteristicReadCallback(
        bleReadCallback: BleReadCallback?,
        uuid_read: String
    ) {
        if (bleReadCallback != null)
            mBleBluetooth!!.addReadCallback(uuid_read, bleReadCallback)
        readMsgInit()
        bleReadCallback?.key = uuid_read
        bleReadCallback?.handler = mHandler

        mHandler!!.sendMessageDelayed(
            mHandler!!.obtainMessage(BleMsg.MSG_CHA_READ_START,
                bleReadCallback),
            BleManager.instance.operateTimeout.toLong())
    }

    /**
     * rssi
     */
    private fun handleRSSIReadCallback(bleRssiCallback: BleRssiCallback?) {
        if (bleRssiCallback != null) mBleBluetooth!!.addRssiCallback(bleRssiCallback)
        rssiMsgInit()
        bleRssiCallback?.handler = mHandler
        mHandler!!.sendMessageDelayed(
            mHandler!!.obtainMessage(BleMsg.MSG_READ_RSSI_START,
                bleRssiCallback),
            BleManager.instance.operateTimeout.toLong())
    }

    /**
     * set mtu
     */
    private fun handleSetMtuCallback(bleMtuChangedCallback: BleMtuChangedCallback?) {
        if (bleMtuChangedCallback != null)
            mBleBluetooth!!.addMtuChangedCallback(bleMtuChangedCallback)
        mtuChangedMsgInit()
        bleMtuChangedCallback?.handler = mHandler

        mHandler!!.sendMessageDelayed(
            mHandler!!.obtainMessage(BleMsg.MSG_SET_MTU_START,
                bleMtuChangedCallback),
            BleManager.instance.operateTimeout.toLong())
    }

    fun notifyMsgInit() {
        mHandler!!.removeMessages(BleMsg.MSG_CHA_NOTIFY_START)
    }

    fun indicateMsgInit() {
        mHandler!!.removeMessages(BleMsg.MSG_CHA_INDICATE_START)
    }

    fun writeMsgInit() {
        mHandler!!.removeMessages(BleMsg.MSG_CHA_WRITE_START)
    }

    fun readMsgInit() {
        mHandler!!.removeMessages(BleMsg.MSG_CHA_READ_START)
    }

    fun rssiMsgInit() {
        mHandler!!.removeMessages(BleMsg.MSG_READ_RSSI_START)
    }

    fun mtuChangedMsgInit() {
        mHandler!!.removeMessages(BleMsg.MSG_SET_MTU_START)
    }
}