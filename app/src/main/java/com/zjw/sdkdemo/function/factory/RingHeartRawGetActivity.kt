package com.zjw.sdkdemo.function.factory

import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.UriUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.HeartRateLeakageRawBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.HeartRateLeakageRawCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityRingHeartRawGetBinding

class RingHeartRawGetActivity : BaseActivity() {
    private val binding by lazy { ActivityRingHeartRawGetBinding.inflate(layoutInflater) }
    private val tag: String = RingHeartRawGetActivity::class.java.simpleName

    private val rawData = mutableListOf<HeartRateLeakageRawBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_ring_heart_raw_get)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initListener()
        initCallBack()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnRingTestStart) {
            addLogI("btnRingTestStart")
            ControlBleTools.getInstance().setHeartRateRawSwitch(true, object : SendCmdStateListener(lifecycle) {
                override fun onState(state: SendCmdState) {
                    addLogI("setHeartRateRawSwitch state=$state")

                }
            })
        }
        clickCheckConnect(binding.btnRingTestStop) {
            addLogI("btnRingTestStop")
            ControlBleTools.getInstance().setHeartRateRawSwitch(false, object : SendCmdStateListener(lifecycle) {
                override fun onState(state: SendCmdState) {
                    addLogI("setHeartRateRawSwitch state=$state")
                }
            })
        }
        clickCheckConnect(binding.btnRingTestShare) {
            addLogI("btnRingTestShare")
            if (rawData.isEmpty()) {
                addLogI("rawData=null")
                return@clickCheckConnect
            }
            var validNum = 0
            for (item in rawData) {
                if (!item.toCsvContent().isNullOrEmpty()) {
                    validNum++
                }
            }
            if (validNum == 0) {
                addLogI("validNum=0")
            }

            val csvDir = PathUtils.getAppDataPathExternalFirst() + "/csv"
            FileUtils.createOrExistsDir(csvDir)
            val csvFilePath = csvDir + "/raw_" + System.currentTimeMillis() + ".csv"
            FileIOUtils.writeFileFromString(csvFilePath, HeartRateLeakageRawBean.getHeadStr(), true)
            for (item in rawData) {
                if (!item.toCsvContent().isNullOrEmpty()) {
                    FileIOUtils.writeFileFromString(csvFilePath, item.toCsvContent(), true)
                }
            }
            shareFile(csvFilePath)
        }
    }

    private fun initCallBack() {
        CallBackUtils.heartRateLeakageRawCallBack = HeartRateLeakageRawCallBack { bean ->
            addLogBean( "heartRateLeakageRawCallBack",bean)
            rawData.add(bean)
        }
    }

    private fun shareFile(filePath: String) {
        addLogI("filePath=$filePath")
        val zipFile = FileUtils.getFileByPath(filePath)
        var intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/csv"
        val uri = UriUtils.file2Uri(zipFile)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.btn_share))
        intent = Intent.createChooser(intent, getString(R.string.btn_share))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }
}