package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.callback.AgpsCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceLargeFileStatusListener
import com.zhapp.ble.callback.UploadBigDataListener
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityFileBinding
import com.zjw.sdkdemo.utils.DialogUtils
import java.io.File

class FileActivity : BaseActivity() {
    private val binding by lazy { ActivityFileBinding.inflate(layoutInflater) }
    private val tag: String = FileActivity::class.java.simpleName

    private lateinit var otaFile: File
    private val otaFilePath = PathUtils.getExternalAppCachePath() + "/ota"

    private lateinit var agpsFile: File
    private val agpsFilePath = PathUtils.getExternalAppCachePath() + "/agps"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_file)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallback()
        initData()
    }

    private fun initData() {
        FileUtils.createOrExistsDir(otaFilePath)
        binding.layoutFileOta.layoutSelectFile.tvTip.text = getString(R.string.select_file_attention, otaFilePath)

        FileUtils.createOrExistsDir(agpsFilePath)
        binding.layoutFileAGPS.layoutSelectFile.tvTip.text = getString(R.string.select_file_attention, agpsFilePath)
    }

    private fun initView() {
        setMyCheckBox(binding.layoutFileOta.cbTop, binding.layoutFileOta.llBottom, binding.layoutFileOta.ivHelp)
        setMyCheckBox(binding.layoutFileAGPS.cbTop, binding.layoutFileAGPS.llBottom, binding.layoutFileAGPS.ivHelp)
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutFileOta.layoutSelectFile.btnSelectFile) {
            addLogI("layoutSelectFile.btnSelectFile")
            DialogUtils.showSelectFileDialog(this, otaFilePath, ".bin") { selectedFile ->
                otaFile = selectedFile
                binding.layoutFileOta.layoutSelectFile.tvFileName.text = otaFile.name
            }
        }

        clickCheckConnect(binding.layoutFileOta.btnSend) {
            addLogI("layoutFileOta.btnSend")
            if (!::otaFile.isInitialized) {
                addLogI(getString(R.string.select_file_tip))
                return@clickCheckConnect
            }

            val isForce = true
            val version = "443"
            val md5 = "1305828"
            val fileByte = FileIOUtils.readFile2BytesByStream(otaFile)
            addLogI("getDeviceLargeFileState isForce=$isForce version=$version md5=$md5")
            ControlBleTools.getInstance().getDeviceLargeFileState(isForce, version, md5, object : DeviceLargeFileStatusListener {
                override fun onSuccess(statusValue: Int, statusName: String) {
                    addLogI("getDeviceLargeFileState onSuccess statusValue=$statusValue statusName=$statusName")

                    if (statusValue == DeviceLargeFileStatusListener.PrepareStatus.READY.state) {

                        val type = BleCommonAttributes.UPLOAD_BIG_DATA_OTA
                        val resumable = true
                        addLogI("startUploadBigData type=$type fileByte=${fileByte.size} resumable=$resumable")
                        ControlBleTools.getInstance().startUploadBigData(type, fileByte, resumable, object : UploadBigDataListener {
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
                    addLogE("getDeviceLargeFileState timeOut")
                }
            })
        }

        clickCheckConnect(binding.layoutFileAGPS.layoutSelectFile.btnSelectFile) {
            addLogI("layoutSelectFile.btnSelectFile")
            DialogUtils.showSelectFileDialog(this, otaFilePath, "brm") { selectedFile ->
                agpsFile = selectedFile
                binding.layoutFileAGPS.layoutSelectFile.tvFileName.text = agpsFile.name
            }
        }

        clickCheckConnect(binding.layoutFileAGPS.btnSend) {
            addLogI("layoutFileAGPS.btnSend")
            addLogI("requestAgpsState")
            ControlBleTools.getInstance().requestAgpsState(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("requestAgpsState state=$state")
                }
            })
        }
    }


    private fun initCallback() {
        CallBackUtils.agpsCallBack = AgpsCallBack { bean ->
            addLogBean("agpsCallBack onSuccess", bean)
            addLogI("isNeed=${bean.isNeed}")

            if (bean.isNeed) {
                if (!::agpsFile.isInitialized) {
                    addLogI(getString(R.string.select_file_tip))
                    return@AgpsCallBack
                }
                val isForce = true
                val version = "443"
                val md5 = "1305828"
                val fileByte = FileIOUtils.readFile2BytesByStream(agpsFile)
                addLogI("getDeviceLargeFileState isForce=$isForce version=$version md5=$md5")
                ControlBleTools.getInstance().getDeviceLargeFileState(isForce, version, md5, object : DeviceLargeFileStatusListener {
                    override fun onSuccess(statusValue: Int, statusName: String) {
                        addLogI("getDeviceLargeFileState onSuccess statusValue=$statusValue statusName=$statusName")

                        if (statusValue == DeviceLargeFileStatusListener.PrepareStatus.READY.state) {

                            val type = BleCommonAttributes.UPLOAD_BIG_DATA_LTO
                            val isReplace = true
                            addLogI("startUploadBigData type=$type fileByte=${fileByte.size} isReplace=$isReplace")
                            ControlBleTools.getInstance().startUploadBigData(type, fileByte, isReplace, object : UploadBigDataListener {
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
                        addLogE("getDeviceLargeFileState timeOut")
                    }
                })
            }
        }
    }
}