package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.FirmwareLogStateCallBack
import com.zhapp.ble.callback.FirmwareTrackingLogCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityDeviceHistoryLogBinding

class DeviceHistoryLogActivity : BaseActivity() {
    private val binding by lazy { ActivityDeviceHistoryLogBinding.inflate(layoutInflater) }
    private val tag: String = DeviceHistoryLogActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.data_get_device_history_log)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initListener()
        initCallback()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnGetDeviceLog) {
            addLogI("btnGetDeviceLog")
            addLogI("getFirmwareLog")
            ControlBleTools.getInstance().getFirmwareLog(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getFirmwareLog state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnGetTrackingLog) {
            addLogI("btnGetTrackingLog")
            addLogI("getFirmwareTrackingLog")
            ControlBleTools.getInstance().getFirmwareTrackingLog(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getFirmwareTrackingLog state=$state")
                }
            })
        }
    }

    private fun initCallback() {
        CallBackUtils.firmwareLogStateCallBack = object : FirmwareLogStateCallBack {
            override fun onFirmwareLogState(state: Int) {
                addLogI("firmwareLogStateCallBack onFirmwareLogState state=$state")
            }

            override fun onFirmwareLogFilePath(filePath: String?) {
                addLogI("firmwareLogStateCallBack onFirmwareLogFilePath filePath=$filePath")
            }
        }
        CallBackUtils.firmwareTrackingLogCallBack = FirmwareTrackingLogCallBack { bean ->
            addLogBean("firmwareTrackingLogCallBack", bean)
        }
    }
}