package com.zjw.sdkdemo.function.apricot.dial

import android.graphics.Bitmap
import android.os.Bundle
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.DiyWatchFaceConfigBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceWatchFaceFileStatusListener
import com.zhapp.ble.callback.DiyDialDataCallBack
import com.zhapp.ble.callback.DiyWatchFaceCallBack
import com.zhapp.ble.callback.UploadBigDataListener
import com.zhapp.ble.callback.WatchFaceInstallCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityDiyDialV2SimpleBinding
import com.zjw.sdkdemo.utils.AssetUtils
import com.zjw.sdkdemo.utils.BitmapUtils


class DiyDialV2SimpleActivity : BaseActivity() {
    private val binding by lazy { ActivityDiyDialV2SimpleBinding.inflate(layoutInflater) }
    private val tag: String = DiyDialV2SimpleActivity::class.java.simpleName

    private var numberJsonStr: String? = ""
    private var pointerJsonStr: String? = ""
    private var diyWatchFaceConfig: DiyWatchFaceConfigBean? = null
    private var isSendPointer = false

    private val mNumberResource = AssetUtils.ASS_APRICOT_DIAL_DIY_V2_FOLDER + "number_resource"
    private val mPointerResource = AssetUtils.ASS_APRICOT_DIAL_DIY_V2_FOLDER + "pointer_resource"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.clock_dial_diy_v2)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initListener()
        initCallBack()
        initData()
    }

    private fun initData() {
        //数字表盘信息 Number dial information
        numberJsonStr = AssetUtils.getAssetFileContent(this, "$mNumberResource/watch.json")
        addLogI("numberJsonStr=${formatObject(numberJsonStr!!)}")

        //指针表盘信息 Pointer dial information
        pointerJsonStr = AssetUtils.getAssetFileContent(this, "$mPointerResource/watch.json")
        addLogI("numberJsonStr=${formatObject(pointerJsonStr!!)}")
    }

    private fun initListener() {

        clickCheckConnect(binding.btnSendNumber) {
            addLogI("btnSendNumber")
            isSendPointer = false

            addLogI("getDefDiyWatchFaceConfig")
            diyWatchFaceConfig = ControlBleTools.getInstance().getDefDiyWatchFaceConfig(numberJsonStr)
            addLogBean("getDefDiyWatchFaceConfig", diyWatchFaceConfig!!)

            val id = diyWatchFaceConfig?.id
            addLogI("getDiyWatchFaceConfig id=$id")
            ControlBleTools.getInstance().getDiyWatchFaceConfig(id, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getDiyWatchFaceConfig state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSendPointer) {
            addLogI("btnSendPointer")
            isSendPointer = true

            addLogI("getDefDiyWatchFaceConfig")
            diyWatchFaceConfig = ControlBleTools.getInstance().getDefDiyWatchFaceConfig(pointerJsonStr)
            addLogBean("getDefDiyWatchFaceConfig", diyWatchFaceConfig!!)

            val id = diyWatchFaceConfig?.id
            addLogI("getDiyWatchFaceConfig id=$id")
            ControlBleTools.getInstance().getDiyWatchFaceConfig(id, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getDiyWatchFaceConfig state=$state")
                }
            })
        }
    }


    fun sendPointerDiy() {
        val bitmapList = mutableListOf<Bitmap?>().apply {
            add(
                BitmapUtils.combineBitmap(
                    AssetUtils.getAssetBitmap(this@DiyDialV2SimpleActivity, "$mPointerResource/background/background.png"),
                    AssetUtils.getAssetBitmap(this@DiyDialV2SimpleActivity, "$mPointerResource/overlay/overlay_00.png"), 0, 0
                )
            )
        }
        val thumbnailBitmap = AssetUtils.getAssetBitmap(this, "$mPointerResource/background/thumbnail.png")
        val data = AssetUtils.getAssetBytes(this, "$mPointerResource/pointer/3101_Data.bin")
        val complex = AssetUtils.getAssetBytes(this, "$mPointerResource/complex/complex.bin")
        addLogI("getSimplePointerDiyDialData")
        ControlBleTools.getInstance().getSimplePointerDiyDialData(bitmapList, thumbnailBitmap, data, complex, diyWatchFaceConfig, pointerJsonStr, object : DiyDialDataCallBack {
            override fun onDialData(diyDialId: String, data: ByteArray, bean: DiyWatchFaceConfigBean) {
                addLogBean("getSimplePointerDiyDialData onDialData iyDialId=$diyDialId data.size=${data.size}", bean)
                //需要更新文件和配置类
                uploadWatch(diyDialId, data, bean)
            }

            override fun onChangeConfig(bean: DiyWatchFaceConfigBean) {
                addLogBean("getSimplePointerDiyDialData onChangeConfig", bean)

                //只需要更新配置类
                addLogBean("setDiyWatchFaceConfig", bean)
                ControlBleTools.getInstance().setDiyWatchFaceConfig(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("setDiyWatchFaceConfig state=$state")
                        if (state == SendCmdState.SUCCEED) {
                            diyWatchFaceConfig = bean
                        }
                    }
                })
            }

            override fun onError(errMsg: String?) {
                addLogE("getSimplePointerDiyDialData onError errMsg=$errMsg")
            }
        })
    }


    fun sendNumberDiy() {
        val bitmapList = mutableListOf<Bitmap?>().apply {
            val bgBitmap = AssetUtils.getAssetBitmap(this@DiyDialV2SimpleActivity, "$mNumberResource/background/background.png")
            val overlayBitmap = AssetUtils.getAssetBitmap(this@DiyDialV2SimpleActivity, "$mNumberResource/overlay/overlay_00.png")
            val newBitmap = BitmapUtils.combineBitmap(bgBitmap, overlayBitmap, 0, 0)
            add(newBitmap)
        }
        val design = AssetUtils.getAssetBitmap(this, "$mNumberResource/background/designsketch.png")
        val numberId = "G1_MidUp"
        val time = AssetUtils.getAssetBytes(this, "$mNumberResource/time/G1/G1_Data.bin")
        val complex = AssetUtils.getAssetBytes(this, "$mNumberResource/complex/complex.bin")
        val colorList = mutableListOf<IntArray?>().apply {
            add(intArrayOf(255, 255, 255))
        }

        addLogI("getSimpleNumberDiyDialData")
        ControlBleTools.getInstance().getSimpleNumberDiyDialData(bitmapList, design, numberId, time, complex, colorList, diyWatchFaceConfig, numberJsonStr, object : DiyDialDataCallBack {
            override fun onDialData(diyDialId: String, data: ByteArray, bean: DiyWatchFaceConfigBean) {
                addLogBean("getSimpleNumberDiyDialData onDialData diyDialId=$diyDialId data.size=${data.size}", bean)
                //需要更新文件和配置类
                uploadWatch(diyDialId, data, bean)
            }

            override fun onChangeConfig(bean: DiyWatchFaceConfigBean) {
                addLogBean("getSimpleNumberDiyDialData onChangeConfig", bean)

                //只需要更新配置类
                addLogBean("setDiyWatchFaceConfig", bean)
                ControlBleTools.getInstance().setDiyWatchFaceConfig(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("setDiyWatchFaceConfig state=$state")
                        if (state == SendCmdState.SUCCEED) {
                            diyWatchFaceConfig = bean
                        }
                    }
                })
            }

            override fun onError(errMsg: String?) {
                addLogE("getSimpleNumberDiyDialData onError errMsg=$errMsg")
            }
        })
    }


    private fun uploadWatch(watchId: String, data: ByteArray, configBean: DiyWatchFaceConfigBean) {
        val isReplace = true
        addLogBean("getDeviceDiyWatchFace watchId=$watchId data.size=${data.size} isReplace=$isReplace", configBean)
        ControlBleTools.getInstance().getDeviceDiyWatchFace(watchId, data.size, isReplace, configBean, object : DeviceWatchFaceFileStatusListener {
            override fun onSuccess(statusValue: Int, statusName: String) {
                addLogI("getDeviceDiyWatchFace statusValue$statusValue statusName=$statusName")

                if (statusValue == DeviceWatchFaceFileStatusListener.PrepareStatus.READY.state) {

                    val type = BleCommonAttributes.UPLOAD_BIG_DATA_WATCH
                    val isReplace = true
                    addLogI("startUploadBigData type=$type fileByte=${data.size} isReplace=$isReplace")
                    ControlBleTools.getInstance().startUploadBigData(type, data, isReplace, object : UploadBigDataListener {
                        override fun onSuccess() {
                            addLogI("startUploadBigData onSuccess")
                            diyWatchFaceConfig = configBean
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
                addLogE("getDeviceDiyWatchFace timeOut")
            }
        })
    }


    private fun initCallBack() {
        //获取设备的规则
        CallBackUtils.diyWatchFaceCallBack = DiyWatchFaceCallBack { bean ->
            addLogBean("diyWatchFaceCallBack", bean)
            diyWatchFaceConfig?.ruleCount = bean.ruleCount
            if (isSendPointer) {
                sendPointerDiy()
            } else {
                sendNumberDiy()
            }
        }

        /**
         * 表盘文件安装结果回调
         */
        CallBackUtils.watchFaceInstallCallBack = WatchFaceInstallCallBack { bean ->
            addLogBean("watchFaceInstallCallBack", bean)
        }
    }
}