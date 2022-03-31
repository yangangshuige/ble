package com.example.test.biz.pages.device

import android.app.Application
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import com.didi.bike.applicationholder.AppContextHolder
import com.example.test.R
import com.example.test.biz.bean.CmdParams
import com.example.test.biz.bean.ConnectState
import com.example.test.biz.bean.RemoteCommand
import com.example.test.biz.bean.ResourceState
import com.example.test.biz.viewmodel.ConnectViewModel
import com.example.test.databinding.FragmentDeviceBinding
import com.example.test.utils.PhoneUtils
import com.yg.ble.data.BleDevice
import java.util.*

class DeviceFragment : Fragment(R.layout.fragment_device) {
    private lateinit var viewBinding: FragmentDeviceBinding
    private var bleDevice: BleDevice? = null
    private val connectViewModel: ConnectViewModel by processViewModels()
    private val connectStatus = Observer<ConnectState<BleDevice>> { connectState ->
        run {
            when (connectState) {
                is ConnectState.ConnectStart -> {
                    viewBinding.tvStatus.text = "开始连接..."
                }
                is ConnectState.ConnectSuccess -> {
                    viewBinding.tvStatus.text = "连接成功"
                }
                is ConnectState.ConnectFail -> {
                    viewBinding.tvStatus.text = "连接失败"
                }
                is ConnectState.DisConnected -> {
                    viewBinding.tvStatus.text = "连接断开"
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding = FragmentDeviceBinding.bind(view)
        bleDevice = arguments?.getParcelable(KEY_DEVICE_DATA)
        viewBinding.tvMac.text = bleDevice?.getMac()
        viewBinding.tvStatus.text = "连接成功"
        connectViewModel.connectLiveData.observeUnsticky(viewLifecycleOwner, connectStatus)
        connectViewModel.commandExecuteStateLiveData(RemoteCommand.CMD_SWITCH)
            .observeUnsticky(viewLifecycleOwner) { state ->
                run {
                    when (state) {
                        is ResourceState.Loading -> {
                            showLoading("请稍后")
                        }
                        is ResourceState.Success -> {
                            hideLoading()
                            val lockeState = connectViewModel.lockState.value ?: false
                            showToast(if (lockeState) "开锁成功" else "关锁成功")
                        }
                        is ResourceState.Error -> {
                            hideLoading()
                            val lockeState = connectViewModel.lockState.value ?: false
                            showToast(if (lockeState) "关锁失败" else "开锁失败")
                        }
                    }
                }
            }
        connectViewModel.lockState.observeUnsticky(viewLifecycleOwner) { state ->
            run {
                viewBinding.btnLock.text = if (state) "关锁" else "开锁"
            }
        }
        connectViewModel.securityState.observeUnsticky(viewLifecycleOwner) { state ->
            run {
                viewBinding.btnSecurity.text = if (state) "解防" else "设防"
            }
        }
        viewBinding.btnLock.setOnClickListener {
            val lockeState = connectViewModel.lockState.value ?: false
            val paramSetJson = RemoteCommand.deviceActions()
                ?.getTargetJson(RemoteCommand.CMD_SWITCH)
            val params = RemoteCommand.CmdParamsBuilder(paramSetJson!!)
                .addArgument("ACC", if (lockeState) 1 else 0)
                .buildJsonParams()
            connectViewModel.executeCommand(CmdParams("", RemoteCommand.CMD_SWITCH, params))
        }
//        viewBinding.btnCloseLock.setOnClickListener {
//            val paramSetJson = RemoteCommand.deviceActions()
//                ?.getTargetJson(RemoteCommand.CMD_SWITCH)
//            val params = RemoteCommand.CmdParamsBuilder(paramSetJson!!)
//                .addArgument("ACC", 1)
//                .buildJsonParams()
//            connectViewModel.executeCommand(CmdParams("", RemoteCommand.CMD_SWITCH, params))
//        }
        viewBinding.btnSecurity.setOnClickListener {
            val securityState = connectViewModel.securityState.value ?: false
            val paramSetJson = RemoteCommand.deviceActions()
                ?.getTargetJson(RemoteCommand.CMD_ANFANG)
            val params = RemoteCommand.CmdParamsBuilder(paramSetJson!!)
                .addArgument("SecurityState",  if (securityState) 0 else 1)
                .buildJsonParams()
            connectViewModel.executeCommand(CmdParams("", RemoteCommand.CMD_ANFANG, params))
        }
        viewBinding.btnFindCar.setOnClickListener {
            val paramSetJson = RemoteCommand.deviceActions()
                ?.getTargetJson(RemoteCommand.CMD_WHISTLE)
            val params = RemoteCommand.CmdParamsBuilder(paramSetJson!!)
                .buildJsonParams()
            connectViewModel.executeCommand(CmdParams("", RemoteCommand.CMD_WHISTLE, params))
        }
        viewBinding.btnPair.setOnClickListener {

            val paramsJson = RemoteCommand.deviceActions()
                ?.getTargetJson(RemoteCommand.CMD_BLUETOOTH_KEY)

            val params = RemoteCommand.CmdParamsBuilder(paramsJson!!)
                .addArgument("PhoneUUID", PhoneUtils.bluetoothDeviceId())
                .addArgument("BlePairingCode", "000000")
                .addArgument("BlePT", "60")
                .buildJsonParams()
//            connectViewModel.sendCommand(params)
            connectViewModel.executeCommand(CmdParams("", RemoteCommand.CMD_BLUETOOTH_KEY, params))
        }
        viewBinding.btnUnpair.setOnClickListener {
            val paramSetJson = RemoteCommand.deviceActions()
                ?.getTargetJson(RemoteCommand.CMD_DELETE_BLUETOOTH_KEY)

            val boundList = connectViewModel.phoneFeatureList
            val deviceId = PhoneUtils.bluetoothDeviceId()

            var bondIndex: Int = -1
            for (boundItem in boundList) {
                if (Objects.equals(boundItem.phoneUUID, deviceId)) {
                    bondIndex = boundItem.phoneFeatureIndex
                    break
                }
            }

            val params = RemoteCommand.CmdParamsBuilder(paramSetJson!!)
                .addArgument("PhoneFeatureIndex", bondIndex)
                .buildJsonParams()

//            connectViewModel.sendCommand(params)
            connectViewModel.executeCommand(CmdParams("",
                RemoteCommand.CMD_DELETE_BLUETOOTH_KEY,
                params))
        }
        viewBinding.btnUpdate.setOnClickListener {
//            val paramSetJson = RemoteCommand.deviceActions()
//                ?.getTargetJson(RemoteCommand.CMD_WHISTLE)
//            val params = RemoteCommand.CmdParamsBuilder(paramSetJson!!)
//                .buildJsonParams()
//            connectViewModel.sendCommand(params)
        }

    }

    companion object {
        private const val KEY_DEVICE_DATA = "KEY_DEVICE_DATA"

        @JvmStatic
        fun buildArguments(
            bleDevice: BleDevice,
        ): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(KEY_DEVICE_DATA, bleDevice)
            return bundle
        }

    }
}