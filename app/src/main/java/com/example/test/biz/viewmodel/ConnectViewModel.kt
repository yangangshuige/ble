package com.example.test.biz.viewmodel

import android.bluetooth.BluetoothGatt
import android.text.TextUtils
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.PolLiveData
import com.alibaba.fastjson.JSON
import com.example.test.biz.bean.*
import com.example.test.biz.bean.PolarisBleAlarm.EventAck
import com.example.test.biz.bean.PolarisBleAlarm.Msg
import com.example.test.utils.AppExecutors
import com.example.test.utils.CodecUtil
import com.example.test.utils.JsonUtils
import com.example.test.utils.pb.ActionName
import com.example.test.utils.pb.BleAlarmProtoBufUtil
import com.example.test.utils.pb.PbString
import com.google.gson.JsonObject
import com.yg.ble.BleManager
import com.yg.ble.callback.*
import com.yg.ble.data.BleDevice
import com.yg.ble.exception.BleException
import com.yg.ble.scan.BleScanRuleConfig
import com.yg.ble.utils.HexUtil
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class ConnectViewModel : ProcessViewModel() {
    private val scanDeviceState = PolLiveData<ScanState<BleDevice>>()
    val scanDeviceLiveData: PolLiveData<ScanState<BleDevice>> = scanDeviceState
    private val connectState = PolLiveData<ConnectState<BleDevice>>()
    val connectLiveData: PolLiveData<ConnectState<BleDevice>> = connectState
    private var mConnectDevice: BleDevice? = null
    var phoneFeatureList = arrayListOf<PhoneFeature>()

    /** 防止指令重复执行 */
    private val commandExecuteState = HashMap<String, AtomicBoolean>()

    /** 指令执行的状态 */
    private val commandExecuteResults =
        HashMap<String, PolLiveData<ResourceState<Any>>>()

    /** 超时任务 */
    private val timeOutRunnable = HashMap<String, TimeOutRunnable>()

    val lockState = PolLiveData<Boolean>()
    val securityState = PolLiveData<Boolean>()
    private val bleGattCallback = object : BleGattCallback() {
        override fun onStartConnect() {
            connectState.postValue(ConnectState.connectStart())
        }

        override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            connectState.postValue(ConnectState.connectFail(bleDevice))
        }

        override fun onConnectSuccess(
            bleDevice: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int,
        ) {
            //  连接状态通知
            connectState.postValue(ConnectState.connectSuccess(bleDevice))
            mConnectDevice = bleDevice
            //  延迟2秒开启通知
            AppExecutors.instance.executeDelayedOnMainExecutor(notifyRunnable, 2000)
            //  延迟4秒设置Mtu
            AppExecutors.instance.executeDelayedOnMainExecutor(mtuRunnable, 4000)

            //  取消心跳
            AppExecutors.instance.getMainHandler().removeCallbacks(heartRunnable)
            //取消重连
            AppExecutors.instance.getMainHandler().removeCallbacks(reConnectRunnable)
            //  延迟6秒同步车辆时间
            AppExecutors.instance.executeDelayedOnMainExecutor(deviceRTCRunnable, 6 * 1000)
            //  延迟8秒获取车辆信息
            AppExecutors.instance.executeDelayedOnMainExecutor(deviceRunnable, 8 * 1000)
            //  延迟秒10秒开启心跳
            AppExecutors.instance.executeDelayedOnMainExecutor(heartRunnable, 10 * 1000)
        }

        override fun onDisConnected(
            isActiveDisConnected: Boolean,
            device: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int,
        ) {
            connectState.postValue(ConnectState.disConnected(device))
        }

    }

    fun connectDevice(bleDevice: BleDevice?) {
        BleManager.instance.connect(bleDevice, bleGattCallback)
    }

    private val bleScanCallback = object : BleScanCallback() {

        override fun onScanStarted(success: Boolean) {
            Log.d(TAG, "onScanStarted==========$success")
            scanDeviceState.postValue(ScanState.scanStart())

        }

        override fun onScanning(bleDevice: BleDevice) {
            Log.d(TAG, "onScanning==========${bleDevice?.getKey()}")
            scanDeviceState.postValue(ScanState.scanning(bleDevice))
        }

        override fun onScanFinished(scanResultList: List<BleDevice>) {
            Log.d(TAG, "onScanFinished==========${scanResultList.size}")
            scanDeviceState.postValue(ScanState.scanFinish())
        }
    }

    fun startScan() {
        BleManager.instance.scan(bleScanCallback)
    }

    fun stopScan() {
        BleManager.instance.cancelScan()
    }

    fun setScanRule(
        uuidsStr: String,
        nameStr: String,
        mac: String,
        isAutoConnect: Boolean,
    ) {
        val uuids = if (TextUtils.isEmpty(uuidsStr)) {
            arrayListOf()
        } else {
            uuidsStr.split(",".toRegex())
        }
        var serviceUuids = arrayListOf<UUID>()
        for (item in uuids) {
            serviceUuids.add(UUID.fromString(item))
        }
        val names = if (TextUtils.isEmpty(nameStr)) {
            arrayListOf()
        } else {
            nameStr.split(",".toRegex())
        }
        val nameArray = arrayListOf<String>()
        for (name in names) {
            nameArray.add(name)
        }
        val scanRuleConfig: BleScanRuleConfig = BleScanRuleConfig.Builder()
            .setServiceUuids(serviceUuids) // 只扫描指定的服务的设备，可选
            .setDeviceName(true, nameArray) // 只扫描指定广播名的设备，可选
            .setDeviceMac(mac) // 只扫描指定mac的设备，可选
            .setAutoConnect(isAutoConnect) // 连接时的autoConnect参数，可选，默认false
            .setScanTimeOut(30000) // 扫描超时时间，可选，默认10秒
            .build()
        BleManager.instance.initScanRule(scanRuleConfig)
    }

    private var notifyState = false
    private val notifyCallback = object : BleNotifyCallback() {
        override fun onNotifySuccess() {
            Log.d(TAG, "onNotifySuccess========")
            notifyState = true
            AppExecutors.instance.getMainHandler().removeCallbacks(notifyRunnable)
        }

        override fun onNotifyFailure(exception: BleException?) {
            notifyState = false
            AppExecutors.instance.executeDelayedOnMainExecutor(notifyRunnable, 2000)
            Log.d(TAG, "onNotifyFailure========${exception?.description}")
        }

        override fun onCharacteristicChanged(data: ByteArray?) {
            Log.d(TAG, "onCharacteristicChanged========${HexUtil.formatHexString(data)}")
            parseReceiverData(data!!)

        }

    }
    private val notifyRunnable = Runnable {
        enableNotify()
    }

    private fun enableNotify() {
        BleManager.instance.notify(mConnectDevice, SERVICE_UUID, NOTIFY_UUID, false, notifyCallback)
    }

    private var mtuState = false
    private val bleMtuChangedCallback = object : BleMtuChangedCallback() {
        override fun onSetMTUFailure(exception: BleException?) {
            if (!mtuState)
                AppExecutors.instance.executeDelayedOnMainExecutor(mtuRunnable, 2000)
        }

        override fun onMtuChanged(mtu: Int) {
            mtuState = true
            AppExecutors.instance.getMainHandler().removeCallbacks(mtuRunnable)
            Log.d(TAG, "onMtuChanged========$mtu")
        }

    }
    private val mtuRunnable = Runnable { setMtu() }
    private fun setMtu() {
        Log.d(TAG, "setMtu====")
        BleManager.instance.setMtu(mConnectDevice, 240, bleMtuChangedCallback)
    }

    private val writeCallback = object : BleWriteCallback() {
        override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
            Log.d(TAG, "onWriteSuccess========${HexUtil.formatHexString(justWrite)}")
        }

        override fun onWriteFailure(exception: BleException) {
            Log.d(TAG, "onWriteFailure========${exception?.description}")
        }

    }

    fun write(data: ByteArray) {
        BleManager.instance
            .write(mConnectDevice,
                SERVICE_UUID,
                WRITE_UUID,
                data,
                !mtuState,
                false,
                0L,
                writeCallback)
    }

    private val reConnectRunnable = Runnable {
        Log.d(TAG, "reConnectRunnable====")
        reConnect()

    }

    private fun reConnect() {
        BleManager.instance.disconnectAllDevice()
        BleManager.instance.connect(mConnectDevice, bleGattCallback)
    }

    private val heartRunnable = Runnable {
        heartCommand()
    }

    private fun heartCommand() {
        Log.d(TAG, "heartRunnable====")

        val paramSetJson = RemoteCommand.deviceActions()
            ?.getTargetJson(RemoteCommand.CMD_HEART)
        val params = RemoteCommand.CmdParamsBuilder(paramSetJson!!)
            .buildJsonParams()
//        sendCommand(params)
        executeCommand(CmdParams("", RemoteCommand.CMD_HEART, params))
        AppExecutors.instance.executeDelayedOnMainExecutor(heartRunnable, 30 * 1000)
    }

    /**
     * 连接成功获取设备信息
     */

    private val deviceRunnable = Runnable {
        getDeviceCommand()
    }
    private val deviceRTCRunnable = Runnable {
        deviceRtcCommand()
    }

    private fun getDeviceCommand() {
        Log.d(TAG, "getDeviceRunnable====")
        // 获取车辆信息
        val paramSetJson = RemoteCommand.deviceActions()
            ?.getTargetJson(RemoteCommand.CMD_GET_DEVICE)
        val params = RemoteCommand.CmdParamsBuilder(paramSetJson!!)
            .buildJsonParams()
//        sendCommand(params)
        executeCommand(CmdParams("", RemoteCommand.CMD_GET_DEVICE, params))
    }

    private fun deviceRtcCommand() {
        Log.d(TAG, "deviceRtcCommand====")
        // 同步车辆时间
        val paramsJson = RemoteCommand.deviceActions()
            ?.getTargetJson(RemoteCommand.CMD_SET_RTC)
        val params = RemoteCommand.CmdParamsBuilder(paramsJson!!)
            .addArgument("RTC", System.currentTimeMillis())
            .buildJsonParams()
//        sendCommand(params)
        executeCommand(CmdParams("", RemoteCommand.CMD_SET_RTC, params))
    }

    fun sendCommand(params: JsonObject) {
        val buff = BleAlarmProtoBufUtil.encrypt(params.toString())
        write(CodecUtil.encrypt(buff))
    }

    fun parseReceiverData(receiver: ByteArray) {
        try {
            val buff = CodecUtil.decrypt(receiver)
            val pbJson = JSON.parseObject(BleAlarmProtoBufUtil.decrypt(buff))
            val repSeqId: Int = pbJson.getInteger("ReplySeqId")
            val bleReceiverData =
                JsonUtils.decode(pbJson.toJSONString(), BleReceiverData::class.java)
            Log.d(TAG, "bleReceiverData========${bleReceiverData?.toString()}")
            Log.d(TAG, "pbJson========${pbJson}")
            //NOTE  判断该action是否需要Ack
//            val actionName = pbJson.getString("Name")
            val actionName = bleReceiverData?.name
            if (BleAlarmProtoBufUtil.actionNeedToAck.contains(actionName)) {
                val dataJson = pbJson.getJSONObject("Data")
                Log.d(TAG, "dataJson========${dataJson}")
                if (actionName == "ACCChange") {
                } else if (actionName == "SecurityChange") {
                } else if (actionName == "BatteryLockChange") {
                } else if (actionName == "PowerBatteryEvent") {
                } else if (actionName == "BlePairingFinish") {
                    //{"PbVersion":"0.01","ReplySeqId":10005,"Data":
                    // {"TriggerType":"ActionTrigger","PhoneFeature":
                    // [{"PhoneFeatureIndex":0,"PhoneUUID":"3dba1fcf25bed0f4","PhoneMac":"00:00:00:00:00:00"}],
                    // "GT":"1647942841000","BlePairingResult":"PairingSuccess","TrackId":10005},
                    // "Time":"1647942841000","SeqId":15,
                    // "Name":"BlePairingFinish"}
                    if (dataJson.getString("BlePairingResult") == "PairingSuccess") {
                        // 配对成功
                        phoneFeatureList.clear()
                        val phoneFeature = pbJson.getJSONArray("PhoneFeature")
                        val arrayList = JsonUtils.decodeArray(phoneFeature.toJSONString(),
                            PhoneFeature::class.java)
                        for (item in arrayList) {
                            phoneFeatureList.add(item)
                        }
                    }
                } else if (actionName == "BikeWarningEvent") {
                    //NOTE  记录事件
                }

                //NOTE  下发ACK
                val eventAckBuilder = EventAck.newBuilder()
                eventAckBuilder.state = PolarisBleAlarm.StateStruct.Ok
                eventAckBuilder.trackId = dataJson.getInteger("TrackId")
                val msgBuilder = Msg.newBuilder()
                msgBuilder.seqId = repSeqId
                msgBuilder.time = System.currentTimeMillis()
                msgBuilder.pbVersion = "1.0"
                msgBuilder.replySeqId = pbJson.getInteger("SeqId")
                msgBuilder.data = eventAckBuilder.build().toByteString()
                msgBuilder.name = Msg.NameStruct.EventAck
                write(CodecUtil.encrypt(msgBuilder.build().toByteArray()))
            } else {
                val serial = String.format("BleAlarm_%d", repSeqId)
                val dataJson = pbJson.getJSONObject("Data")
                val inputJson = dataJson.getJSONObject("Input")
                val outputJson = dataJson.getJSONObject("Output")
                var cmdResult: Int
                when (actionName) {
                    ActionName.ACCSet.name -> {
                        cmdResult =
                            if (outputJson.getString(PbString.ACC_SET_RESULT) == "ACCOpenSuccess" || outputJson.getString(
                                    "ACCSetResult") == "ACCOpenAlready"
                            ) {
                                lockState.postValue(true)
                                AppExecutors.instance.getMainHandler()
                                    .removeCallbacks(cmdTimeOutRunnable(RemoteCommand.CMD_SWITCH))
                                commandExecuteStateLiveData(RemoteCommand.CMD_SWITCH).postValue(
                                    ResourceState.success(""))
                                Log.d(TAG, "ACCOpen")
                            } else if (outputJson.getString("ACCSetResult") == "ACCCloseSuccess" || outputJson.getString(
                                    "ACCSetResult") == "ACCCloseAlready"
                            ) {
                                lockState.postValue(false)
                                AppExecutors.instance.getMainHandler()
                                    .removeCallbacks(cmdTimeOutRunnable(RemoteCommand.CMD_SWITCH))
                                commandExecuteStateLiveData(RemoteCommand.CMD_SWITCH).postValue(
                                    ResourceState.success(""))
                                Log.d(TAG, "ACCClose")
                            } else {
                                AppExecutors.instance.getMainHandler()
                                    .removeCallbacks(cmdTimeOutRunnable(RemoteCommand.CMD_SWITCH))
                                commandExecuteStateLiveData(RemoteCommand.CMD_SWITCH).postValue(
                                    ResourceState.error(null, -1))
                                Log.d(TAG, "操作失败")
                            }
                        commandState(RemoteCommand.CMD_SWITCH).set(false)
                    }
                    "SecuritySet" -> {
                        cmdResult =
                            if (outputJson.getString("SSResult") == "SSetOpenSuccess" || outputJson.getString(
                                    "SSResult") == "SSetOpenAlready"
                            ) {
                                securityState.postValue(true)
                                AppExecutors.instance.getMainHandler()
                                    .removeCallbacks(cmdTimeOutRunnable(RemoteCommand.CMD_ANFANG))
                                commandExecuteStateLiveData(RemoteCommand.CMD_ANFANG).postValue(
                                    ResourceState.success(""))
                                Log.d(TAG, "SSEnable")
                            } else if (outputJson.getString("SSResult") == "SSetCloseSuccess" || outputJson.getString(
                                    "SSResult") == "SSetCloseAlready"
                            ) {
                                securityState.postValue(false)
                                AppExecutors.instance.getMainHandler()
                                    .removeCallbacks(cmdTimeOutRunnable(RemoteCommand.CMD_ANFANG))
                                commandExecuteStateLiveData(RemoteCommand.CMD_ANFANG).postValue(
                                    ResourceState.success(""))
                                Log.d(TAG, "SSDisable")
                            } else {
                                AppExecutors.instance.getMainHandler()
                                    .removeCallbacks(cmdTimeOutRunnable(RemoteCommand.CMD_ANFANG))
                                commandExecuteStateLiveData(RemoteCommand.CMD_ANFANG).postValue(
                                    ResourceState.error(null, -1))
                                Log.d(TAG, "操作失败")
                            }
                        commandState(RemoteCommand.CMD_ANFANG).set(false)
                    }
                    "BlePairingCodeSet" -> {
                        cmdResult =
                            if (outputJson.getString("BlePairingCodeSetResult") == "SetCodeSuccess") {
                                Log.d(TAG, "SetCodeSuccess")
                            } else {
                                Log.d(TAG, "操作失败")
                            }
                    }
                    "HDGet", "HBGet", "ParamGet" -> {
                        lockState.postValue("ACCOpen" == bleReceiverData.data.output.aCC)
                        Log.d(TAG, "HDGet=====HBGet=====ParamGet")
                    }
                    "GetDeviceInfo" -> {
                        Log.d(TAG, "GetDeviceInfo===========")
                    }
                    "BatteryGet" -> {
                        val oneLineBatJson = outputJson.getJSONObject("OneLineBat")
                        Log.d(TAG, "BatteryGet========oneLineBatJson========$oneLineBatJson")
                    }
                    "DeletePhoneFeature" -> {
                        if (outputJson.getString("DPFR") == "DeleteSuccess") {
                            Log.d(TAG, "DeletePhoneFeature=====成功")
                            AppExecutors.instance.getMainHandler()
                                .removeCallbacks(cmdTimeOutRunnable(RemoteCommand.CMD_DELETE_BLUETOOTH_KEY))
                            commandExecuteStateLiveData(RemoteCommand.CMD_DELETE_BLUETOOTH_KEY).postValue(
                                ResourceState.success(""))
                            BleManager.instance.removeBondWithDevice(mConnectDevice?.getDevice()!!)
                        } else {
                            AppExecutors.instance.getMainHandler()
                                .removeCallbacks(cmdTimeOutRunnable(RemoteCommand.CMD_DELETE_BLUETOOTH_KEY))
                            commandExecuteStateLiveData(RemoteCommand.CMD_DELETE_BLUETOOTH_KEY).postValue(
                                ResourceState.error("", -1))
                            Log.d(TAG, "操作失败")
                        }
                        commandState(RemoteCommand.CMD_DELETE_BLUETOOTH_KEY).set(false)
                    }
                    "UpdateByBle" -> {
                        cmdResult =
                            if (outputJson.getString("UpdateActionResult") == "StartUpdate") {
                                Log.d(TAG, "UpdateByBle=====成功")
                            } else {
                                Log.d(TAG, "UpdateByBle=====失败")
                            }
                    }
                    "WriteFirmData" -> {
                        cmdResult =
                            if (outputJson.getString("WriteFirmDataResult") == "WriteDataSuccess") {
                                Log.d(TAG, "WriteFirmData=====成功")
                            } else {
                                Log.d(TAG, "WriteFirmData=====失败")
                            }
                    }
                    "HDSet", "DeviceRestart", "DeviceRTCSet", "BatteryLockSet", "BikeReFactory", "FindBike" -> {
                        Log.d(TAG, "HDSet=====成功")
                    }
                    else -> {
                        Log.d(TAG, "=====操作失败")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun commandExecuteStateLiveData(command: String): PolLiveData<ResourceState<Any>> {
        var state = commandExecuteResults[command]
        if (null == state) {
            state = PolLiveData()
            commandExecuteResults[command] = state
        }

        return state
    }

    @Synchronized
    private fun commandState(command: String): AtomicBoolean {
        var state = commandExecuteState[command]
        if (null == state) {
            state = AtomicBoolean(false)
            commandExecuteState[command] = state
        }

        return state
    }

    @MainThread
    fun executeCommand(cmdParams: CmdParams) {
        val command = cmdParams.command
        // 指令还未执行结束，不再重复执行
        val executeState = commandState(command)
        if (!executeState.compareAndSet(false, true)) return
        val executeResult = commandExecuteStateLiveData(command)
        executeResult.postValue(ResourceState.loading())
        val buff = BleAlarmProtoBufUtil.encrypt(cmdParams.params.toString())
        write(CodecUtil.encrypt(buff))
        AppExecutors.instance.executeDelayedOnMainExecutor(cmdTimeOutRunnable(command), 5000)
    }

    @Synchronized
    private fun cmdTimeOutRunnable(command: String): TimeOutRunnable {
        var runnable = timeOutRunnable[command]
        if (null == runnable) {
            runnable = TimeOutRunnable(commandExecuteStateLiveData(command), commandState(command))
            timeOutRunnable[command] = runnable
        }

        return runnable
    }

    class TimeOutRunnable(
        private val executeResult: PolLiveData<ResourceState<Any>>,
        private val commandState: AtomicBoolean,
    ) : Runnable {
        override fun run() {
            executeResult.postValue(ResourceState.error(null, -1, "操作超时"))
            commandState.set(false)
            AppExecutors.instance.getMainHandler().removeCallbacks(this)
        }

    }

    companion object {
        private val TAG = ConnectViewModel::class.java.simpleName
        private const val SERVICE_UUID = "0000fee7-0000-1000-8000-00805f9b34fb"
        private const val WRITE_UUID = "000036f1-0000-1000-8000-00805f9b34fb"
        private const val NOTIFY_UUID = "000036f2-0000-1000-8000-00805f9b34fb"
    }
}