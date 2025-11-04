package com.zjw.sdkdemo.function.apricot.sifli

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.ThreadUtils
import com.sifli.watchfacelibraryzh.SifliWatchfaceService
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceWatchFaceFileStatusListener
import com.zhapp.ble.callback.WatchFaceInstallCallBack
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivitySifliPhotoDialBinding
import com.zjw.sdkdemo.utils.AssetUtils
import com.zjw.sdkdemo.utils.MyConstants
import com.zjw.sdkdemo.utils.MyFileUtils

class SifliPhotoActivity : BaseActivity() {
    private val binding by lazy { ActivitySifliPhotoDialBinding.inflate(layoutInflater) }
    private val tag: String = SifliPhotoActivity::class.java.simpleName

    private var dfuReceiver: DfuReceiver? = null
    private var timeoutTask: TimeoutTask? = null
    private var oldProgress = -1

    private var colorR = 255
    private var colorG = 255
    private var colorB = 255

    private var bgBitmap: Bitmap? = null
    private var textBitmap: Bitmap? = null
    private var sourceData: ByteArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.sifil_photo)
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
        registerReceiver()
        sourceData = AssetUtils.getAssetBytes(this, AssetUtils.ASS_APRICOT_SIFLI_PHOTO_FOLDER + "Source.bin")
        bgBitmap = AssetUtils.getAssetBitmap(this, AssetUtils.ASS_APRICOT_SIFLI_PHOTO_FOLDER + "Bg.png")
        textBitmap = AssetUtils.getAssetBitmap(this, AssetUtils.ASS_APRICOT_SIFLI_PHOTO_FOLDER + "Text.png")
        updateUi(255, 255, 255)
    }

    private fun initListener() {
        clickCheckConnect(binding.ivColor1) {
            addLogI("btnTextColor1")
            updateUi(51, 153, 255)
        }
        clickCheckConnect(binding.ivColor2) {
            addLogI("btnTextColor2")
            updateUi(255, 153, 51)
        }
        clickCheckConnect(binding.ivColor3) {
            addLogI("btnTextColor3")
            updateUi(51, 255, 153)
        }

        clickCheckConnect(binding.btnSend) {
            addLogI("btnSend")
            ControlBleTools.getInstance().newCustomClockDialData(
                sourceData, colorR, colorG, colorB, bgBitmap, textBitmap, { data ->
                    val dialFile = MyFileUtils.saveSifliPhotoCacheFile(data)
                    val dialData = FileIOUtils.readFile2BytesByStream(dialFile)
                    val dialId = "dialId"
                    val dialSize = dialData.size
                    val isReplace = true
                    addLogI("getDeviceWatchFace dialId=$dialId dialSize=$dialSize")
                    ControlBleTools.getInstance().getDeviceWatchFace(dialId, dialSize, isReplace, object : DeviceWatchFaceFileStatusListener {
                        override fun onSuccess(statusValue: Int, statusName: String) {
                            addLogI("getDeviceWatchFace statusValue=$statusValue statusName=$statusName")
                            if (statusValue == DeviceWatchFaceFileStatusListener.PrepareStatus.READY.state) {
                                updateTimeoutTask()
                                SifliWatchfaceService.startActionWatchface(this@SifliPhotoActivity, dialFile.path, MyConstants.deviceAddress, 0, 1)
                            }
                        }

                        override fun timeOut() {
                            addLogE("getDeviceWatchFace timeOut")
                        }
                    })

                }, true
            )
        }
    }

    private fun initCallback() {
        CallBackUtils.watchFaceInstallCallBack = WatchFaceInstallCallBack { bean ->
            addLogBean( "watchFaceInstallCallBack",bean)
        }
    }

    private fun updateUi(colorR: Int, colorG: Int, colorB: Int) {
        this.colorR = colorR
        this.colorG = colorG
        this.colorB = colorB
        ControlBleTools.getInstance().myCustomClockUtils(sourceData, colorR, colorG, colorB, bgBitmap, textBitmap) { result: Bitmap? ->
            binding.ivEffect.setImageBitmap(result)
        }
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
                    SifliWatchfaceService.BROADCAST_WATCHFACE_STATE -> {
                        val state = intent.getIntExtra(SifliWatchfaceService.EXTRA_WATCHFACE_STATE, -1)
                        val rsp = intent.getIntExtra(SifliWatchfaceService.EXTRA_WATCHFACE_STATE_RSP, 0)
                        addLogI("onReceive BROADCAST_WATCHFACE_STATE state=$state rsp=$rsp")

                        if (state == 0) {
                            timeoutTask?.finish(true)
                        } else {
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