package com.zjw.sdkdemo.function.apricot.sifli

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ThreadUtils
import com.sifli.watchfacelibraryzh.SifliWatchfaceService
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceWatchFaceFileStatusListener
import com.zhapp.ble.callback.WatchFaceInstallCallBack
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivitySifliDialBinding
import com.zjw.sdkdemo.utils.DialogUtils
import com.zjw.sdkdemo.utils.MyConstants
import java.io.File

class SifliDialActivity : BaseActivity() {
    private val binding by lazy { ActivitySifliDialBinding.inflate(layoutInflater) }
    private val tag: String = SifliDialActivity::class.java.simpleName

    private val ordinaryFilePath = PathUtils.getExternalAppCachePath() + "/sifilOrdinaryDial"
    private lateinit var ordinaryFile: File

    private var dfuReceiver: DfuReceiver? = null
    private var timeoutTask: TimeoutTask? = null
    private var oldProgress = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.sifil_dial)
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
        FileUtils.createOrExistsDir(ordinaryFilePath)
        binding.layoutSelectFile.tvTip.text = getString(R.string.select_file_attention, ordinaryFilePath)
        registerReceiver()
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutSelectFile.btnSelectFile) {
            addLogI("btnSelectFile")
            DialogUtils.showSelectFileDialog(this, ordinaryFilePath, ".zip") { selectedFile ->
                ordinaryFile = selectedFile
                binding.layoutSelectFile.tvFileName.text = selectedFile.name
            }
        }

        clickCheckConnect(binding.btnSend) {
            addLogI("btnSend")
            if (!::ordinaryFile.isInitialized) {
                addLogI(getString(R.string.select_file_tip))
                return@clickCheckConnect
            }
            val dialData = FileIOUtils.readFile2BytesByStream(ordinaryFile)
            val dialId = "dialId"
            val dialSize = dialData.size
            val isReplace = true
            addLogI("getDeviceWatchFace dialId=$dialId isReplace=$isReplace dialSize=$dialSize")
            ControlBleTools.getInstance().getDeviceWatchFace(dialId, dialData.size, isReplace, object : DeviceWatchFaceFileStatusListener {
                override fun onSuccess(statusValue: Int, statusName: String) {
                    addLogI("getDeviceWatchFace statusValue=$statusValue statusName=$statusName")

                    if (statusValue == DeviceWatchFaceFileStatusListener.PrepareStatus.READY.state) {
                        updateTimeoutTask()
                        SifliWatchfaceService.startActionWatchface(this@SifliDialActivity, ordinaryFile.path, MyConstants.deviceAddress, 0)
                    }
                }

                override fun timeOut() {
                    addLogE("getDeviceWatchFace timeOut")
                }
            })
        }
    }

    private fun initCallback() {
        CallBackUtils.watchFaceInstallCallBack = WatchFaceInstallCallBack { bean ->
            addLogBean("watchFaceInstallCallBack", bean)
        }
    }

    private fun updateTimeoutTask() {
        if (timeoutTask != null) {
            ThreadUtils.cancel(timeoutTask)
        }
        timeoutTask = TimeoutTask()
        ThreadUtils.executeByIo(timeoutTask)
    }

    private inner class TimeoutTask : ThreadUtils.SimpleTask<Int>() {
        var i = 0
        var isOk = false

        fun finish(isOk: Boolean) {
            oldProgress = -1
            this.isOk = isOk
            i = 30
        }

        override fun doInBackground(): Int {
            while (i <= 30) {
                i++
                Thread.sleep(1000)
            }
            return 0
        }

        override fun onSuccess(result: Int?) {
            addLogI("TimeoutTask onSuccess result=$result isOk=$isOk")
        }
    }

    inner class DfuReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    SifliWatchfaceService.BROADCAST_WATCHFACE_STATE -> {
                        val state = intent.getIntExtra(SifliWatchfaceService.EXTRA_WATCHFACE_STATE, -1)
                        val rsp = intent.getIntExtra(SifliWatchfaceService.EXTRA_WATCHFACE_STATE_RSP, 0)
                        addLogI("onReceive BROADCAST_WATCHFACE_STATE state=$state rsp=$rsp")

                        if (state == 0) {
                            timeoutTask?.finish(true)
                        }
                        else {
                            timeoutTask?.finish(false)
                            if (state == 2 || rsp == 37) {
                                addLogI(getString(R.string.device_out_memory))
                            }
                        }
                    }

                    SifliWatchfaceService.BROADCAST_WATCHFACE_PROGRESS -> {
                        val progress = intent.getIntExtra(SifliWatchfaceService.EXTRA_WATCHFACE_PROGRESS, 0)
                        addLogI("onReceive BROADCAST_WATCHFACE_PROGRESS progress=$progress")
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