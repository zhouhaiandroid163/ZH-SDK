package com.zjw.sdkdemo.function.apricot.dial

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.GsonUtils
import com.bumptech.glide.Glide
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.DiyWatchFaceConfigBean
import com.zhapp.ble.bean.diydial.NewDiyParamsBean
import com.zhapp.ble.bean.diydial.NewZhDiyDialBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceWatchFaceFileStatusListener
import com.zhapp.ble.callback.DiyDialDataCallBack
import com.zhapp.ble.callback.DiyDialPreviewCallBack
import com.zhapp.ble.callback.DiyWatchFaceCallBack
import com.zhapp.ble.callback.UploadBigDataListener
import com.zhapp.ble.callback.WatchFaceInstallCallBack
import com.zhapp.ble.custom.DiyDialUtils
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityDiyDialV2Binding
import com.zjw.sdkdemo.ui.view.ColorRoundView
import com.zjw.sdkdemo.utils.AssetUtils
import com.zjw.sdkdemo.utils.DiyUtils
import com.zjw.sdkdemo.utils.ToastUtils
import java.io.File

@SuppressLint("NotifyDataSetChanged")
class DiyDialV2Activity : BaseActivity() {
    private val binding by lazy { ActivityDiyDialV2Binding.inflate(layoutInflater) }
    private val tag: String = DiyDialV2Activity::class.java.simpleName

    companion object {
        const val DIY_TYPE_TAG = "DIY_TYPE_TAG"
        const val COLOR_PICKER_NAME = "MyColorPickerDialog"
    }

    //diy表盘描述对象
    private var dataJson: String? = ""

    //背景
    private val photoData = mutableListOf<PhotoBean>()
    private var photoSelectBitmap = mutableListOf<Bitmap>()

    //背景覆盖图
    private val overlayData = mutableListOf<OverlayBean>()
    private var overlaySelectBitmap: Bitmap? = null

    //指针
    private val styles = mutableListOf<StyleBean>()
    private var styleSelect: StyleBean? = null


    //数字样式 数字位置
    private val numberFonts = mutableListOf<NumberFontBean>()
    private val numberLocationBeans = mutableListOf<NumberLocationBean>()
    private var numberSelect: NumberLocationBean? = null
    private var numberSelectPosition = 0

    //选中的颜色 默认白色
    private var selectedColors: MutableList<IntArray> = mutableListOf<IntArray>().apply {
        add(intArrayOf(255, 255, 255))
    }

    private var diyWatchFaceConfig: DiyWatchFaceConfigBean? = null

    //功能选择跳转返回
    private lateinit var functionSelectResultLauncher: ActivityResultLauncher<Intent>

