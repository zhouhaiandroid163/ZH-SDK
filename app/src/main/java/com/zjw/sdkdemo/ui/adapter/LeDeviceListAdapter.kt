package com.zjw.sdkdemo.ui.adapter

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.bean.ScanDeviceBean
import com.zhapp.ble.utils.BleUtils
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.function.MainActivity
import com.zjw.sdkdemo.function.ScanDeviceActivity
import com.zjw.sdkdemo.utils.MyConstants
import com.zjw.sdkdemo.utils.MyFormatUtils
import com.zjw.sdkdemo.utils.SpUtils

class LeDeviceListAdapter(private val mContext: Context) : BaseAdapter() {

    private class ViewHolder {
        lateinit var llTopView: LinearLayoutCompat
        lateinit var tvDeviceName: AppCompatTextView
        lateinit var tvDeviceAddress: AppCompatTextView
        lateinit var tvBroadcast: AppCompatTextView
        lateinit var btnConnect: AppCompatButton
        lateinit var llBottomView: LinearLayoutCompat
        lateinit var tvProtocol: AppCompatTextView
    }

    private val mLeDevices = ArrayList<ScanDeviceBean>()
    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)

    fun addDevice(device: ScanDeviceBean): String {
        if (getIsRepeat(mLeDevices, device)) {
            mLeDevices.add(0, device)
            mLeDevices.sortWith { o1, o2 ->
                val num1 = o1.rssi
                val num2 = o2.rssi
                num2 - num1
            }
            return device.address
        } else {
            return ""
        }
    }

    fun clearAll() {
        mLeDevices.clear()
    }

    private fun getIsRepeat(listDevice: ArrayList<ScanDeviceBean>, device: ScanDeviceBean): Boolean {
        for (i in listDevice.indices) {
            if (listDevice[i].address == device.address && TextUtils.equals(listDevice[i].deviceType, device.deviceType)) {
                return false
            }
        }
        return true
    }

    fun getDevice(position: Int): ScanDeviceBean {
        return mLeDevices[position]
    }

    fun clear() {
        mLeDevices.clear()
    }

    override fun getCount(): Int {
        return mLeDevices.size
    }

    override fun getItem(position: Int): Any {
        return mLeDevices[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item_device, parent, false)
            viewHolder = ViewHolder()
            viewHolder.llTopView = view.findViewById(R.id.llTopView)
            viewHolder.tvDeviceName = view.findViewById(R.id.tvDeviceName)
            viewHolder.tvDeviceAddress = view.findViewById(R.id.tvDeviceAddress)
            viewHolder.tvBroadcast = view.findViewById(R.id.tvBroadcast)
            viewHolder.btnConnect = view.findViewById(R.id.btnConnect)
            viewHolder.llBottomView = view.findViewById(R.id.llBottomView)
            viewHolder.tvProtocol = view.findViewById(R.id.tvProtocol)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val device = getDevice(position)

        viewHolder.btnConnect.setOnClickListener {
            handleData(device)
        }

        viewHolder.tvDeviceName.text = device.name
        viewHolder.tvDeviceAddress.text = device.address

        val isBroadcast = !TextUtils.isEmpty(device.deviceType)

        viewHolder.llTopView.setOnClickListener {
            if (isBroadcast) {
                viewHolder.llBottomView.visibility = if (viewHolder.llBottomView.isVisible) View.GONE else View.VISIBLE
            }
        }

        if (isBroadcast) {
            viewHolder.tvBroadcast.text = mContext.getString(R.string.broadcast_is_true)
            viewHolder.tvBroadcast.setTextColor(ContextCompat.getColor(mContext, R.color.color_1456F0))
            viewHolder.tvProtocol.text = getProtocolStr(device)
        } else {
            viewHolder.tvBroadcast.text = mContext.getString(R.string.broadcast_is_false)
            viewHolder.tvBroadcast.setTextColor(ContextCompat.getColor(mContext, R.color.color_888888))
            viewHolder.llBottomView.visibility = View.GONE
        }

        return view
    }

    private fun getProtocolStr(device: ScanDeviceBean): String {
        return MyFormatUtils.format(device)?.replace("isBind", mContext.getString(R.string.broadcast_is_bind))
            ?.replace("isUserMode", mContext.getString(R.string.broadcast_is_user_mode))
            ?.replace("deviceType", mContext.getString(R.string.broadcast_device_type))
            ?.replace("deviceVersionName", mContext.getString(R.string.broadcast_device_version_name))
            ?.replace("protocolName", mContext.getString(R.string.broadcast_protocol_name))
            ?: ""
    }

    private fun handleData(device: ScanDeviceBean) {
        //设备支持通话蓝牙 & 通话蓝牙蓝牙mac不为空  The device supports call bluetooth & call bluetooth bluetooth mac is not empty
        if (device.isSupportHeadset && !TextUtils.isEmpty(device.headsetMac)) {
            SpUtils.setBrDeviceMac(device.headsetMac)
            val hBleName = "XXX_Calling_" + BleUtils.getMacLastStr(device.headsetMac, 4)
            SpUtils.setBrDeviceName(hBleName)
        } else {
            SpUtils.setBrDeviceName("")
            SpUtils.setBrDeviceMac("")
        }
        val intent = Intent(mContext, MainActivity::class.java)
        intent.putExtra(MyConstants.EXTRA_DEVICE_ADDRESS, device.address)
            .putExtra(MyConstants.EXTRA_DEVICE_NAME, device.name)
            .putExtra(MyConstants.EXTRA_DEVICE_PROTOCOL, device.protocolName)
            .putExtra(MyConstants.EXTRA_DEVICE_IS_BIND, device.isBind)
        if(device.serviceDataString.isNullOrEmpty() && device.protocolName.isNullOrEmpty()){
            showProtocolChooseDialog(device,intent)
            return
        }
        mContext.startActivity(intent)
    }

    private fun showProtocolChooseDialog(device: ScanDeviceBean,intent: Intent) {
        //弹窗提示选择协议
        // 弹窗提示选择协议
        AlertDialog.Builder(mContext)
            .setTitle(mContext.getString(R.string.choose_protocol))
            .setItems(arrayOf(mContext.getString(R.string.protocol_a), mContext.getString(R.string.protocol_b))) { dialog, which ->
                when (which) {
                    0 -> {
                        // 选择协议A
                        intent.putExtra(MyConstants.EXTRA_DEVICE_PROTOCOL, BleCommonAttributes.DEVICE_PROTOCOL_APRICOT)
                        mContext.startActivity(intent)
                        SpUtils.setValue(ScanDeviceActivity.SP_PROTOCOL_NAME + device.address, BleCommonAttributes.DEVICE_PROTOCOL_APRICOT)
                    }
                    1 -> {
                        // 选择协议B
                        intent.putExtra(MyConstants.EXTRA_DEVICE_PROTOCOL, BleCommonAttributes.DEVICE_PROTOCOL_BERRY)
                        mContext.startActivity(intent)
                        SpUtils.setValue(ScanDeviceActivity.SP_PROTOCOL_NAME + device.address, BleCommonAttributes.DEVICE_PROTOCOL_BERRY)
                    }
                }
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}