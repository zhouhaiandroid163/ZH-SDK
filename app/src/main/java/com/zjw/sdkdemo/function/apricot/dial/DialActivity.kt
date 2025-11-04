package com.zjw.sdkdemo.function.apricot.dial

import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.WatchFaceListBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceLargeFileStatusListener
import com.zhapp.ble.callback.DeviceWatchFaceFileStatusListener
import com.zhapp.ble.callback.UploadBigDataListener
import com.zhapp.ble.callback.WatchFaceInstallCallBack
import com.zhapp.ble.callback.WatchFaceListCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityDialBinding
import com.zjw.sdkdemo.utils.DialogUtils
import java.io.File

class DialActivity : BaseActivity() {
    private val binding by lazy { ActivityDialBinding.inflate(layoutInflater) }
    private val tag: String = DialActivity::class.java.simpleName

    private val ordinaryFilePath = PathUtils.getExternalAppCachePath() + "/ordinary_dial"
    private lateinit var ordinaryFile: File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_dial)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallBack()
        initData()
    }

    private fun initData() {
        FileUtils.createOrExistsDir(ordinaryFilePath)
        binding.layoutDialOrdinary.layoutSelectFile.tvTip.text = getString(R.string.select_file_attention, ordinaryFilePath)
    }

    private fun initView() {
        setMyCheckBox(binding.layoutDialManage.cbTop, binding.layoutDialManage.llBottom, binding.layoutDialManage.ivHelp)
        setMyCheckBox(binding.layoutDialOrdinary.cbTop, binding.layoutDialOrdinary.llBottom, binding.layoutDialOrdinary.ivHelp)
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutDialManage.btnGet) {
            addLogI("layoutDialManage.btnGet")
            addLogI("getWatchFaceList")
            ControlBleTools.getInstance().getWatchFaceList(object : WatchFaceListCallBack {
                override fun onResponse(list: MutableList<WatchFaceListBean?>) {
                    addLogBean("getWatchFaceList onResponse", list)
                }

                override fun timeOut(errorState: SendCmdState?) {
                    addLogI("getWatchFaceList timeOut errorState=$errorState")
                }
            })
        }

        clickCheckConnect(binding.layoutDialManage.btnSet) {
            addLogI("layoutDialManage.btnSet")
            val id = binding.layoutDialManage.etId.text.toString()
            addLogI("setDeviceWatchFromId id=$id")
            ControlBleTools.getInstance().setDeviceWatchFromId(id, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setDeviceWatchFromId state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutDialManage.btnDelete) {
            addLogI("layoutDialManage.btnDelete")
            val id = binding.layoutDialManage.etId.text.toString()
            addLogI("deleteDeviceWatchFromId id=$id")
            ControlBleTools.getInstance().deleteDeviceWatchFromId(id, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("deleteDeviceWatchFromId state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutDialOrdinary.layoutSelectFile.btnSelectFile) {
            addLogI("layoutDialOrdinary.layoutSelectFile.btnSelectFile")
            DialogUtils.showSelectFileDialog(this, ordinaryFilePath, ".bin") { selectedFile ->
                ordinaryFile = selectedFile
                binding.layoutDialOrdinary.layoutSelectFile.tvFileName.text = selectedFile.name
            }
        }

        clickCheckConnect(binding.layoutDialOrdinary.btnSend) {
            addLogI("layoutDialOrdinary.btnSend")
            if (!::ordinaryFile.isInitialized) {
                addLogI(getString(R.string.select_file_tip))
                return@clickCheckConnect
            }
            val dialData = FileIOUtils.readFile2BytesByStream(ordinaryFile)
            val dialId = "dialId"
            val dialSize = dialData.size
            val isReplace = true
            addLogI("getDeviceWatchFace dialId=$dialId dialSize=$dialSize isReplace=$isReplace")
            ControlBleTools.getInstance().getDeviceWatchFace(dialId, dialSize, isReplace, object : DeviceWatchFaceFileStatusListener {
                override fun onSuccess(statusValue: Int, statusName: String?) {
                    addLogI("getDeviceWatchFace statusValue=$statusValue statusName=$statusName")

                    if (statusValue == DeviceLargeFileStatusListener.PrepareStatus.READY.state) {

                        val type = BleCommonAttributes.UPLOAD_BIG_DATA_WATCH
                        addLogI("startUploadBigData type=$type dialData=${dialData.size} isReplace=$isReplace")
                        ControlBleTools.getInstance().startUploadBigData(type, dialData, isReplace, object : UploadBigDataListener {
                            override fun onSuccess() {
                                addLogI("startUploadBigData onSuccess")
                            }

                            override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                                val percentage = (curPiece * 100 / dataPackTotalPieceLength)
                                addLogI("startUploadBigData onProgress curPiece=$curPiece dataPackTotalPieceLength=$dataPackTotalPieceLength  percentage=$percentage")
                            }

                            override fun onTimeout(msg: String?) {
                                addLogE("startUploadBigData onTimeout msg=$msg")
                            }
                        })
                    }
                }

                override fun timeOut() {
                    addLogE("getDeviceWatchFace timeOut")
                }
            })
        }

        clickCheckConnect(binding.btnDialPhoto) {
            addLogI("btnDialPhoto")
            startActivity(Intent(this, PhotoDialActivity::class.java))
        }

        clickCheckConnect(binding.btnClockDialDiyV1Demo) {
            addLogI("btnClockDialDiyV1Demo")
            startActivity(Intent(this, DiyDialV1Activity::class.java))
        }

        clickCheckConnect(binding.btnClockDialDiyV2) {
            addLogI("btnClockDialDiyV2")
            startActivity(Intent(this, DiyDialV2SimpleActivity::class.java))
        }

        clickCheckConnect(binding.btnClockDialDiyV2DemoNumber) {
            addLogI("btnClockDialDiyV2DemoNumber")
            startActivity(Intent(this, DiyDialV2Activity::class.java).apply { putExtra(DiyDialV2Activity.DIY_TYPE_TAG, 1) })
        }

        clickCheckConnect(binding.btnClockDialDiyV2DemoPointer) {
            addLogI("btnClockDialDiyV2DemoPointer")
            startActivity(Intent(this, DiyDialV2Activity::class.java).apply { putExtra(DiyDialV2Activity.DIY_TYPE_TAG, 2) })
        }
    }

    private fun initCallBack()
    {
        CallBackUtils.watchFaceInstallCallBack = WatchFaceInstallCallBack { bean ->
            addLogBean("watchFaceInstallCallBack", bean)
        }
    }
}