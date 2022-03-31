package com.yg.ble.scan

import com.yg.ble.BleManager
import java.util.*
import kotlin.collections.ArrayList

class BleScanRuleConfig {
    var mServiceUuids = arrayListOf<UUID>()
    var mDeviceNames = arrayListOf<String>()
    var mDeviceMac: String = ""
    var mAutoConnect = false

    /**
     * name 模糊查询
     */
    var mFuzzy = true
    var mScanTimeOut: Long = BleManager.DEFAULT_SCAN_TIME.toLong()

    class Builder {
        private var mServiceUuids = arrayListOf<UUID>()
        private var mDeviceNames = arrayListOf<String>()
        private var mDeviceMac: String = ""
        private var mAutoConnect = false
        private var mFuzzy = false
        private var mScanTimeOut: Long = BleManager.DEFAULT_SCAN_TIME.toLong()
        fun setServiceUuids(uuids: ArrayList<UUID>): Builder {
            mServiceUuids = uuids
            return this
        }

        fun setDeviceName(fuzzy: Boolean, name: ArrayList<String>): Builder {
            mFuzzy = fuzzy
            mDeviceNames = name
            return this
        }

        fun setDeviceMac(mac: String): Builder {
            mDeviceMac = mac
            return this
        }

        fun setAutoConnect(autoConnect: Boolean): Builder {
            mAutoConnect = autoConnect
            return this
        }

        fun setScanTimeOut(timeOut: Long): Builder {
            mScanTimeOut = timeOut
            return this
        }

        private fun applyConfig(config: BleScanRuleConfig) {
            config.mServiceUuids = mServiceUuids
            config.mDeviceNames = mDeviceNames
            config.mDeviceMac = mDeviceMac
            config.mAutoConnect = mAutoConnect
            config.mFuzzy = mFuzzy
            config.mScanTimeOut = mScanTimeOut
        }

        fun build(): BleScanRuleConfig {
            val config = BleScanRuleConfig()
            applyConfig(config)
            return config
        }
    }
}