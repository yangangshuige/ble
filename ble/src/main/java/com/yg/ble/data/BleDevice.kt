package com.yg.ble.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Parcel
import android.os.Parcelable

@SuppressLint("MissingPermission")
class BleDevice(
    device: BluetoothDevice?,
    rssi: Int = 0,
    scanRecord: ByteArray = byteArrayOf(),
    timestampNanos: Long = 0
) : Parcelable {
    private var mDevice: BluetoothDevice? = device
    private var mScanRecord: ByteArray = scanRecord
    private var mRssi = rssi
    private var mTimestampNanos: Long = timestampNanos

    constructor(parcel: Parcel) : this(
        TODO("device"),
        TODO("rssi"),
        TODO("scanRecord"),
        TODO("timestampNanos")) {
        mDevice = parcel.readParcelable(BluetoothDevice::class.java.classLoader)
        mScanRecord = parcel.createByteArray()!!
        mRssi = parcel.readInt()
        mTimestampNanos = parcel.readLong()
    }

    fun getDevice(): BluetoothDevice? {
        return mDevice
    }

    fun getMac(): String {
        return if (mDevice == null) {
            ""
        } else {
            mDevice!!.address ?: ""
        }
    }

    fun getName(): String {
        return return if (mDevice == null) {
            ""
        } else {
            mDevice!!.name ?: ""
        }
    }

    fun getRssi(): Int {
        return mRssi
    }

    fun getScanRecord(): ByteArray {
        return mScanRecord
    }

    fun getKey(): String {
        return if (mDevice != null) {
            mDevice!!.name + mDevice!!.address
        } else ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(mDevice, flags)
        parcel.writeByteArray(mScanRecord)
        parcel.writeInt(mRssi)
        parcel.writeLong(mTimestampNanos)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BleDevice> {
        override fun createFromParcel(parcel: Parcel): BleDevice {
            return BleDevice(parcel)
        }

        override fun newArray(size: Int): Array<BleDevice?> {
            return arrayOfNulls(size)
        }
    }
}