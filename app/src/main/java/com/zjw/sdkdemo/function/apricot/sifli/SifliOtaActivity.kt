package com.zjw.sdkdemo.function.apricot.sifli

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ThreadUtils
import com.sifli.siflidfu.Protocol
import com.sifli.siflidfu.SifliDFUService
import com.sifli.watchfacelibraryzh.SifliWatchfaceService
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.SifliDfuDeviceCallBack
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivitySifliOtaBinding
import com.zjw.sdkdemo.utils.DialogUtils
import com.zjw.sdkdemo.utils.MyConstants
import com.zjw.sdkdemo.utils.MyFileUtils
import java.io.File

class SifliOtaActivity : BaseActivity() {
    private val binding by lazy { ActivitySifliOtaBinding.inflate(layoutInflater) }
    private val tag: String = SifliOtaActivity::class.java.simpleName

    private val otaFilePath = PathUtils.getExternalAppCachePath() + "/sifilOrdinaryDial"
    private lateinit var otaFile: File

    private var dfuReceiver: DfuReceiver? = null
    private var timeoutTask: TimeoutTask? = null
    private var oldProgress = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.sifil_ota)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initListener()
        initCallback()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
    }

    private fun initData() {
        FileUtils.createOrExistsDir(otaFilePath)
        binding.layoutSelectFile.tvTip.text = getString(R.string.select_file_attention, otaFilePath)
        registerReceiver()
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutSelectFile.btnSelectFile) {
            addLogI("btnSelectFile")
            DialogUtils.showSelectFileDialog(this, otaFilePath, ".zip") { selectedFile ->
                otaFile = selectedFile
                binding.layoutSelectFile.tvFileName.text = selectedFile.name
            }
        }

        clickCheckConnect(binding.btnSend) {
            addLogI("btnSend")
            if (!::otaFile.isInitialized) {
                addLogI(getString(R.string.select_file_tip))
                return@clickCheckConnect
            }
            val otaFileList = MyFileUtils.saveSifliOtaCacheFile(otaFile)
            if (otaFileList.isEmpty()) {
                addLogI("otaFileList is null")
                return@clickCheckConnect
            }

            updateTimeoutTask()
            SifliDFUService.startActionDFUNorExt(this, MyConstants.deviceAddress, otaFileList, 1, 0)
            ControlBleTools.getInstance().disconnect()

        }
    }

    private fun initCallback() {
        CallBackUtils.sifliDfuDeviceCallBack = SifliDfuDeviceCallBack { mac ->
            addLogI("setSifliDfuDeviceCallBack mac=$mac")
            ThreadUtils.runOnUiThread {
                if (mac == MyConstants.deviceAddress) {
                    binding.btnSend.callOnClick()
                }
            }
        }
    }

    private inner class TimeoutTask : ThreadUtils.SimpleTask<Int>() {
        var i = 0
        var isOk = false

        fun finish(isOk: Boolean) {
            oldProgress = -1
            this.isOk = isOk
            i = 60
        }

        override fun doInBackground(): Int {
            while (i < 60) {
                i++
                Thread.sleep(1000)
            }
            return 0
        }

        override fun onSuccess(result: Int?) {
            addLogI("TimeoutTask onSuccess result=$result isOk=$isOk")
            ControlBleTools.getInstance().connect(MyConstants.deviceName, MyConstants.deviceAddress, MyConstants.deviceProtocol)
        }
    }


    private fun updateTimeoutTask() {
        if (timeoutTask != null) {
            ThreadUtils.cancel(timeoutTask)
        }
        timeoutTask = TimeoutTask()
        ThreadUtils.executeByIo(timeoutTask)
    }

    inner class DfuReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {

                    SifliDFUService.BROADCAST_DFU_LOG -> {
                        val log = intent.getStringExtra(SifliDFUService.EXTRA_LOG_MESSAGE)
                        addLogI("onReceive BROADCAST_DFU_LOG log=$log")
                    }

                    SifliDFUService.BROADCAST_DFU_STATE -> {
                        val state = intent.getIntExtra(SifliDFUService.EXTRA_DFU_STATE, 0)
                        val stateResult = intent.getIntExtra(SifliDFUService.EXTRA_DFU_STATE_RESULT, 0)
                        addLogI("onReceive BROADCAST_DFU_STATE state=$state stateResult=$stateResult")
                        if (state == Protocol.DFU_SERVICE_EXIT && stateResult == 0) {
                            timeoutTask?.finish(true)
                        } else {
                            timeoutTask?.finish(false)
                        }
                        ControlBleTools.getInstance().connect(MyConstants.deviceName, MyConstants.deviceAddress, MyConstants.deviceProtocol)
                    }

                    SifliDFUService.BROADCAST_DFU_PROGRESS -> {
                        val progress = intent.getIntExtra(SifliDFUService.EXTRA_DFU_PROGRESS, 0)
                        val progressType = intent.getIntExtra(SifliDFUService.EXTRA_DFU_PROGRESS_TYPE, 0)
                        addLogI("onReceive BROADCAST_DFU_PROGRESS progress=$progress progressType=$progressType")
                        if (oldProgress != progress) {
                            oldProgress = progress
                            updateTimeoutTask()
                        }
                    }
                }
            }
        }
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(SifliWatchfaceService.BROADCAST_WATCHFACE_STATE)
        intentFilter.addAction(SifliWatchfaceService.BROADCAST_WATCHFACE_PROGRESS)
        dfuReceiver = DfuReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(dfuReceiver!!, intentFilter)
    }

    private fun unregisterReceiver() {
        try {
            if (dfuReceiver != null) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(dfuReceiver!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}