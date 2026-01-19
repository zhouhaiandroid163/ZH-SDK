package com.zjw.sdkdemo.function.factory

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.UriUtils
import com.blankj.utilcode.util.ZipUtils
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.DeviceFileUploadStatusBean
import com.zhapp.ble.bean.LogFileStatusBean
import com.zhapp.ble.callback.BerryFirmwareLogCallBack
import com.zhapp.ble.callback.BerryRealTimeLogCollectCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.parsing.ParsingStateManager
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBerryDeviceRelaTimeLogBinding
import com.zjw.sdkdemo.function.MainActivity.GlobalData
import com.zjw.sdkdemo.livedata.BleConnectState
import com.zjw.sdkdemo.manager.WakeLockManager
import java.io.File

class BerryDeviceRealtimeLogActivity : BaseActivity() {
    val binding by lazy { ActivityBerryDeviceRelaTimeLogBinding.inflate(layoutInflater) }
    private val tag: String = BerryDeviceRealtimeLogActivity::class.java.simpleName

    private var isEnd = true
    var type = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_get_device_real_log_berry)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initListener()
        initCallBack()
        initData()
    }

    private fun initData() {
        supportActionBar?.apply {
            setHomeButtonEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }
        binding.tvSType.text = binding.btnType1.text.toString()
        binding.tvStatus.text = getString(R.string.not_started)
        WakeLockManager.Companion.instance.keepUnLock(this.lifecycle)
    }

    private fun initListener() {
        clickCheckConnect(binding.btnType1) {
            addLogI("btnType1")
            type = 1
            binding.tvSType.text = binding.btnType1.text.toString()
        }
        clickCheckConnect(binding.btnType2) {
            addLogI("btnType2")
            type = 2
            binding.tvSType.text = binding.btnType2.text.toString()
        }
        clickCheckConnect(binding.btnType3) {
            addLogI("btnType3")
            type = 3
            binding.tvSType.text = binding.btnType3.text.toString()
        }
        clickCheckConnect(binding.btnType4) {
            addLogI("btnType4")
            type = 4
            binding.tvSType.text = binding.btnType4.text.toString()
        }
        clickCheckConnect(binding.btnType5) {
            addLogI("btnType5")
            type = 5
            binding.tvSType.text = binding.btnType5.text.toString()
        }
        clickCheckConnect(binding.btnType6) {
            addLogI("btnType6")
            type = 6
            binding.tvSType.text = binding.btnType6.text.toString()
        }
        clickCheckConnect(binding.btnType7) {
            addLogI("btnType7")
            type = 7
            binding.tvSType.text = binding.btnType7.text.toString()
        }
        clickCheckConnect(binding.btnType8) {
            addLogI("btnType8")
            type = 8
            binding.tvSType.text = binding.btnType8.text.toString()
        }
        clickCheckConnect(binding.btnType9) {
            addLogI("btnType9")
            type = 9
            binding.tvSType.text = binding.btnType9.text.toString()
        }
        clickCheckConnect(binding.btnType10) {
            addLogI("btnType10")
            type = 10
            binding.tvSType.text = binding.btnType10.text.toString()
        }
        clickCheckConnect(binding.btnType11) {
            addLogI("btnType11")
            type = 11
            binding.tvSType.text = binding.btnType11.text.toString()
        }
        clickCheckConnect(binding.btnType12) {
            addLogI("btnType12")
            type = 12
            binding.tvSType.text = binding.btnType12.text.toString()
        }
        clickCheckConnect(binding.btnType13) {
            addLogI("btnType13")
            type = 13
            binding.tvSType.text = binding.btnType13.text.toString()
        }
        clickCheckConnect(binding.btnType14) {
            addLogI("btnType14")
            type = 14
            binding.tvSType.text = binding.btnType14.text.toString()
        }
        clickCheckConnect(binding.btnType15) {
            addLogI("btnType15")
            type = 15
            binding.tvSType.text = binding.btnType15.text.toString()
        }
        clickCheckConnect(binding.btnType16) {
            addLogI("btnType16")
            type = 16
            binding.tvSType.text = binding.btnType16.text.toString()
        }
        clickCheckConnect(binding.btnType17) {
            addLogI("btnType17")
            type = 17
            binding.tvSType.text = binding.btnType17.text.toString()
        }
        clickCheckConnect(binding.btnType18) {
            addLogI("btnType18")
            type = 18
            binding.tvSType.text = binding.btnType18.text.toString()
        }
        clickCheckConnect(binding.btnType19) {
            addLogI("btnType19")
            type = 19
            binding.tvSType.text = binding.btnType19.text.toString()
        }
        clickCheckConnect(binding.btnType20) {
            addLogI("btnType20")
            type = 20
            binding.tvSType.text = binding.btnType20.text.toString()
        }
        clickCheckConnect(binding.btnType21) {
            addLogI("btnType21")
            type = 21
            binding.tvSType.text = binding.btnType21.text.toString()
        }
        clickCheckConnect(binding.btnType22) {
            addLogI("btnType22")
            type = 22
            binding.tvSType.text = binding.btnType22.text.toString()
        }
        clickCheckConnect(binding.btnType23) {
            addLogI("btnType23")
            type = 23
            binding.tvSType.text = binding.btnType23.text.toString()
        }

        clickCheckConnect(binding.btnStartLog) {
            addLogI("btnGet")
            if (GlobalData.deviceInfo == null) {
                addLogI("deviceInfo is null")
                return@clickCheckConnect
            }
            addLogI("btnStartLog")
            binding.btnStartLog.isEnabled = false
            binding.tvStatus.text = getString(R.string.in_progress)
            ControlBleTools.getInstance().berryRealTimeLog(true, type, object : ParsingStateManager.SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("berryRealTimeLog state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnStopLog) {
            addLogI("btnStopLog")
            isEnd = true
            binding.btnStartLog.isEnabled = true
            binding.tvStatus.text = getString(R.string.not_started)
            ControlBleTools.getInstance().berryRealTimeLog(false, type, object : ParsingStateManager.SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("berryRealTimeLog state=$state")
                }
            })
        }

        binding.btnExit.setOnLongClickListener {
            addLogI("btnExit")
            if (!isEnd) {
                return@setOnLongClickListener false
            }
            finish()
            return@setOnLongClickListener true
        }

        clickCheckConnect(binding.btnShare) {
            addLogI("btnShare")
            val zipFilePath = getExternalFilesDir("logZip")?.absolutePath + File.separator + "device log_log_" + System.currentTimeMillis() + ".zip"

            val logDirs = arrayListOf<String>()
            getExternalFilesDir("deviceLog")?.absolutePath?.let {
                logDirs.add(it)
            }
            shareAllZip(logDirs, zipFilePath)
        }

    }

    private fun initCallBack() {
        BleConnectState.observe(this) { state ->
            addLogI("BleConnectState state=$state")
            if (state == BleCommonAttributes.STATE_CONNECTED || state == BleCommonAttributes.STATE_DISCONNECTED) {
                isEnd = true
                binding.btnStartLog.isEnabled = true
            }
        }

        CallBackUtils.berryRealTimeLogCollectCallBack = BerryRealTimeLogCollectCallBack { bean ->
            addLogBean("berryRealTimeLogCollectCallBack", bean)
            if (bean != null && bean.dataLen > 0) {
                val type = 13
                val optionalUserId = "10086"
                val optionalPhoneType = DeviceUtils.getManufacturer() + " - " + DeviceUtils.getModel() + " - " + DeviceUtils.getSDKVersionCode()
                val optionalAppVer = AppUtils.getAppVersionName()
                val optionalDeviceType = GlobalData.deviceInfo!!.equipmentNumber
                addLogI("requestLogFileStatusByBerry type=$type optionalUserId=$optionalUserId optionalPhoneType=$optionalPhoneType optionalAppVer=$optionalAppVer optionalDeviceType=$optionalDeviceType")
                    ControlBleTools.getInstance().requestLogFileStatusByBerry(type, optionalUserId, optionalPhoneType, optionalAppVer, optionalDeviceType, object : ParsingStateManager.SendCmdStateListener() {
                    override fun onState(state: SendCmdState) {
                        addLogI("requestLogFileStatusByBerry state=$state")
                    }
                })
            }
        }

        CallBackUtils.berryFirmwareLogCallBack = object : BerryFirmwareLogCallBack {
            override fun onDeviceRequestAppGetLog() {
                addLogI("berryFirmwareLogCallBack onDeviceRequestAppGetLog")
            }

            override fun onLogFileStatus(bean: LogFileStatusBean) {
                addLogBean("berryFirmwareLogCallBack onLogFileStatus", bean)

                if (bean.type == BerryFirmwareLogCallBack.LogFileType.SENSOR_LOG.type && bean.fileSize > 0) {
                    isEnd = false
                    val isStart = true
                    val type = bean.type
                    val size = bean.fileSize
                    addLogI("requestUploadLogFileByBerry isStart=$isStart type=$type size=$size")
                    ControlBleTools.getInstance().requestUploadLogFileByBerry(isStart, type, size, object : ParsingStateManager.SendCmdStateListener() {
                        override fun onState(state: SendCmdState) {
                            addLogI("requestUploadLogFileByBerry state=$state")
                        }
                    })
                }
            }

            override fun onLogProgress(curSize: Int, allSize: Int) {
                val logStr = "berryFirmwareLogCallBack onLogProgress curSize=$curSize allSize=$allSize"
                addLogI(logStr)
                isEnd = false
                binding.tvProgress.text = logStr
            }

            override fun onLogFileUploadStatus(bean: DeviceFileUploadStatusBean) {
                addLogBean("berryFirmwareLogCallBack onLogFileUploadStatus", bean)
                if (bean.isSuccessful) {
                    val isStart = false
                    val type = bean.type
                    val size = bean.fileSize
                    addLogI("requestUploadLogFileByBerry isStart=$isStart type=$type size=$size")
                    ControlBleTools.getInstance().requestUploadLogFileByBerry(isStart, type, size, object : ParsingStateManager.SendCmdStateListener() {
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

    private fun shareAllZip(logDirs: List<String>, zipFilePath: String) {
        ThreadUtils.executeByIo(object : ThreadUtils.Task<String>() {
            override fun onCancel() {}
            override fun onFail(t: Throwable?) {
                addLogE("shareZip ->$t")
            }

            override fun doInBackground(): String {
                var zipPath = ""
                val logPaths = mutableListOf<String>()
                for (logDir in logDirs) {
                    for (file in listAllFilesInDir(logDir)) {
                        logPaths.add(file.absolutePath)
                    }
                }
                if (logPaths.isNotEmpty()) {
                    ZipUtils.zipFiles(logPaths, zipFilePath)
                    zipPath = zipFilePath
                }
                return zipPath
            }

            override fun onSuccess(result: String?) {
                if (result.isNullOrEmpty()) {
                    addLogE("no share log")
                    return
                }
                val zipFile = FileUtils.getFileByPath(result)
                var intent = Intent(Intent.ACTION_SEND)
                intent.type = "application/zip"
                intent.putExtra(Intent.EXTRA_STREAM, UriUtils.file2Uri(zipFile))
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.btn_share))
                intent = Intent.createChooser(intent, getString(R.string.btn_share))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        })
    }

    fun listAllFilesInDir(dir: String): List<File> {
        val list = mutableListOf<File>()
        for (file in FileUtils.listFilesInDir(dir)) {
            if (file.isFile) {
                list.add(file)
            } else if (file.isDirectory) {
                list.addAll(listAllFilesInDir(file.absolutePath))
            }
        }
        return list
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false
        }
        return super.onKeyDown(keyCode, event)
    }
}