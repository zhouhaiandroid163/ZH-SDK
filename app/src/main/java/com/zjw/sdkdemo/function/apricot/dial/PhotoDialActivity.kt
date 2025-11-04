package com.zjw.sdkdemo.function.apricot.dial

import android.graphics.Bitmap
import android.os.Bundle
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.callback.DeviceLargeFileStatusListener
import com.zhapp.ble.callback.DeviceWatchFaceFileStatusListener
import com.zhapp.ble.callback.UploadBigDataListener
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityPhotoDialBinding
import com.zjw.sdkdemo.utils.AssetUtils

class PhotoDialActivity : BaseActivity() {
    private val binding by lazy { ActivityPhotoDialBinding.inflate(layoutInflater) }
    private val tag: String = PhotoDialActivity::class.java.simpleName

    private var colorR = 255
    private var colorG = 255
    private var colorB = 255

    private var bgBitmap: Bitmap? = null
    private var textBitmap: Bitmap? = null
    private var sourceData: ByteArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.clock_dial_photo)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initListener()
        intData()
    }

    private fun intData() {
        binding.layoutTip.tvTip.text = getString(R.string.select_file_assets_attention, AssetUtils.ASS_APRICOT_DIAL_PHOTO_FOLDER)
        sourceData = AssetUtils.getAssetBytes(this, AssetUtils.ASS_APRICOT_DIAL_PHOTO_FOLDER + "Source.bin")
        bgBitmap = AssetUtils.getAssetBitmap(this, AssetUtils.ASS_APRICOT_DIAL_PHOTO_FOLDER + "Bg.png")
        textBitmap = AssetUtils.getAssetBitmap(this, AssetUtils.ASS_APRICOT_DIAL_PHOTO_FOLDER + "Text.png")
        updateUi(255, 255, 255)
    }

    private fun initListener() {
        clickCheckConnect(binding.btnTextColor1) {
            addLogI("btnTextColor1")
            updateUi(51, 153, 255)
        }

        clickCheckConnect(binding.btnTextColor2) {
            addLogI("btnTextColor2")
            updateUi(255, 153, 51)
        }

        clickCheckConnect(binding.btnTextColor3) {
            addLogI("btnTextColor3")
            updateUi(51, 255, 153)
        }

        clickCheckConnect(binding.btnSend) {
            addLogI("btnSend")
            addLogI("newCustomClockDialData")
            ControlBleTools.getInstance().newCustomClockDialData(
                sourceData, colorR, colorG, colorB, bgBitmap, textBitmap, { dialData ->
                    addLogI("newCustomClockDialData data.size=${dialData.size}")
                    val dialId = "dialId"
                    val dialSize = dialData.size
                    val isReplace = true
                    addLogI("getDeviceWatchFace dialId=$dialId dialSize=$dialSize isReplace=$isReplace")
                    ControlBleTools.getInstance().getDeviceWatchFace(dialId, dialData.size, isReplace, object : DeviceWatchFaceFileStatusListener {
                        override fun onSuccess(statusValue: Int, statusName: String?) {
                            addLogI("getDeviceWatchFace onSuccess statusValue=$statusValue statusName=$statusName")
                            if (statusValue == DeviceLargeFileStatusListener.PrepareStatus.READY.state) {
                                addLogI("uploadClockDialFie")

                                val type = BleCommonAttributes.UPLOAD_BIG_DATA_WATCH
                                val isReplace = true
                                addLogI("startUploadBigData type=$type fileByte=${dialData.size} isReplace=$isReplace")
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
                }, true
            )
        }
    }

    fun updateUi(colorR: Int, colorG: Int, colorB: Int) {
        this.colorR = colorR
        this.colorG = colorG
        this.colorB = colorB
        addLogI("myCustomClockUtils")
        ControlBleTools.getInstance().myCustomClockUtils(sourceData, colorR, colorG, colorB, bgBitmap, textBitmap) { result: Bitmap? ->
            binding.ivEffect.setImageBitmap(result)
        }
    }
}