package com.zjw.sdkdemo.function

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.ClickUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.ScanDeviceBean
import com.zhapp.ble.callback.ScanDeviceCallBack
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityScandDeviceBinding
import com.zjw.sdkdemo.ui.adapter.LeDeviceListAdapter
import com.zjw.sdkdemo.utils.MyConstants
import com.zjw.sdkdemo.utils.SpUtils

/**
 * 扫描设备
 * scanning device
 */
@SuppressLint("MissingPermission")
class ScanDeviceActivity : BaseActivity() {
    val binding by lazy { ActivityScandDeviceBinding.inflate(layoutInflater) }
    private val tag: String = ScanDeviceActivity::class.java.simpleName

    companion object {
        const val SP_PROTOCOL_NAME = "SP_PROTOCOL_NAME"
        const val MESSAGE_BLE_SCAN: Int = 101
    }

    private var mHandler: Handler? = null
    private val scanHandler = Handler(Looper.getMainLooper())

    private var searchText = ""
    private var isBLEScanning = false

    var bluetoothAdapter: BluetoothAdapter? = null
    private var mLeDeviceListAdapter: LeDeviceListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.title_scan_device)
        initView()
        initListener()
        initHandle()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanLeDevice(false)
        scanHandler.removeCallbacksAndMessages(null)
        mHandler!!.removeCallbacksAndMessages(null)
    }

    fun initHandle() {
        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_BLE_SCAN -> {
                        val deviceBean = msg.obj as ScanDeviceBean
                        Log.i(tag, "deviceBean=${formatObject(deviceBean)}")

                        val deviceName = deviceBean.name
                        if (deviceName != null && deviceName.isNotEmpty()) {
                            var isCancelAdd = true
                            if (!TextUtils.isEmpty(searchText) && !deviceName.lowercase().contains(searchText.lowercase()) && !deviceBean.address.lowercase()
                                    .equals(searchText.lowercase(), ignoreCase = true)
                            ) {
                                isCancelAdd = false
                            }
                            if (isCancelAdd) {
                                mLeDeviceListAdapter!!.addDevice(deviceBean)
                                binding.lvDevice.post { mLeDeviceListAdapter!!.notifyDataSetChanged() }
                            }
                        }
                    }

                    else -> {}
                }
                super.handleMessage(msg)
            }
        }
    }

    private fun initView() {
        mLeDeviceListAdapter = LeDeviceListAdapter(this@ScanDeviceActivity)
        binding.lvDevice.adapter = mLeDeviceListAdapter
        searchText = SpUtils.getSearchName()
        binding.etSearch.setText(searchText)
        binding.etSearch.setSelection(searchText.length)
        binding.etSearch.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (!TextUtils.isEmpty(s)) {
                        searchText = s.toString()
                        SpUtils.setSearchName(searchText)
                        mLeDeviceListAdapter!!.clearAll()
                        mLeDeviceListAdapter!!.notifyDataSetChanged()
                    }
                }
            }
        )
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    private fun initListener() {
        ClickUtils.applySingleDebouncing(binding.btnStart) {
            startScanLeDevice()
        }
        ClickUtils.applySingleDebouncing(binding.btnStop) {
            stopSCan()
        }
    }

    fun startScanLeDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 0x999
                )
                return
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0x9999)
                return
            }
        }

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            val mIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(mIntent, 0x99999)
        }

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!providerEnabled) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(settingsIntent)
            return
        }
        ControlBleTools.getInstance().setInitStatusCallBack { scanLeDevice(true) }
    }

    private inner class BluetoothScanDeviceCallBack : ScanDeviceCallBack {
        override fun onBleScan(device: ScanDeviceBean) {
            if (isBLEScanning) {
                if (!device.protocolName.isNullOrEmpty()) {
                    SpUtils.getValue(SP_PROTOCOL_NAME + device.address, device.protocolName)
                }
                sendDeviceDate(device)
            }
        }
    }

    private fun initTestBindDevice() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val devices = adapter.bondedDevices
        for (device in devices) {
            val scanDeviceBean = ScanDeviceBean()
            scanDeviceBean.device = device
            scanDeviceBean.name = device.name
            scanDeviceBean.address = device.address
            scanDeviceBean.protocolName = SpUtils.getValue(SP_PROTOCOL_NAME + scanDeviceBean.address, "")
            sendDeviceDate(scanDeviceBean)
        }
    }

    fun sendDeviceDate(device: ScanDeviceBean?) {
        val message = Message()
        message.what = MESSAGE_BLE_SCAN
        message.obj = device
        mHandler!!.sendMessage(message)
    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            if (!this.bluetoothAdapter!!.isEnabled) {
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            }
            // Stops scanning after a pre-defined scan period.
            scanHandler.removeCallbacksAndMessages(null)
            scanHandler.postDelayed({
                stopSCan()
                ControlBleTools.getInstance().stopScanDevice()
            }, MyConstants.SCAN_DEVICE_TIME.toLong())

            startSCan()
            ControlBleTools.getInstance().startScanDevice(BluetoothScanDeviceCallBack())
        } else {
            stopSCan()
            scanHandler.removeCallbacksAndMessages(null)
            ControlBleTools.getInstance().stopScanDevice()
        }
    }

    fun stopSCan() {
        isBLEScanning = false
    }

    fun startSCan() {
        mLeDeviceListAdapter!!.clear()
        isBLEScanning = true
        initTestBindDevice()
    }
}