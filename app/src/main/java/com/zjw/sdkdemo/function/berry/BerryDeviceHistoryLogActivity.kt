package com.zjw.sdkdemo.function.berry

import android.os.Bundle
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.DeviceFileUploadStatusBean
import com.zhapp.ble.bean.LogFileStatusBean
import com.zhapp.ble.callback.BerryFirmwareLogCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBerryDeviceHistoryLogBinding
import com.zjw.sdkdemo.function.MainActivity.GlobalData

class BerryDeviceHistoryLogActivity : BaseActivity() {
    val binding by lazy { ActivityBerryDeviceHistoryLogBinding.inflate(layoutInflater) }
    private val tag: String = BerryDeviceHistoryLogActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.data_get_device_history_log_berry)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallBack()
    }

    override fun onDestroy() {
        super.onDestroy()
        addLogI("disconnect")
        ControlBleTools.getInstance().disconnect()
    }

    private fun initView() {
        binding.rgLog.check(R.id.rb1)
    }

    private fun initListener() {
        clickCheckConnect(binding.btnGet) {
            addLogI("btnGet")
            if (GlobalData.deviceInfo == null) {
                addLogI("deviceInfo is null")
                return@clickCheckConnect
            }
            var type = 8
            when (binding.rgLog.checkedRadioButtonId) {
                R.id.rb1 -> type = 8
                R.id.rb2 -> type = 9
                R.id.rb3 -> type = 10
                R.id.rb4 -> type = 11
                R.id.rb5 -> type = 12
                R.id.rb6 -> type = 13
            }
            val optionalUserId = "10086"
            val optionalPhoneType = DeviceUtils.getManufacturer() + " - " + DeviceUtils.getModel() + " - " + DeviceUtils.getSDKVersionCode()
            val optionalAppVer = AppUtils.getAppVersionName()
            val optionalDeviceType = GlobalData.deviceInfo!!.equipmentNumber
            addLogI("requestLogFileStatusByBerry type=$type optionalUserId=$optionalUserId optionalAppVer=$optionalAppVer optionalDeviceType=$optionalDeviceType")
            ControlBleTools.getInstance().requestLogFileStatusByBerry(type, optionalUserId, optionalPhoneType, optionalAppVer, optionalDeviceType, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("requestLogFileStatusByBerry state=$state")
                }
            })

        }
    }

    private fun initCallBack() {

        CallBackUtils.berryFirmwareLogCallBack = object : BerryFirmwareLogCallBack {
            override fun onDeviceRequestAppGetLog() {
                addLogI("berryFirmwareLogCallBack onDeviceRequestAppGetLog")
            }

            override fun onLogFileStatus(bean: LogFileStatusBean) {
                addLogBean("berryFirmwareLogCallBack onLogFileStatus", bean)
                if (bean.fileSize > 0) {
                    val isStart = true
                    val type = bean.type
                    val size = bean.fileSize
                    addLogI("requestUploadLogFileByBerry isStart=$isStart type=$type size=$size")
                    ControlBleTools.getInstance().requestUploadLogFileByBerry(isStart, type, size, object : SendCmdStateListener() {
                        override fun onState(state: SendCmdState) {
                            addLogI("requestUploadLogFileByBerry state=$state")
                        }
                    })
                }
            }

            override fun onLogProgress(curSize: Int, allSize: Int) {
                addLogI("berryFirmwareLogCallBack onLogProgress curSize=$curSize allSize=$allSize")
            }

            override fun onLogFileUploadStatus(bean: DeviceFileUploadStatusBean) {
                addLogBean("berryFirmwareLogCallBack onLogFileUploadStatus", bean)
                if (bean.isSuccessful) {
                    val isStart = false
                    val type = bean.type
                    val size = bean.fileSize
                    addLogI("requestUploadLogFileByBerry isStart=$isStart type=$type size=$size")
                    ControlBleTools.getInstance().requestUploadLogFileByBerry(isStart, type, size, object : SendCmdStateListener() {
                        override fun onState(state: SendCmdState) {
                            addLogI("requestUploadLogFileByBerry state=$state")
                        }
                    })
                }
            }

            override fun onLogFilePath(type: Int, path: String?) {
                addLogI("berryFirmwareLogCallBack onLogFilePath type=$type path=$path")
            }
        }
    }
}