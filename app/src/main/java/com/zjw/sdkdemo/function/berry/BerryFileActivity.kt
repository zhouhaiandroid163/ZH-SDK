package com.zjw.sdkdemo.function.berry

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
import com.zjw.sdkdemo.databinding.ActivityBerryFileBinding
import com.zjw.sdkdemo.function.MainActivity.GlobalData
import com.zjw.sdkdemo.utils.DialogUtils
import com.zjw.sdkdemo.utils.ToastUtils
import java.io.File

class BerryFileActivity : BaseActivity() {
    val binding by lazy { ActivityBerryFileBinding.inflate(layoutInflater) }
    private val tag: String = BerryFileActivity::class.java.simpleName


    private lateinit var otaFile: File
    private val otaFilePath = PathUtils.getExternalAppCachePath() + "/berry_ota"

    private lateinit var agpsFile: File
    private val agpsFilePath = PathUtils.getExternalAppCachePath() + "/berry_agps"

    private var agpsGR3File:File?=null
    private var agpsGALFile:File?=null
    private var agpsBDSFile:File?=null
    private var isLodaAgps = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_file_berry)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallback()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        addLogI("disconnect")
        ControlBleTools.getInstance().disconnect()
        CallBackUtils.agpsCallBack = null
    }

    private fun initData() {
        FileUtils.createOrExistsDir(otaFilePath)
        binding.layoutBerryFileOta.layoutSelectFile.tvTip.text = getString(R.string.select_file_attention, otaFilePath)

        FileUtils.createOrExistsDir(agpsFilePath)
        binding.layoutBerryFileAGPS.layoutSelectFile.tvTip.text = getString(R.string.select_file_attention, agpsFilePath)
        binding.layoutBerryFileLodaAGPS.layoutSelectFile1.tvTip.text = "ELPO_GR3_1\n" + getString(R.string.select_file_attention, agpsFilePath)
        binding.layoutBerryFileLodaAGPS.layoutSelectFile2.tvTip.text = "ELPO_GAL_3\n" +getString(R.string.select_file_attention, agpsFilePath)
        binding.layoutBerryFileLodaAGPS.layoutSelectFile3.tvTip.text = "ELPO_BDS_3\n" +getString(R.string.select_file_attention, agpsFilePath)

    }

    private fun initView() {
        setMyCheckBox(binding.layoutBerryFileOta.cbTop, binding.layoutBerryFileOta.llBottom, binding.layoutBerryFileOta.ivHelp)
        setMyCheckBox(binding.layoutBerryFileAGPS.cbTop, binding.layoutBerryFileAGPS.llBottom, binding.layoutBerryFileAGPS.ivHelp)
        setMyCheckBox(binding.layoutBerryFileLodaAGPS.cbTop, binding.layoutBerryFileLodaAGPS.llBottom, binding.layoutBerryFileLodaAGPS.ivHelp)
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutBerryFileOta.layoutSelectFile.btnSelectFile) {
            addLogI("layoutSelectFile.btnSelectFile")
            DialogUtils.showSelectFileDialog(this, otaFilePath, ".bin") { selectedFile ->
                otaFile = selectedFile
                binding.layoutBerryFileOta.layoutSelectFile.tvFileName.text = otaFile.name
            }
        }

        clickCheckConnect(binding.layoutBerryFileOta.btnSend) {
            addLogI("layoutBerryFileOta.btnSend")
            if (GlobalData.deviceInfo == null) {
                addLogI("deviceInfo is null")
                return@clickCheckConnect
            }
            if (!::otaFile.isInitialized) {
                addLogI(getString(R.string.select_file_tip))
                return@clickCheckConnect
            }

            val fileByte = FileIOUtils.readFile2BytesByStream(otaFile)
            val fileType = BleCommonAttributes.UPLOAD_BIG_DATA_OTA
            val deviceNum = GlobalData.deviceInfo!!.equipmentNumber
            val deviceVer = GlobalData.deviceInfo!!.firmwareVersion

            addLogI("getDeviceLargeFileStateByBerry fileByte=${fileByte.size} fileType=$fileType deviceNum=$deviceNum deviceVer=$deviceVer")
            ControlBleTools.getInstance().getDeviceLargeFileStateByBerry(fileByte, fileType, deviceNum, deviceVer, object : DeviceLargeFileStatusListener {

                override fun onSuccess(statusValue: Int, statusName: String?) {
                    addLogI("getDeviceLargeFileStateByBerry onSuccess statusValue=$statusValue statusName=$statusName")

                    if (statusValue == DeviceLargeFileStatusListener.PrepareStatus.READY.state) {

                        val type = BleCommonAttributes.UPLOAD_BIG_DATA_OTA
                        addLogI("startUploadBigDataByBerry type=$type fileByte=${fileByte.size}")
                        ControlBleTools.getInstance().startUploadBigDataByBerry(type, fileByte, object : UploadBigDataListener {
                            override fun onSuccess() {
                                addLogI("startUploadBigDataByBerry onSuccess()")
                            }

                            override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                                val percentage = (curPiece * 100 / dataPackTotalPieceLength)
                                addLogI("startUploadBigDataByBerry onProgress curPiece=$curPiece dataPackTotalPieceLength=$dataPackTotalPieceLength  percentage=$percentage")
                            }

                            override fun onTimeout(msg: String?) {
                                addLogE("startUploadBigDataByBerry onTimeout msg=$msg")
                            }
                        })
                    } else {
                        addLogI("startUploadBigDataByBerry onSuccess error")
                    }
                }

                override fun timeOut() {
                    addLogE("getDeviceLargeFileStateByBerry timeOut")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryFileAGPS.layoutSelectFile.btnSelectFile) {
            addLogI("layoutSelectFile.btnSelectFile")
            DialogUtils.showSelectFileDialog(this, agpsFilePath, "brm") { selectedFile ->
                agpsFile = selectedFile
                binding.layoutBerryFileAGPS.layoutSelectFile.tvFileName.text = otaFile.name
            }
        }

        clickCheckConnect(binding.layoutBerryFileAGPS.btnSend) {
            addLogI("layoutBerryFileAGPS.btnSend")
            addLogI("requestAgpsState")
            isLodaAgps = false
            ControlBleTools.getInstance().requestAgpsState(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("requestAgpsState state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryFileLodaAGPS.layoutSelectFile1.btnSelectFile) {
            addLogI("layoutBerryFileLodaAGPS.btnSelectFile")
            DialogUtils.showSelectFileDialog(this, agpsFilePath, "") { selectedFile ->
                agpsGR3File = selectedFile
                binding.layoutBerryFileLodaAGPS.layoutSelectFile1.tvFileName.text = agpsGR3File?.name?:""
            }
        }

        clickCheckConnect(binding.layoutBerryFileLodaAGPS.layoutSelectFile2.btnSelectFile) {
            addLogI("layoutBerryFileLodaAGPS.btnSelectFile")
            DialogUtils.showSelectFileDialog(this, agpsFilePath, "") { selectedFile ->
                agpsGALFile = selectedFile
                binding.layoutBerryFileLodaAGPS.layoutSelectFile2.tvFileName.text = agpsGALFile?.name?:""
            }
        }

        clickCheckConnect(binding.layoutBerryFileLodaAGPS.layoutSelectFile3.btnSelectFile) {
            addLogI("layoutBerryFileLodaAGPS.btnSelectFile")
            DialogUtils.showSelectFileDialog(this, agpsFilePath, "") { selectedFile ->
                agpsBDSFile = selectedFile
                binding.layoutBerryFileLodaAGPS.layoutSelectFile3.tvFileName.text = agpsBDSFile?.name?:""
            }
        }

        clickCheckConnect(binding.layoutBerryFileLodaAGPS.btnSend) {
            addLogI("layoutBerryFileLodaAGPS.btnSend")
            addLogI("requestAgpsState")
            isLodaAgps = true
            ControlBleTools.getInstance().requestAgpsState(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("requestAgpsState state=$state")
                }
            })
        }
    }

    private fun initCallback() {
        CallBackUtils.agpsCallBack = AgpsCallBack { bean ->
            addLogBean("agpsCallBack",bean)
            addLogI("isNeed=${bean.isNeed}")
            if (bean.isNeed) {
                if (GlobalData.deviceInfo == null) {
                    addLogI("deviceInfo is null")
                    return@AgpsCallBack
                }
                //LUODA = 0x04;
                if(isLodaAgps && bean.type == 4){
                    sendlodaAgps()
                    return@AgpsCallBack
                }
                val fileByte = FileIOUtils.readFile2BytesByStream(agpsFile)
                val fileType = BleCommonAttributes.UPLOAD_BIG_DATA_LTO
                val deviceNum = GlobalData.deviceInfo!!.equipmentNumber
                val deviceVer = GlobalData.deviceInfo!!.firmwareVersion

                addLogI("getDeviceLargeFileStateByBerry fileByte=${fileByte.size} fileType=$fileType deviceNum=$deviceNum deviceVer=$deviceVer")
                ControlBleTools.getInstance().getDeviceLargeFileStateByBerry(fileByte, fileType, deviceNum, deviceVer, object : DeviceLargeFileStatusListener {
                    override fun onSuccess(statusValue: Int, statusName: String?) {
                        addLogI("getDeviceLargeFileStateByBerry onSuccess statusValue=$statusValue statusName=$statusName")

                        if (statusValue == DeviceLargeFileStatusListener.PrepareStatus.READY.state) {

                            val type = BleCommonAttributes.UPLOAD_BIG_DATA_LTO
                            addLogI("startUploadBigDataByBerry type=$type fileByte=${fileByte.size}")
                            ControlBleTools.getInstance().startUploadBigDataByBerry(type, fileByte, object : UploadBigDataListener {
                                override fun onSuccess() {
                                    addLogI("startUploadBigDataByBerry onSuccess()")
                                }

                                override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                                    val percentage = (curPiece * 100 / dataPackTotalPieceLength)
                                    addLogI("startUploadBigDataByBerry onProgress curPiece=$curPiece dataPackTotalPieceLength=$dataPackTotalPieceLength  percentage=$percentage")
                                }

                                override fun onTimeout(msg: String?) {
                                    addLogE("startUploadBigDataByBerry onTimeout msg=$msg")
                                }
                            })
                        }
                    }

                    override fun timeOut() {
                        addLogE("getDeviceLargeFileStateByBerry timeOut")
                    }
                })
            }

        }
    }

    private fun sendlodaAgps() {
        if(agpsGR3File == null || agpsGALFile == null || agpsBDSFile == null){
            ToastUtils.showToast(R.string.select_file_tip)
            return
        }
        ControlBleTools.getInstance().sendLuodaAgpsFiles(
            FileIOUtils.readFile2BytesByStream(agpsGR3File),
            FileIOUtils.readFile2BytesByStream(agpsGALFile),
            FileIOUtils.readFile2BytesByStream(agpsBDSFile),
            GlobalData.deviceInfo!!.equipmentNumber,object : UploadBigDataListener {
                override fun onSuccess() {
                    addLogI("startUploadBigDataByBerry onSuccess()")
                }

                override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                    val percentage = (curPiece * 100 / dataPackTotalPieceLength)
                    addLogI("startUploadBigDataByBerry onProgress curPiece=$curPiece dataPackTotalPieceLength=$dataPackTotalPieceLength  percentage=$percentage")
                }

                override fun onTimeout(msg: String?) {
                    addLogE("startUploadBigDataByBerry onTimeout msg=$msg")
                }
            }
            )
    }

}