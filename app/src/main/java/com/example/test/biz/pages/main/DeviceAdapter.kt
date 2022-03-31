package com.example.test.biz.pages.main

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.processViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.biz.viewmodel.ConnectViewModel
import com.example.test.databinding.AdapterDeviceBinding
import com.example.test.recyclerView.AutoInflateViewHolder
import com.example.test.recyclerView.activityHost
import com.example.test.recyclerView.context
import com.example.test.utils.NavigationConfig
import com.yg.ble.BleManager
import com.yg.ble.data.BleDevice
import java.util.concurrent.CopyOnWriteArrayList

class DeviceAdapter() :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    private val deviceList = CopyOnWriteArrayList<BleDevice>()
    private var actionClick: ActionClick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(parent, actionClick)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bindData(deviceList[position])
    }

    fun setActionClick(actionClick: ActionClick) {
        this.actionClick = actionClick
    }

    fun addDevice(bleDevice: BleDevice) {
        removeDevice(bleDevice)
        deviceList.add(bleDevice)
        notifyDataSetChanged()
    }

    fun removeDevice(bleDevice: BleDevice) {
        for (device in deviceList) {
            if (bleDevice.getKey() == device.getKey()) {
                deviceList.remove(device)
            }
        }
    }

    private fun clearConnectedDevice() {
        for (device in deviceList) {
            if (BleManager.instance.isConnected(device)) {
                deviceList.remove(device)
            }
        }
        notifyDataSetChanged()
    }

    fun clearScanDevice() {
        for (device in deviceList) {
            if (!BleManager.instance.isConnected(device)) {
                deviceList.remove(device)
            }
        }
        notifyDataSetChanged()
    }

    fun clear() {
        clearConnectedDevice()
        clearScanDevice()
    }

    fun submit(devices: List<BleDevice>) {
        deviceList.clear()
        deviceList.addAll(devices)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = deviceList.size
    class DeviceViewHolder(
        parent: ViewGroup,
        private val actionClick: ActionClick?,
    ) :
        AutoInflateViewHolder(parent, R.layout.adapter_device) {
        private val viewBinding = AdapterDeviceBinding.bind(itemView)

        fun bindData(bleDevice: BleDevice) {
            val isConnected = BleManager.instance.isConnected(bleDevice)
            val name = bleDevice.getName()
            val mac = bleDevice.getMac()
            val rssi = bleDevice.getRssi()
            viewBinding.txtName.text = name
            viewBinding.txtMac.text = mac
            viewBinding.txtRssi.text = rssi.toString()
            if (isConnected) {
                viewBinding.imgBlue.setImageResource(R.mipmap.ic_blue_connected)
                viewBinding.txtName.setTextColor(ContextCompat.getColor(context,
                    R.color.color_FF1DE9B6))
                viewBinding.txtMac.setTextColor(ContextCompat.getColor(context,
                    R.color.color_FF1DE9B6))
                viewBinding.layoutIdle.visibility = View.GONE
                viewBinding.layoutConnected.visibility = View.VISIBLE
            } else {
                viewBinding.imgBlue.setImageResource(R.mipmap.ic_blue_remote)
                viewBinding.txtName.setTextColor(ContextCompat.getColor(context, R.color.black))
                viewBinding.txtMac.setTextColor(ContextCompat.getColor(context, R.color.black))
                viewBinding.layoutIdle.visibility = View.VISIBLE
                viewBinding.layoutConnected.visibility = View.GONE

            }
            viewBinding.btnConnect.setOnClickListener {
                actionClick?.connectAction(bleDevice)

            }
            viewBinding.btnDisconnect.setOnClickListener {
                actionClick?.disConnectAction(bleDevice)

            }
            viewBinding.btnDetail.setOnClickListener {
                actionClick?.detailAction(bleDevice)
            }
        }
    }

    interface ActionClick {
        fun connectAction(bleDevice: BleDevice)
        fun disConnectAction(bleDevice: BleDevice)
        fun detailAction(bleDevice: BleDevice)
    }
}