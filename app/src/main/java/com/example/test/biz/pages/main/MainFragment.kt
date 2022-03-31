package com.example.test.biz.pages.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.*
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.biz.bean.ConnectState
import com.example.test.biz.bean.ScanState
import com.example.test.biz.pages.device.DeviceFragment
import com.example.test.biz.viewmodel.ConnectViewModel
import com.example.test.databinding.FragmentMainBinding
import com.example.test.recyclerView.LineSeparatorDecoration
import com.yg.ble.BleManager
import com.yg.ble.data.BleDevice
import java.util.*

class MainFragment : Fragment() {
    private lateinit var viewBinding: FragmentMainBinding

    private val hasLocationPermission: Boolean
        get() {
            return ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    private val operatingAnim: Animation
        get() {
            return AnimationUtils.loadAnimation(requireContext(), R.anim.rotate)
        }
    private val lineSeparatorDecoration: LineSeparatorDecoration by lazy {
        LineSeparatorDecoration.Builder()
            .separatorOrientation(RecyclerView.HORIZONTAL)
            .separatorColor(R.color.plr_line_color)
            .separatorSizeInDp(1f)
            .build()
    }
    private val connectViewModel: ConnectViewModel by processViewModels()

    private val actionClick = object : DeviceAdapter.ActionClick {
        override fun connectAction(bleDevice: BleDevice) {
            if (!BleManager.instance.isConnected(bleDevice)) {
                connectViewModel.stopScan()
                connectViewModel.connectDevice(bleDevice)
            }
        }

        override fun disConnectAction(bleDevice: BleDevice) {
            if (BleManager.instance.isConnected(bleDevice)) {
                BleManager.instance.disconnect(bleDevice)
            }
        }

        override fun detailAction(bleDevice: BleDevice) {
            if (BleManager.instance.isConnected(bleDevice)) {
                findNavController().navigate(R.id.deviceFragment,
                    DeviceFragment.buildArguments(bleDevice))
            }
        }

    }
    private val mDeviceAdapter = DeviceAdapter()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding = FragmentMainBinding.bind(view)
        viewBinding.recyclerView.addItemDecoration(lineSeparatorDecoration)
        mDeviceAdapter.setActionClick(actionClick)
        viewBinding.recyclerView.adapter = mDeviceAdapter
        viewBinding.txtSetting.setOnClickListener {
            if (viewBinding.layoutSetting.visibility == View.VISIBLE) {
                viewBinding.layoutSetting.visibility = View.GONE
                viewBinding.txtSetting.text = getString(R.string.expand_search_settings)
            } else {
                viewBinding.layoutSetting.visibility = View.VISIBLE
                viewBinding.txtSetting.text = getString(R.string.retrieve_search_settings)
            }
        }
        viewBinding.btnScan.setOnClickListener {

            if (viewBinding.btnScan.text == getString(R.string.start_scan)) {
                checkPermissions()
            } else if (viewBinding.btnScan.text == getString(R.string.stop_scan)) {
                BleManager.instance.cancelScan()
            }


        }
        connectViewModel.scanDeviceLiveData.observeUnsticky(viewLifecycleOwner) { scanDeviceState ->
            run {
                when (scanDeviceState) {
                    is ScanState.ScanStart -> {
                        mDeviceAdapter.clearScanDevice()
                        viewBinding.imgLoading.startAnimation(operatingAnim)
                        viewBinding.imgLoading.visibility = View.VISIBLE
                        viewBinding.btnScan.text = getString(R.string.stop_scan)
                    }
                    is ScanState.Scanning -> {
                        mDeviceAdapter.addDevice(scanDeviceState.data!!)
                        mDeviceAdapter.notifyDataSetChanged()
                    }
                    is ScanState.ScanFinish -> {
                        viewBinding.imgLoading.clearAnimation()
                        viewBinding.imgLoading.visibility = View.INVISIBLE
                        viewBinding.btnScan.text = getString(R.string.start_scan)
                    }
                }
            }
        }
        connectViewModel.connectLiveData.observeUnsticky(viewLifecycleOwner) { connectState ->
            run {
                when (connectState) {
                    is ConnectState.ConnectStart -> {
                        showLoading("连接中...")
                        viewBinding.imgLoading.clearAnimation()
                        viewBinding.imgLoading.visibility = View.INVISIBLE
                        viewBinding.btnScan.text = getString(R.string.start_scan)
                    }
                    is ConnectState.ConnectSuccess -> {
                        hideLoading()
                        mDeviceAdapter.addDevice(connectState.data!!)
                        mDeviceAdapter.notifyDataSetChanged()
                    }
                    is ConnectState.ConnectFail -> {
                        viewBinding.imgLoading.clearAnimation()
                        viewBinding.imgLoading.visibility = View.INVISIBLE
                        viewBinding.btnScan.text = getString(R.string.start_scan)
                        hideLoading()
                        showToast(R.string.connect_fail)
                    }
                    is ConnectState.DisConnected -> {
                        viewBinding.imgLoading.clearAnimation()
                        viewBinding.imgLoading.visibility = View.INVISIBLE
                        viewBinding.btnScan.text = getString(R.string.start_scan)
                        hideLoading()
                        showToast(R.string.disconnected)
                        mDeviceAdapter.removeDevice(connectState.data!!)
                        mDeviceAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun startScan() {
        connectViewModel.setScanRule(viewBinding.etUuid.text.toString(),
            viewBinding.etName.text.toString(),
            viewBinding.etMac.text.toString(),
            false)
        connectViewModel.startScan()
    }

    private fun checkPermissions() {
        if (hasLocationPermission) {
            checkBluetooth()
        } else {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
                run {
                    if (permissionGranted) {
                        checkBluetooth()
                    } else {
                        Toast.makeText(requireContext(), "请打开定位权限", Toast.LENGTH_SHORT).show()
                    }
                }
            }.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkBluetooth() {
        if (BleManager.instance.isBlueEnable()) {
            startScan()
        } else {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            registerForActivityResult(object : ActivityResultContract<Intent, Boolean>() {
                override fun createIntent(context: Context, input: Intent): Intent {
                    return input
                }

                override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
                    Log.d(TAG, "resultCode=====$resultCode" + "intent========$intent")
                    return BleManager.instance.isBlueEnable()
                }

            }) { resultCallback ->
                if (resultCallback) {
                    startScan()
                }
                Log.d(TAG, "ActivityResultCallback=====$resultCallback")
            }.launch(intent)
        }
    }

    companion object {
        private val TAG = MainFragment::class.java.simpleName
    }
}