    //diy_v1 表盘样式 ： 1：数字 ， 2：指针
    private var diyType = 1
    private var mResource: String? = ""
    private var mUser: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.clock_dial_diy_v2_demo_number)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initListener()
        initCallBack()
        initData()
    }

    private fun initData() {
        diyType = intent.getIntExtra(DIY_TYPE_TAG, 1)
        //数字资源
        if (diyType == 1) {
            setTitle(R.string.clock_dial_diy_v2_demo_number)
            mResource = AssetUtils.ASS_APRICOT_DIAL_DIY_V2_FOLDER + "number_resource"
            mUser = AssetUtils.ASS_APRICOT_DIAL_DIY_V2_FOLDER + "number_user_bg"
            initNumbers()
        }
        //指针资源
        else {
            setTitle(R.string.clock_dial_diy_v2_demo_pointer)
            mResource = AssetUtils.ASS_APRICOT_DIAL_DIY_V2_FOLDER + "pointer_resource"
            mUser = AssetUtils.ASS_APRICOT_DIAL_DIY_V2_FOLDER + "pointer_user_bg"
            initPointer()
        }
        activityResultRegister()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnSync) {
            addLogI("btnSync")
            val bean = getDiyParamsBean()
            addLogI("getNewDiyDialData")
            ControlBleTools.getInstance().getNewDiyDialData(bean, object : DiyDialDataCallBack {
                override fun onDialData(diyDialId: String?, data: ByteArray, bean: DiyWatchFaceConfigBean) {
                    //需要更新文件和配置类
                    addLogBean("getDeviceDiyWatchFace onDialData diyDialId=$diyDialId Data.size=${data.size}", bean)
                    ControlBleTools.getInstance().getDeviceDiyWatchFace(diyDialId, data.size, true, bean, object : DeviceWatchFaceFileStatusListener {
                        override fun onSuccess(statusValue: Int, statusName: String) {
                            addLogI("getDeviceDiyWatchFace onSuccess statusValue$statusValue statusName=$statusName")

                            if (statusValue == DeviceWatchFaceFileStatusListener.PrepareStatus.READY.state) {

                                val type = BleCommonAttributes.UPLOAD_BIG_DATA_WATCH
                                val isReplace = true
                                addLogI("startUploadBigData type=$type fileByte=${data.size} isReplace=$isReplace")
                                ControlBleTools.getInstance().startUploadBigData(type, data, isReplace, object : UploadBigDataListener {
                                    override fun onSuccess() {
                                        addLogI("startUploadBigData onSuccess")
                                        diyWatchFaceConfig = bean
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

                override fun onChangeConfig(bean: DiyWatchFaceConfigBean) {

                    //只需要更新配置类
                    addLogBean("setDiyWatchFaceConfig onChangeConfig", bean)
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
                    addLogI("getNewDiyDialData onError errMsg=$errMsg")
                }
            })
        }
    }

    private fun initCallBack() {
        /**
         * 获取diy表盘配置相关回调
         */
        CallBackUtils.diyWatchFaceCallBack = DiyWatchFaceCallBack { bean ->
            addLogBean("diyWatchFaceCallBack", bean)
            //根据 DiyWatchFaceConfigBean更新UI
            if (bean != null) {


                //复杂功能为空时，取默认配置类的复杂功能设置
                if (bean.functionsConfigs == null || bean.functionsConfigs.isEmpty()) {
                    val cfConfig = diyWatchFaceConfig?.functionsConfigs
                    diyWatchFaceConfig = bean
                    diyWatchFaceConfig?.functionsConfigs = cfConfig
                } else {
                    diyWatchFaceConfig = bean
                }
                //刷新复杂功能
                refFunctionsAdapter()
                //刷新 背景 / 背景覆盖图
                if (diyWatchFaceConfig?.backgroundFileConfig != null && !diyWatchFaceConfig!!.backgroundFileConfig.watchFaceFiles.isNullOrEmpty()) {
                    //先全部清空背景图选中
                    for (photo in photoData) {
                        photo.isSelected = false
                    }
                    photoSelectBitmap.clear()
                    selectedColors.clear()

                    var selectedOverlayMd5 = ""
                    for (faceFile in diyWatchFaceConfig!!.backgroundFileConfig!!.watchFaceFiles) {
                        addLogI("background selectedMd5=${faceFile.fileMd5}")
                        for (photo in photoData) {
                            if (TextUtils.equals(faceFile.fileMd5, DiyDialUtils.getDiyBitmapMd5String(photo.imgBitmap))) {
                                photo.isSelected = true
                                photoSelectBitmap.add(photo.imgBitmap!!)
                                selectedColors.add(DiyDialUtils.getColorByRGBValue(faceFile.backgroundColorHex))
                            }
                        }
                        if (faceFile.fileNumber == diyWatchFaceConfig!!.backgroundFileConfig!!.usedFileNumber) {
                            selectedOverlayMd5 = faceFile.backgroundOverlayMd5
                            addLogI("background selectedOverlayMd5=$selectedOverlayMd5")
                        }
                    }

                    for (overlay in overlayData) {
                        addLogI("overlay Md5=${DiyDialUtils.getDiyBitmapMd5String(overlay.imgBitmap)}")
                        overlay.isSelected = TextUtils.equals(selectedOverlayMd5, DiyDialUtils.getDiyBitmapMd5String(overlay.imgBitmap))
                        if (overlay.isSelected) overlaySelectBitmap = overlay.imgBitmap
                    }
                }
                refPhotoAdapter()
                refOverlayAdapter()
                refFixedColorPickerItem()
                //刷新指针
                if (diyWatchFaceConfig?.pointerFileConfig != null && !diyWatchFaceConfig!!.pointerFileConfig.watchFaceFiles.isNullOrEmpty()) {
                    var selectedMd5 = ""
                    for (faceFile in diyWatchFaceConfig!!.pointerFileConfig!!.watchFaceFiles) {
                        if (faceFile.fileNumber == diyWatchFaceConfig!!.pointerFileConfig!!.usedFileNumber) {
                            selectedMd5 = faceFile.fileMd5
                            addLogI("pointer selectedMd5=$selectedMd5")
                        }
                    }
                    for (style in styles) {
                        style.isSelected = TextUtils.equals(selectedMd5, DiyDialUtils.getDiyBinBytesMd5(style.binData))
                        if (style.isSelected) styleSelect = style
                        addLogI("pointer Md5=${DiyDialUtils.getDiyBinBytesMd5(style.binData)}")
                    }
                }
                refStyleAdapter()
                //刷新数字 字体 位置 颜色
                if (diyWatchFaceConfig?.numberFileConfig != null && !diyWatchFaceConfig!!.numberFileConfig.watchFaceFiles.isNullOrEmpty()) {
                    var selectedNumberLocationMd5 = ""
                    var selectedNumberColor: IntArray = intArrayOf(255, 255, 255)
                    for (faceFile in diyWatchFaceConfig!!.numberFileConfig!!.watchFaceFiles) {
                        if (faceFile.fileNumber == diyWatchFaceConfig!!.numberFileConfig!!.usedFileNumber) {
                            val selectedNumberFontMd5 = faceFile.numberFontMd5
                            selectedNumberLocationMd5 = faceFile.numberLocationMd5
                            selectedNumberColor = DiyDialUtils.getColorByRGBValue(faceFile.numberColorHex)
                            addLogI("diy_resource selectedNumberFontMd5=$selectedNumberFontMd5")
                            addLogI("diy_resource selectedNumberLocationMd5=$selectedNumberLocationMd5")
                            addLogI("diy_resource selectedNumberColor=${formatObject(selectedNumberColor)}")
                        }
                    }

                    var fontName = ""
                    var locationName = ""
                    for (font in numberFonts) {
                        for (location in font.locations) {
                            if (TextUtils.equals(selectedNumberLocationMd5, DiyDialUtils.getDiyBitmapMd5String(location.locationImg))) {
                                fontName = location.fontName
                                locationName = location.locationName
                                addLogI("diy_resource fontName=$fontName locationName=$locationName")

                            }
                        }
                    }
                    for (numberFont in numberFonts) {
                        numberFont.locations.firstOrNull { it.locationName == locationName }?.locationImg?.let {
                            numberFont.fontImg = it
                        }
                        if (TextUtils.equals(numberFont.fontName, fontName)) {
                            numberFont.isSelected = true
                            numberLocationBeans.clear()
                            numberLocationBeans.addAll(numberFont.locations)
                            for (nl in numberLocationBeans) {
                                if (TextUtils.equals(nl.locationName, locationName)) {
                                    nl.isSelected = true
                                    numberSelect = nl
                                    numberSelectPosition = numberLocationBeans.indexOf(nl)
                                } else {
                                    nl.isSelected = false
                                }

                            }
                        } else {
                            numberFont.isSelected = false
                        }
                    }
                    refNumberFontAndLocation()
                    //赋值给第一个背景对应色
                    selectedColors[0][0] = selectedNumberColor[0]
                    selectedColors[0][1] = selectedNumberColor[1]
                    selectedColors[0][2] = selectedNumberColor[2]
                    refFixedColorPickerItem()
                }
                refPreView()
            }
        }

        /**
         * 表盘文件安装结果回调
         */
        CallBackUtils.watchFaceInstallCallBack = WatchFaceInstallCallBack { bean ->
            addLogBean("watchFaceInstallCallBack", bean)
        }
    }


    private fun activityResultRegister() {
        functionSelectResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            addLogI("functionSelectResultLauncher ${result.resultCode}")
            if (result.resultCode == RESULT_OK) {
                val json = result.data?.getStringExtra(DiyFunctionSelectActivity.RESULT_DATA_TEXT)
                if (json != null) {
                    val configBean = GsonUtils.fromJson(json, DiyWatchFaceConfigBean.FunctionsConfig::class.java)
                    for (i in 0..diyWatchFaceConfig!!.functionsConfigs!!.size) {
                        val info = diyWatchFaceConfig!!.functionsConfigs!![i]
                        if (info.position == configBean.position) {
                            val configs = diyWatchFaceConfig!!.functionsConfigs as MutableList
                            configs[i] = configBean
                            diyWatchFaceConfig!!.functionsConfigs = configs
                            break
                        }
                    }
                    refFunctionsAdapter()
                    refPreView()
                }
            }
        }
    }

    private fun initNumbers() {
        try {
            dataJson = AssetUtils.getAssetFileContent(this, "$mResource/watch.json")
            val diyDialBean: NewZhDiyDialBean? = GsonUtils.fromJson(dataJson, NewZhDiyDialBean::class.java)
            if (diyDialBean == null) {
                addLogI("diyDialBean is null")
                ToastUtils.showToast("diyDialBean is null")
                finish()
                return
            }
            //region 背景图 可自定义 UI必须存在
            photoData.apply {
                // 图片资源高宽必须和表盘高宽一致
                val bgPath = mResource + diyDialBean.background.backgroundImgPath.replace("\\", File.separator)
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, bgPath), true))
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, "$mUser/photo_1.png"), false))
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, "$mUser/photo_2.png"), false))
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, "$mUser/photo_3.png"), false))
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, "$mUser/photo_4.png"), false))
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, "$mUser/photo_5.png"), false))
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, "$mUser/photo_6.png"), false))
            }
            photoSelectBitmap.clear()
            photoSelectBitmap.add(photoData[0].imgBitmap!!)
            binding.rvPhoto.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.rvPhoto.adapter = PhotoAdapter(this, photoData) { addP, delP ->
                if (addP != -1) {
                    photoSelectBitmap.add(photoData[addP].imgBitmap!!)
                    selectedColors.add(intArrayOf(255, 255, 255))
                }
                if (delP != -1) {
                    if (photoSelectBitmap.size > 1) {
                        photoSelectBitmap.remove(photoData[delP].imgBitmap)
                        if (delP < selectedColors.size) {
                            selectedColors.removeAt(delP)
                        }
                    }
                }
                refFixedColorPickerItem()
                refPreView()
            }
            //endregion

            //region 背景覆盖图 读取资源配置，不存在隐藏UI
            if (diyDialBean.overlayImgPaths != null) {
                overlayData.apply {
                    //TODO 更换数据格式
                    for (path in diyDialBean.overlayImgPaths) {
                        val overlayPath = mResource + path.replace("\\", File.separator)
                        add(OverlayBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, overlayPath)!!, false))
                    }

                }
            }
            if (overlayData.isNotEmpty()) {
                binding.rvOverlay.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.rvOverlay.adapter = OverlayAdapter(this, overlayData) { selectedP ->
                    overlaySelectBitmap = overlayData[selectedP].imgBitmap
                    refPreView()
                }
                //背景覆盖图默认选中
                overlayData[0].isSelected = true
                overlaySelectBitmap = overlayData[0].imgBitmap
            } else {
                binding.tvOverlay.visibility = View.GONE
                binding.rvOverlay.visibility = View.GONE
            }
            //endregion

            //region 数字样式 数字位置 颜色 读取资源配置，不存在隐藏UI
            if (!diyDialBean.time.isNullOrEmpty()) {
                numberFonts.apply {
                    for (time in diyDialBean.time) {
                        val timePath = mResource + time.timeDataPath.replace("\\", File.separator)
                        val binData: ByteArray = AssetUtils.getAssetBytes(this@DiyDialV2Activity, timePath)!!
                        val fontImgPath = mResource + time.locationInfos[0].timeImgPath.replace("\\", File.separator)
                        val fontImgBitmap = AssetUtils.getAssetBitmap(this@DiyDialV2Activity, fontImgPath)
                        add(
                            NumberFontBean(
                                time.fontsName, fontImgBitmap!!,
                                mutableListOf<NumberLocationBean>().apply {
                                    if (!time.locationInfos.isNullOrEmpty()) {
                                        for (location in time.locationInfos) {
                                            val locationImgPath = mResource + location.timeImgPath.replace("\\", File.separator)
                                            val locationImgBitmap = AssetUtils.getAssetBitmap(this@DiyDialV2Activity, locationImgPath)
                                            val imgDataPath = mResource + location.timeOverlayPath.replace("\\", File.separator)
                                            val imgDataBitmap = AssetUtils.getAssetBitmap(this@DiyDialV2Activity, imgDataPath)
                                            add(NumberLocationBean(time.fontsName, location.locationName, locationImgBitmap!!, imgDataBitmap!!, binData, false))
                                        }
                                    }
                                },
                                false
                            )
                        )
                    }
                }
            }
            if (numberFonts.isNotEmpty()) {
                //默认选中
                numberFonts[0].isSelected = true
                numberFonts[0].locations[0].isSelected = true
                numberSelect = numberFonts[0].locations[0]
                numberSelectPosition = 0
                numberLocationBeans.addAll(numberFonts[0].locations)

                binding.rvNumberStyle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.rvNumberStyle.adapter = NumberStylerAdapter(this, numberFonts) { selectedP ->
                    numberLocationBeans.clear()
                    numberLocationBeans.addAll(numberFonts[selectedP].locations)
                    if (numberSelectPosition < numberLocationBeans.size) {
                        //切换样式更新上次选中的位置
                        numberSelect = numberLocationBeans[numberSelectPosition]
                        for (i in 0 until numberLocationBeans.size) {
                            numberLocationBeans[i].isSelected = numberSelectPosition == i
                        }
                    } else {
                        //切换样式 上次选中的位置不存在默认选中第一位
                        numberSelectPosition = 0
                        numberSelect = numberLocationBeans[0]
                        for (i in 0 until numberLocationBeans.size) {
                            numberLocationBeans[i].isSelected = numberSelectPosition == i
                        }
                        //更新样式
                        val selectLocationName = numberLocationBeans[numberSelectPosition].locationName
                        for (numberFont in numberFonts) {
                            numberFont.locations.firstOrNull { it.locationName == selectLocationName }?.locationImg?.let {
                                numberFont.fontImg = it
                            }
                        }
                        binding.rvNumberStyle.adapter?.notifyDataSetChanged()
                    }
                    binding.rvNumberLocation.adapter?.notifyDataSetChanged()

                    refPreView()
                }
                binding.rvNumberLocation.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.rvNumberLocation.adapter = NumberLocationAdapter(this, numberLocationBeans) { selectedP ->
                    //切换位置 更新样式图
                    val selectLocationName = numberLocationBeans[selectedP].locationName
                    for (numberFont in numberFonts) {
                        numberFont.locations.firstOrNull { it.locationName == selectLocationName }?.locationImg?.let {
                            numberFont.fontImg = it
                        }
                    }
                    binding.rvNumberStyle.adapter?.notifyDataSetChanged()
                    numberSelect = numberLocationBeans[selectedP]
                    numberSelectPosition = selectedP
                    refPreView()
                }
                // 颜色选择器
                refFixedColorPickerItem()
            } else {
                binding.tvNumberStyle.visibility = View.GONE
                binding.rvNumberStyle.visibility = View.GONE
                binding.tvNumberLocation.visibility = View.GONE
                binding.rvNumberLocation.visibility = View.GONE
                binding.layoutCustomize.visibility = View.GONE
            }
            //endregion

            //region 数字颜色(与多背景对应)
            binding.rvBgColors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.rvBgColors.adapter = BgColorAdapter(this, selectedColors)
            //endregion

            binding.tvStyle.visibility = View.GONE
            binding.rvStyle.visibility = View.GONE

            //复杂功能
            addLogBean("getDefDiyWatchFaceConfig",dataJson!!)
            diyWatchFaceConfig = ControlBleTools.getInstance().getDefDiyWatchFaceConfig(dataJson)
            addLogBean("getDefDiyWatchFaceConfig", diyWatchFaceConfig!!)

            if (diyWatchFaceConfig != null && diyWatchFaceConfig!!.functionsConfigs != null) {
                binding.rvComplex.apply {
                    layoutManager = LinearLayoutManager(this@DiyDialV2Activity, LinearLayoutManager.VERTICAL, false)
                    addItemDecoration(DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL))
                    adapter = FunctionsAdapter(this@DiyDialV2Activity, diyWatchFaceConfig!!.functionsConfigs!!) { clickPosition ->
                        val info = diyWatchFaceConfig!!.functionsConfigs!![clickPosition]
                        val details = info.functionsConfigTypes
                        if (details.isNullOrEmpty()) {
                            return@FunctionsAdapter
                        }
                        val intent = Intent(this@DiyDialV2Activity, DiyFunctionSelectActivity::class.java)
                        intent.putExtra(DiyFunctionSelectActivity.ACTIVITY_DATA_TEXT, GsonUtils.toJson(info))
                        functionSelectResultLauncher.launch(intent)
                    }
                }
            }
            refPreView()
            //获取设备表盘功能状态

            val diyWatchId = diyWatchFaceConfig?.id
            addLogI("getDiyWatchFaceConfig diyWatchId=$diyWatchId")
            ControlBleTools.getInstance().getDiyWatchFaceConfig(diyWatchId, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getDiyWatchFaceConfig state=$state")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            addLogE("initNumbers Exception e=${e.message}")
        }
    }

    private fun initPointer() {
        try {
            dataJson = AssetUtils.getAssetFileContent(this, "$mResource/watch.json")
            val diyDialBean: NewZhDiyDialBean? = GsonUtils.fromJson(dataJson, NewZhDiyDialBean::class.java)
            if (diyDialBean == null) {
                addLogI("diyDialBean is null")
                ToastUtils.showToast("diyDialBean is null")
                finish()
                return
            }

            //region 背景图 可自定义 UI必须存在
            photoData.apply {
                // 图片资源高宽必须和表盘高宽一致
                val bgPath = mResource + diyDialBean.background.backgroundImgPath.replace("\\", File.separator)
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, bgPath), true))
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, "$mUser/photo_1.png"), false))
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, "$mUser/photo_2.png"), false))
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, "$mUser/photo_3.png"), false))
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, "$mUser/photo_4.png"), false))
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, "$mUser/photo_5.png"), false))
                add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, "$mUser/photo_6.png"), false))
            }
            photoSelectBitmap.clear()
            photoSelectBitmap.add(photoData[0].imgBitmap!!)
            binding.rvPhoto.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.rvPhoto.adapter = PhotoAdapter(this@DiyDialV2Activity, photoData) { addP, delP ->
                if (addP != -1) {
                    photoSelectBitmap.add(photoData[addP].imgBitmap!!)
                    selectedColors.add(intArrayOf(255, 255, 255))
                }
                if (delP != -1) {
                    if (photoSelectBitmap.size > 1) {
                        photoSelectBitmap.remove(photoData[delP].imgBitmap)
                        if (delP < selectedColors.size) {
                            selectedColors.removeAt(delP)
                        }
                    }
                }
                refFixedColorPickerItem()
                refPreView()
            }
            //endregion

            //region 背景覆盖图 读取资源配置，不存在隐藏UI
            if (diyDialBean.overlayImgPaths != null) {
                overlayData.apply {
                    //TODO 更换数据格式
                    for (path in diyDialBean.overlayImgPaths) {
                        val overlayPath = mResource + path.replace("\\", File.separator)
                        add(OverlayBean(AssetUtils.getAssetBitmap(this@DiyDialV2Activity, overlayPath)!!, false))
                    }
                }
            }
            if (overlayData.isNotEmpty()) {
                binding.rvOverlay.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.rvOverlay.adapter = OverlayAdapter(this@DiyDialV2Activity, overlayData) { selectedP ->
                    overlaySelectBitmap = overlayData[selectedP].imgBitmap
                    refPreView()
                }
                //背景覆盖图默认选中
                overlayData[0].isSelected = true
                overlaySelectBitmap = overlayData[0].imgBitmap
            } else {
                binding.tvOverlay.visibility = View.GONE
                binding.rvOverlay.visibility = View.GONE
            }
            //endregion

            //region 数字样式 数字位置 颜色 读取资源配置，不存在隐藏UI
            binding.tvNumberStyle.visibility = View.GONE
            binding.rvNumberStyle.visibility = View.GONE
            binding.tvNumberLocation.visibility = View.GONE
            binding.rvNumberLocation.visibility = View.GONE
            binding.layoutCustomize.visibility = View.GONE
            //endregion

            //region 指针 读取资源配置，不存在隐藏UI
            if (!diyDialBean.pointers.isNullOrEmpty()) {
                styles.apply {
                    for (point in diyDialBean.pointers) {
                        val imgPath = mResource + point.pointerImgPath.replace("\\", File.separator)
                        val imgBitmap = AssetUtils.getAssetBitmap(this@DiyDialV2Activity, imgPath)
                        val imgDataPath = mResource + point.pointerOverlayPath.replace("\\", File.separator)
                        val imgDataBitmap = AssetUtils.getAssetBitmap(this@DiyDialV2Activity, imgDataPath)
                        val binPath = mResource + point.pointerDataPath.replace("\\", File.separator)
                        val binData = AssetUtils.getAssetBytes(this@DiyDialV2Activity, binPath)
                        add(StyleBean(imgBitmap!!, imgDataBitmap!!, binData!!, false))
                    }
                }
            }
            if (styles.isNotEmpty()) {
                binding.rvStyle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.rvStyle.adapter = StyleAdapter(this@DiyDialV2Activity, styles) { selectedP ->
                    styleSelect = styles[selectedP]
                    refPreView()
                }
                //指针默认选中
                styles[0].isSelected = true
                styleSelect = styles[0]
            } else {
                binding.tvStyle.visibility = View.GONE
                binding.rvStyle.visibility = View.GONE
            }
            //endregion

            //复杂功能
            addLogBean("getDefDiyWatchFaceConfig",dataJson!!)
            diyWatchFaceConfig = ControlBleTools.getInstance().getDefDiyWatchFaceConfig(dataJson)
            addLogBean("getDefDiyWatchFaceConfig",diyWatchFaceConfig!!)

            if (diyWatchFaceConfig != null && diyWatchFaceConfig!!.functionsConfigs != null) {
                binding.rvComplex.apply {
                    layoutManager = LinearLayoutManager(this@DiyDialV2Activity, LinearLayoutManager.VERTICAL, false)
                    addItemDecoration(DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL))
                    adapter = FunctionsAdapter(this@DiyDialV2Activity, diyWatchFaceConfig!!.functionsConfigs!!) { clickPosition ->
                        val info = diyWatchFaceConfig!!.functionsConfigs!![clickPosition]
                        val details = info.functionsConfigTypes
                        if (details.isNullOrEmpty()) {
                            return@FunctionsAdapter
                        }
                        val intent = Intent(this@DiyDialV2Activity, DiyFunctionSelectActivity::class.java)
                        intent.putExtra(DiyFunctionSelectActivity.ACTIVITY_DATA_TEXT, GsonUtils.toJson(info))
                        functionSelectResultLauncher.launch(intent)
                    }
                }
            }
            refPreView()
            //获取设备表盘功能状态
            val id = diyWatchFaceConfig?.id
            addLogI("getDiyWatchFaceConfig id=$id")
            ControlBleTools.getInstance().getDiyWatchFaceConfig(id, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getDiyWatchFaceConfig state=$state")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            addLogE("initPointer Exception e=${e.message}")
            finish()
            return
        }

    }


    //region 传输表盘


    //region 图片选择适配器
    data class PhotoBean(var imgBitmap: Bitmap?, var isSelected: Boolean)

    class PhotoAdapter(private val context: Context, private val data: List<PhotoBean>, var selected: (addPosition: Int, delPosition: Int) -> Unit) :
        RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            return PhotoViewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_item_diy_photo, parent, false)
            )
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val photoBean = data[position]
            Glide.with(context).load(photoBean.imgBitmap).into(holder.ivIcon)
            holder.ivSelected.visibility = if (photoBean.isSelected) View.VISIBLE else View.GONE
            holder.rootLayout.setOnClickListener {
                if (!photoBean.isSelected) {
                    data[position].isSelected = true
                    notifyDataSetChanged()
                    selected(position, -1)
                } else {
                    var selectedNum = 0
                    for (item in data) {
                        if (item.isSelected) selectedNum += 1
                    }
                    if (selectedNum > 1) {
                        data[position].isSelected = false
                        notifyDataSetChanged()
                        selected(-1, position)
                    }
                }

            }
        }

        override fun getItemCount(): Int = data.size


        inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var ivIcon: AppCompatImageView = view.findViewById(R.id.iv_icon)
            var ivSelected: AppCompatImageView = view.findViewById(R.id.iv_selected)
            val rootLayout: ConstraintLayout = view.findViewById(R.id.root_layout)
        }
    }
    //endregion

    //region 背景覆盖图选择适配器
    data class OverlayBean(var imgBitmap: Bitmap, var isSelected: Boolean)

    class OverlayAdapter(private val context: Context, private val data: List<OverlayBean>, var selected: (position: Int) -> Unit) :
        RecyclerView.Adapter<OverlayAdapter.PhotoViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            return PhotoViewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_item_diy_photo, parent, false)
            )
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val overlayBean = data[position]
            Glide.with(context).load(overlayBean.imgBitmap).into(holder.ivIcon)
            holder.ivSelected.visibility = if (overlayBean.isSelected) View.VISIBLE else View.GONE
            holder.rootLayout.setOnClickListener {

                if (!overlayBean.isSelected) {
                    data.forEach { it.isSelected = false }
                    data[position].isSelected = true
                    notifyDataSetChanged()
                    selected(position)
                }

            }
        }

        override fun getItemCount(): Int = data.size


        inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var ivIcon: AppCompatImageView = view.findViewById(R.id.iv_icon)
            var ivSelected: AppCompatImageView = view.findViewById(R.id.iv_selected)
            val rootLayout: ConstraintLayout = view.findViewById(R.id.root_layout)
        }
    }
    //endregion

    //region 指针适配器
    data class StyleBean(var img: Bitmap, var imgData: Bitmap, var binData: ByteArray, var isSelected: Boolean) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StyleBean) return false

            if (img != other.img) return false
            if (imgData != other.imgData) return false
            if (!binData.contentEquals(other.binData)) return false
            if (isSelected != other.isSelected) return false

            return true
        }

        override fun hashCode(): Int {
            var result = img.hashCode()
            result = 31 * result + imgData.hashCode()
            result = 31 * result + binData.contentHashCode()
            result = 31 * result + isSelected.hashCode()
            return result
        }
    }


    class StyleAdapter(private val context: Context, private val data: List<StyleBean>, var selected: (position: Int) -> Unit) :
        RecyclerView.Adapter<StyleAdapter.PointerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointerViewHolder {
            return PointerViewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_item_diy_photo, parent, false)
            )
        }


        override fun onBindViewHolder(holder: PointerViewHolder, position: Int) {
            val photoBean = data[position]
            Glide.with(context).load(photoBean.img).into(holder.ivIcon)
            holder.ivSelected.visibility = if (photoBean.isSelected) View.VISIBLE else View.GONE
            holder.rootLayout.setOnClickListener {
                if (!photoBean.isSelected) {
                    data.forEach { it.isSelected = false }
                    data[position].isSelected = true
                    notifyDataSetChanged()
                    selected(position)
                }
            }
        }

        override fun getItemCount(): Int = data.size


        inner class PointerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var ivIcon: AppCompatImageView = view.findViewById(R.id.iv_icon)
            var ivSelected: AppCompatImageView = view.findViewById(R.id.iv_selected)
            val rootLayout: ConstraintLayout = view.findViewById(R.id.root_layout)
        }
    }
    //endregion

    //region 数字 适配器
    data class NumberFontBean(var fontName: String, var fontImg: Bitmap, var locations: List<NumberLocationBean>, var isSelected: Boolean)

    data class NumberLocationBean(var fontName: String, var locationName: String, var locationImg: Bitmap, var imgData: Bitmap, var binData: ByteArray, var isSelected: Boolean) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is NumberLocationBean) return false

            if (fontName != other.fontName) return false
            if (locationName != other.locationName) return false
            if (locationImg != other.locationImg) return false
            if (imgData != other.imgData) return false
            if (!binData.contentEquals(other.binData)) return false
            if (isSelected != other.isSelected) return false

            return true
        }

        override fun hashCode(): Int {
            var result = fontName.hashCode()
            result = 31 * result + locationName.hashCode()
            result = 31 * result + locationImg.hashCode()
            result = 31 * result + imgData.hashCode()
            result = 31 * result + binData.contentHashCode()
            result = 31 * result + isSelected.hashCode()
            return result
        }
    }

    class NumberStylerAdapter(private val context: Context, private val data: List<NumberFontBean>, var selected: (position: Int) -> Unit) :
        RecyclerView.Adapter<NumberStylerAdapter.NumberStylerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberStylerViewHolder {
            return NumberStylerViewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_item_diy_photo, parent, false)
            )
        }

        override fun onBindViewHolder(holder: NumberStylerViewHolder, position: Int) {
            val photoBean = data[position]
            Glide.with(context).load(photoBean.fontImg).into(holder.ivIcon)
            holder.ivSelected.visibility = if (photoBean.isSelected) View.VISIBLE else View.GONE
            holder.rootLayout.setOnClickListener {
                if (!photoBean.isSelected) {
                    data.forEach { it.isSelected = false }
                    data[position].isSelected = true
                    notifyDataSetChanged()
                    selected(position)
                }
            }
        }

        override fun getItemCount(): Int = data.size
        inner class NumberStylerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var ivIcon: AppCompatImageView = view.findViewById(R.id.iv_icon)
            var ivSelected: AppCompatImageView = view.findViewById(R.id.iv_selected)
            val rootLayout: ConstraintLayout = view.findViewById(R.id.root_layout)
        }
    }

    class NumberLocationAdapter(private val context: Context, private val data: List<NumberLocationBean>, var selected: (position: Int) -> Unit) :
        RecyclerView.Adapter<NumberLocationAdapter.NumberLocationViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberLocationViewHolder {
            return NumberLocationViewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_item_diy_photo, parent, false)
            )
        }

        override fun onBindViewHolder(holder: NumberLocationViewHolder, position: Int) {
            val photoBean = data[position]
            Glide.with(context).load(photoBean.locationImg).into(holder.ivIcon)
            holder.ivSelected.visibility = if (photoBean.isSelected) View.VISIBLE else View.GONE
            holder.rootLayout.setOnClickListener {
                if (!photoBean.isSelected) {
                    data.forEach { it.isSelected = false }
                    data[position].isSelected = true
                    notifyDataSetChanged()
                    selected(position)
                }
            }
        }

        override fun getItemCount(): Int = data.size
        inner class NumberLocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var ivIcon: AppCompatImageView = view.findViewById(R.id.iv_icon)
            var ivSelected: AppCompatImageView = view.findViewById(R.id.iv_selected)
            val rootLayout: ConstraintLayout = view.findViewById(R.id.root_layout)
        }
    }
    //endregion

    //region 背景颜色适配器
    class BgColorAdapter(private val context: DiyDialV2Activity, private val data: List<IntArray>) :
        RecyclerView.Adapter<BgColorAdapter.BgColorViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BgColorViewHolder {
            return BgColorViewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_item_diy_bg_color, parent, false)
            )
        }

        override fun getItemCount(): Int = data.size


        override fun onBindViewHolder(holder: BgColorViewHolder, position: Int) {
            val itemData = data[position]

            val colorStr = context.getString(R.string.number_text_color) + " " + ((position) + 1)
            holder.tvName.text = colorStr

            val colorList = intArrayOf(
                R.color.theme_color1,
                R.color.theme_color2,
                R.color.theme_color3,
                R.color.theme_color4,
                R.color.theme_color5,
                R.color.theme_color6,
                R.color.theme_color7,
                R.color.theme_color8,
                R.color.theme_color9
            )
            holder.layoutColor.removeAllViews()
            var isFixedValue = false
            for (i in colorList.indices) {
                val parent = (context as? Activity)?.findViewById<ViewGroup>(android.R.id.content)
                val mLinearLayout = context.layoutInflater.inflate(R.layout.layout_theme_color, parent, false) as LinearLayout
                val colorRoundView = mLinearLayout.findViewById<ColorRoundView>(R.id.colorRoundView)
                val ivColorBg = mLinearLayout.findViewById<ImageView>(R.id.ivColorBg)

                colorRoundView.setBgColor(colorList[i], colorList[i])
                if (itemData[0] == Color.red(colorRoundView.getColor())
                    && itemData[1] == Color.green(colorRoundView.getColor())
                    && itemData[2] == Color.blue(colorRoundView.getColor())
                ) {
                    ivColorBg.background = ContextCompat.getDrawable(context, R.drawable.theme_select_circle)
                    isFixedValue = true
                }
                //是提供固定的颜色
                if (isFixedValue) {
                    holder.ivCustomizeColor.background = Color.TRANSPARENT.toDrawable()
                } else {
                    holder.ivCustomizeColor.background = ContextCompat.getDrawable(context, R.drawable.theme_select_circle)
                }
                colorRoundView.setOnClickListener {
                    for (i in colorList.indices) {
                        val childView = holder.layoutColor.getChildAt(i)
                        val childViewColorBg = childView.findViewById<ImageView>(R.id.ivColorBg)
                        childViewColorBg.background = Color.TRANSPARENT.toDrawable()
                    }
                    ivColorBg.background =
                        ContextCompat.getDrawable(context, R.drawable.theme_select_circle)

                    val color: Int = colorRoundView.getColor()
                    context.setColor(position, Color.red(color), Color.green(color), Color.blue(color))
                    holder.ivCustomizeColor.background = Color.TRANSPARENT.toDrawable()
                }
                holder.layoutColor.addView(mLinearLayout)
            }

            holder.layoutCustomizeColor.setOnClickListener {
                showSelectColor(holder, position, context)
            }
        }

        private fun showSelectColor(holder: BgColorViewHolder, position: Int, context: DiyDialV2Activity) {
            //https://github.com/skydoves/ColorPickerView
            val colorPickerBuilder = ColorPickerDialog.Builder(context)
                .setTitle("")
                .setPreferenceName(COLOR_PICKER_NAME)
                .setNegativeButton(context.getString(R.string.btn_cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(false)
                .setBottomSpace(12)
                .setPositiveButton(
                    context.getString(R.string.btn_confirm),
                    ColorEnvelopeListener { envelope, fromUser ->
                        context.setColor(position, envelope.argb[1], envelope.argb[2], envelope.argb[3])
                        context.refFixedColorPickerItem()
                        holder.ivCustomizeColor.background = ContextCompat.getDrawable(context, R.drawable.theme_select_circle)
                    }
                )

            colorPickerBuilder.colorPickerView.apply {
                val bubbleFlag = BubbleFlag(context)
                bubbleFlag.flagMode = FlagMode.FADE
                setFlagView(bubbleFlag)
            }

            val item = context.selectedColors[position]
            ColorPickerPreferenceManager.getInstance(context).clearSavedAllData().setColor(COLOR_PICKER_NAME, Color.rgb(item[0], item[1], item[2]))
            colorPickerBuilder.show()
        }

        inner class BgColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var tvName = view.findViewById<AppCompatTextView>(R.id.tvName)!!
            var layoutColor = view.findViewById<LinearLayoutCompat>(R.id.layoutColor)!!
            var layoutCustomizeColor = view.findViewById<ConstraintLayout>(R.id.layoutCustomizeColor)!!
            var ivCustomizeColor = view.findViewById<AppCompatImageView>(R.id.ivCustomizeColor)!!
        }

    }

    //endregion

    //region 复杂功能

    class FunctionsAdapter(private val context: Context, var data: List<DiyWatchFaceConfigBean.FunctionsConfig>, var click: (position: Int) -> Unit) :
        RecyclerView.Adapter<FunctionsAdapter.FunctionsViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FunctionsViewHolder {
            return FunctionsViewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_item_complex, parent, false)
            )
        }

        override fun onBindViewHolder(holder: FunctionsViewHolder, position: Int) {
            val info = data[position]
            holder.tvLocation.text = DiyUtils.getFunctionsLocationNameByType(context, info.position)

            holder.tvContext.text = DiyUtils.getFunctionsDetailNameByType(context, info.typeChoose)

            ClickUtils.applySingleDebouncing(holder.rootLayout) {
                click(position)
            }
        }

        override fun getItemCount(): Int = data.size


        inner class FunctionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var rootLayout: LinearLayoutCompat = view.findViewById(R.id.rootLayout)
            var tvLocation: AppCompatTextView = view.findViewById(R.id.tvLocation)
            var tvContext: AppCompatTextView = view.findViewById(R.id.tvContext)
        }
    }

    //endregion


    private fun refFunctionsAdapter() {
        (binding.rvComplex.adapter as FunctionsAdapter?)?.let {
            it.data = diyWatchFaceConfig!!.functionsConfigs
            it.notifyDataSetChanged()
        }
    }

    private fun refPhotoAdapter() {
        (binding.rvPhoto.adapter as PhotoAdapter?)?.notifyDataSetChanged()
    }

    private fun refStyleAdapter() {
        (binding.rvStyle.adapter as StyleAdapter?)?.notifyDataSetChanged()
    }

    private fun refOverlayAdapter() {
        (binding.rvOverlay.adapter as OverlayAdapter?)?.notifyDataSetChanged()
    }

    private fun refNumberFontAndLocation() {
        (binding.rvNumberStyle.adapter as NumberStylerAdapter?)?.notifyDataSetChanged()
        (binding.rvNumberLocation.adapter as NumberLocationAdapter?)?.notifyDataSetChanged()
    }

    private fun refFixedColorPickerItem() {
        binding.rvBgColors.adapter?.notifyDataSetChanged()
    }

    //region 颜色选中
    private fun setColor(position: Int, red: Int, green: Int, blue: Int) {
        addLogI("setColor position=$position red=$red green=$green blue=$blue")
        if (position < selectedColors.size) {
            val item = selectedColors[position]
            item[0] = red
            item[1] = green
            item[2] = blue
        }
        refPreView()
    }
    //endregion


    //region 预览
    private fun refPreView() {
        val bean = getDiyParamsBean()
        addLogI("getNewPreviewBitmap")
        ControlBleTools.getInstance().getNewPreviewBitmap(bean, object : DiyDialPreviewCallBack {
            override fun onDialPreview(preview: Bitmap?) {
                if (preview != null) {
                    Glide.with(this@DiyDialV2Activity).load(preview).into(binding.ivPreview)
                }
            }

            override fun onError(errMsg: String?) {
                addLogE("refPreView onError errMsg=$errMsg")
            }
        })
    }
    //endregion


    /**
     * 获取diy表盘请求参数
     */
    private fun getDiyParamsBean(): NewDiyParamsBean {
        val newDiyParamsBean = NewDiyParamsBean()
        try {
            val diyDialBean: NewZhDiyDialBean? = GsonUtils.fromJson(dataJson, NewZhDiyDialBean::class.java)
            if (diyDialBean == null) {
                addLogI("diyDialBean is null")
                finish()
                return newDiyParamsBean
            }

            //json
            newDiyParamsBean.jsonStr = dataJson

            //背景资源
            val backgroundResBean = NewDiyParamsBean.BackgroundResBean()
            val background = photoSelectBitmap
            val backgroundOverlay = overlaySelectBitmap
            backgroundResBean.backgrounds = background
            backgroundResBean.backgroundOverlay = backgroundOverlay
            backgroundResBean.backgroundColors = selectedColors
            newDiyParamsBean.backgroundResBean = backgroundResBean

            //指针
            if (styleSelect != null) {
                val styleResBean = NewDiyParamsBean.StyleResBean()
                styleResBean.styleBm = styleSelect!!.imgData
                styleResBean.styleBin = styleSelect!!.binData
                newDiyParamsBean.styleResBean = styleResBean
            }

            //数字
            if (numberSelect != null) {
                val numberResBean = NewDiyParamsBean.NumberResBean()
                numberResBean.fontName = numberSelect!!.fontName
                for (font in numberFonts) {
                    if (font.fontName == numberResBean.fontName) {
                        numberResBean.fontMD5 = DiyDialUtils.getDiyBitmapMd5String(font.fontImg)
                    }
                }
                numberResBean.locationName = numberSelect!!.locationName
                numberResBean.locationMD5 = DiyDialUtils.getDiyBitmapMd5String(numberSelect!!.locationImg)
                numberResBean.numberBm = numberSelect!!.imgData
                numberResBean.numberBin = numberSelect!!.binData
                //默认使用第一个背景对应色
                numberResBean.red = selectedColors[0][0]
                numberResBean.green = selectedColors[0][1]
                numberResBean.blue = selectedColors[0][2]
                newDiyParamsBean.numberResBean = numberResBean
                //numberResBean.textInfos 由sdk内部根据json检测fontName，locationName补充，不需要赋值
            }

            //复杂功能
            val functionsResBean = NewDiyParamsBean.FunctionsResBean()

            val functionsPath = mResource + diyDialBean.complex.path.replace("\\", File.separator)
            functionsResBean.functionsBin = AssetUtils.getAssetBytes(this@DiyDialV2Activity, functionsPath)

            val functionsBitmapBeans = mutableListOf<NewDiyParamsBean.FunctionsResBean.FunctionsBitmapBean>()
            if (!diyDialBean.complex.infos.isNullOrEmpty()) {
                for (info in diyDialBean.complex.infos) {
                    if (!info.detail.isNullOrEmpty()) {
                        for (d in info.detail) {
                            val function = DiyUtils.getDiyWatchFaceFunctionByTypeName(d.typeName)
                            var isCanAdd = true
                            for (f in functionsBitmapBeans) {
                                if (f.function == function.function) {
                                    isCanAdd = false
                                }
                            }
                            if (isCanAdd) {
                                functionsBitmapBeans.add(
                                    NewDiyParamsBean.FunctionsResBean.FunctionsBitmapBean().apply {
                                        val imgPath = mResource + d.picPath.replace("\\", File.separator)
                                        val imgBitmap = AssetUtils.getAssetBitmap(this@DiyDialV2Activity, imgPath)
                                        this.bitmap = imgBitmap
                                        this.function = function.function
                                    }
                                )
                            }
                        }
                    }
                }
            }
            functionsResBean.functionsBitmaps = functionsBitmapBeans
            //复杂功能资源
            newDiyParamsBean.functionsResBean = functionsResBean
            //复杂功能设置
            newDiyParamsBean.diyWatchFaceConfigBean = diyWatchFaceConfig
            return newDiyParamsBean
        } catch (e: Exception) {
            e.printStackTrace()
            addLogE("getDiyParamsBean Exception e=${e.message}")
            finish()
            return newDiyParamsBean
        }
    }

}