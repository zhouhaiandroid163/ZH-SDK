package com.zjw.sdkdemo.function.apricot

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.PermissionUtils
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.ESIMBigdataBean
import com.zhapp.ble.bean.EsimCommonDataBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.EsimEidSettingsCallBack
import com.zhapp.ble.callback.EsimHttpDataCallBack
import com.zhapp.ble.callback.UploadBigDataListener
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityEsimBinding
import com.zjw.sdkdemo.manager.ESimHttpRequestManager
import java.net.MalformedURLException

class ESimActivity : BaseActivity() {
    private val binding: ActivityEsimBinding by lazy { ActivityEsimBinding.inflate(layoutInflater) }
    private val tag: String = ESimActivity::class.java.simpleName

    private var esimCommonData: EsimCommonDataBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_esim)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initListener()
        initCallback()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnScanQr) {
            addLogI("btnScanQr")
            val permissionGroupCamera = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.CAMERA
                    )
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.CAMERA
                    )
                }

                else -> {
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    )
                }
            }
            if (!PermissionUtils.isGranted(*permissionGroupCamera)) {
                PermissionUtils.permission(*permissionGroupCamera).callback { isAllGranted, granted, deniedForever, denied -> findViewById<View>(R.id.btnScanQr).callOnClick() }.request()
            } else {
                val options = HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE, HmsScan.DATAMATRIX_SCAN_TYPE).setViewType(1).create()
                ScanUtil.startScan(this, 0, options)
            }
        }

        clickCheckConnect(binding.sendActivationCode) {
            addLogI("sendActivationCode")
            val bean = ESIMBigdataBean()
            bean.esimActivationCodeData = binding.etActivationCode.text.toString()
            addLogBean("setEsimBigdataSettings", bean)
            ControlBleTools.getInstance().setEsimBigdataSettings(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setEsimBigdataSettings state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnGetEsimEidSettings) {
            addLogI("btnGetEsimEidSettings")
            addLogI("getEsimEidSettings")
            ControlBleTools.getInstance().getEsimEidSettings(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getEsimEidSettings state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnGetEsimCommonDataSettings) {
            addLogI("btnGetEsimCommonDataSettings")
            addLogI("getEsimCommonDataSettings")
            ControlBleTools.getInstance().getEsimCommonDataSettings(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getEsimCommonDataSettings state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnDeleteEsimData) {
            addLogI("btnDeleteEsimData")
            if (esimCommonData == null) {
                addLogI("esimCommonData is null")
                return@clickCheckConnect
            }
            val id = esimCommonData!!.esimId
            addLogI("delEsimData id=$id")
            ControlBleTools.getInstance().delEsimData(id, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("delEsimData state=$state")
                }
            })
        }


    }

    private fun initCallback() {
        CallBackUtils.esimHttpDataCallBack = EsimHttpDataCallBack { bean ->
            addLogBean("esimHttpDataCallBack", bean)

            val httpUtil = ESimHttpRequestManager(mHttpResponseListener, this)
            try {
                httpUtil.callHttpRequest(bean!!.esimHttpUrl, "", bean.esimHttpData, 8.toByte())
            } catch (e: MalformedURLException) {
                throw RuntimeException(e)
            }
        }

        CallBackUtils.esimEidSettingsCallBack = object : EsimEidSettingsCallBack {
            override fun onEsimEidSettings(eid: String?) {
                addLogI("esimEidSettingsCallBack onEsimEidSettings eid=$eid")
            }

            override fun onEsimCommonDataSetting(bean: EsimCommonDataBean) {
                addLogBean("esimEidSettingsCallBack onEsimCommonDataSetting", bean)
                esimCommonData = bean
            }
        }
    }

    private val mHttpResponseListener: ESimHttpRequestManager.HttpResponseListener = object : ESimHttpRequestManager.HttpResponseListener {
        override fun onSuccess(responseBodyInfo: String, code: Int) {
            addLogBean("mHttpResponseListener onSuccess code=$code", responseBodyInfo)

            if (responseBodyInfo.contains("boundProfilePackage")) {

                val type = BleCommonAttributes.UPLOAD_BIG_DATA_ESIM
                val fileByte = responseBodyInfo.encodeToByteArray()
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
            } else {
                val bean = ESIMBigdataBean()
                bean.esimHttpData = responseBodyInfo
                bean.esimHttpCode = code
                addLogBean("setEsimBigdataSettings", bean)
                ControlBleTools.getInstance().setEsimBigdataSettings(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState) {
                        addLogI("setEsimBigdataSettings state=$state")
                    }
                })

                if (!responseBodyInfo.contains("Executed-Success")) {
                    addLogI("Executed-Success")
                }
            }
        }

        override fun onFailed(errorInfo: String, code: Int) {
            addLogI("HttpResponseListener onFailed errorInfo=$errorInfo code=$code")
            val bean = ESIMBigdataBean()
            bean.esimHttpData = errorInfo
            bean.esimHttpCode = code
            addLogBean("setEsimBigdataSettings", bean)
            ControlBleTools.getInstance().setEsimBigdataSettings(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setEsimBigdataSettings state=$state")
                }
            })
        }
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) {
            return
        }
        if (requestCode == 0) {
            val errorCode: Int = data.getIntExtra(ScanUtil.RESULT_CODE, ScanUtil.SUCCESS)
            if (errorCode == ScanUtil.SUCCESS) {
                val obj: Any? = data.getParcelableExtra(ScanUtil.RESULT)
                addLogI("onActivityResult obj=$obj")
                if (obj != null) {
                    binding.etActivationCode.setText(obj.toString())
                    binding.etActivationCode.setSelection(obj.toString().length)
                }
            }
            if (errorCode == ScanUtil.ERROR_NO_READ_PERMISSION) {
                addLogI("ERROR_NO_READ_PERMISSION onSuccess")
            }
        }
    }
}