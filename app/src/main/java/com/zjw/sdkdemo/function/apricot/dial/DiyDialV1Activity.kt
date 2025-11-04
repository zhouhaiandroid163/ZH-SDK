package com.zjw.sdkdemo.function.apricot.dial

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ResourceUtils
import com.bumptech.glide.Glide
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.DiyWatchFaceConfigBean
import com.zhapp.ble.bean.diydial.OldDiyParamsBean
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
import com.zjw.sdkdemo.databinding.ActivityDiyDialV1Binding
import com.zjw.sdkdemo.utils.AssetUtils
import com.zjw.sdkdemo.utils.DiyUtils

@SuppressLint("NotifyDataSetChanged")
class DiyDialV1Activity : BaseActivity() {
    private val binding by lazy { ActivityDiyDialV1Binding.inflate(layoutInflater) }
    private val tag: String = DiyDialV1Activity::class.java.simpleName

    private var dataJson = ""

    private val photoData = mutableListOf<PhotoBean>()
    private var photoSelectBitmap: Bitmap? = null

    private val styles = mutableListOf<StyleBean>()
    private var styleSelect: StyleBean? = null

    private var diyWatchFaceConfig: DiyWatchFaceConfigBean? = null

    private lateinit var functionSelectResultLauncher: ActivityResultLauncher<Intent>

    private val mResource = AssetUtils.ASS_APRICOT_DIAL_DIY_V1_FOLDER + "diy_resource"
    private val mUser = AssetUtils.ASS_APRICOT_DIAL_DIY_V1_FOLDER + "diy_user_bg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.clock_dial_diy_v1_demo)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initListener()
        initCallBack()
        initView()
        initData()
    }

    private fun initView() {
        binding.rvPhoto.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvPhoto.adapter = PhotoAdapter(this, photoData) { selectedP ->
            photoSelectBitmap = photoData[selectedP].imgBitmap
            refPreView()
        }
        binding.rvStyle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvStyle.adapter = StyleAdapter(this, styles) { selectedP ->
            styleSelect = styles[selectedP]
            refPreView()
        }
    }

    private fun initData() {

        dataJson = ResourceUtils.readAssets2String("$mResource/watch.json")

        //背景 background
        photoData.apply {
            add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/background/background.png"), true))
            add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mUser/photo_1.png"), false))
            add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mUser/photo_2.png"), false))
            add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mUser/photo_3.png"), false))
            add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mUser/photo_4.png"), false))
            add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mUser/photo_5.png"), false))
            add(PhotoBean(AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mUser/photo_6.png"), false))
        }

        //指针 pointer
        styles.add(
            StyleBean(
                OldDiyParamsBean.StyleResBean.StyleType.POINTER.type,
                AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/pointer/3101_IMG.png")!!,
                AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/pointer/3101_Overlay.png")!!,
                AssetUtils.getAssetBytes(this@DiyDialV1Activity, "$mResource/pointer/3101_Data.bin")!!, false
            )
        )
        styles.add(
            StyleBean(
                OldDiyParamsBean.StyleResBean.StyleType.POINTER.type,
                AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/pointer/3102_IMG.png")!!,
                AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/pointer/3102_Overlay.png")!!,
                AssetUtils.getAssetBytes(this@DiyDialV1Activity, "$mResource/pointer/3102_Data.bin")!!, false
            )
        )
        styles.add(
            StyleBean(
                OldDiyParamsBean.StyleResBean.StyleType.POINTER.type,
                AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/pointer/3103_IMG.png")!!,
                AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/pointer/3103_Overlay.png")!!,
                AssetUtils.getAssetBytes(this@DiyDialV1Activity, "$mResource/pointer/3103_Data.bin")!!, false
            )
        )

        //数字 number
        styles.add(
            StyleBean(
                OldDiyParamsBean.StyleResBean.StyleType.NUMBER.type,
                AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/time/3001_IMG.png")!!,
                AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/time/3001_Overlay.png")!!,
                AssetUtils.getAssetBytes(this@DiyDialV1Activity, "$mResource/time/3001_Data.bin")!!, false
            )
        )
        styles.add(
            StyleBean(
                OldDiyParamsBean.StyleResBean.StyleType.NUMBER.type,
                AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/time/3002_IMG.png")!!,
                AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/time/3002_Overlay.png")!!,
                AssetUtils.getAssetBytes(this@DiyDialV1Activity, "$mResource/time/3002_Data.bin")!!, false
            )
        )
        styles.add(
            StyleBean(
                OldDiyParamsBean.StyleResBean.StyleType.NUMBER.type,
                AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/time/3003_IMG.png")!!,
                AssetUtils.getAssetBitmap(this@DiyDialV1Activity, "$mResource/time/3003_Overlay.png")!!,
                AssetUtils.getAssetBytes(this@DiyDialV1Activity, "$mResource/time/3003_Data.bin")!!, false
            )
        )

        styles[0].isSelected = true
        styleSelect = styles[0]

        addLogBean("getDefDiyWatchFaceConfig",dataJson)
        diyWatchFaceConfig = ControlBleTools.getInstance().getDefDiyWatchFaceConfig(dataJson)
        addLogBean("getDefDiyWatchFaceConfig",diyWatchFaceConfig!!)

        if (diyWatchFaceConfig != null && diyWatchFaceConfig!!.functionsConfigs != null) {
            binding.rvComplex.apply {
                layoutManager = LinearLayoutManager(this@DiyDialV1Activity, LinearLayoutManager.VERTICAL, false)
                addItemDecoration(DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL))
                adapter = FunctionsAdapter(this@DiyDialV1Activity, diyWatchFaceConfig!!.functionsConfigs!!) { clickPosition ->
                    val info = diyWatchFaceConfig!!.functionsConfigs!![clickPosition]
                    val details = info.functionsConfigTypes
                    if (details.isNullOrEmpty()) {
                        return@FunctionsAdapter
                    }
                    val intent = Intent(this@DiyDialV1Activity, DiyFunctionSelectActivity::class.java)
                    intent.putExtra(DiyFunctionSelectActivity.ACTIVITY_DATA_TEXT, GsonUtils.toJson(info))
                    functionSelectResultLauncher.launch(intent)
                }
            }
        }

        refPreView()
        activityResultRegister()

        val diyWatchId = diyWatchFaceConfig?.id
        addLogI("getDiyWatchFaceConfig diyWatchId=$diyWatchId")
        ControlBleTools.getInstance().getDiyWatchFaceConfig(diyWatchId, object : SendCmdStateListener() {
            override fun onState(state: SendCmdState?) {
                addLogI("getDiyWatchFaceConfig state=$state")
            }
        })
    }

    private fun initListener() {
        clickCheckConnect(binding.btnSync) {
            addLogI("btnSync")
            val bean = getDiyParamsBean()
            addLogI("getOldDiyDialData")
            ControlBleTools.getInstance().getOldDiyDialData(bean, object : DiyDialDataCallBack {
                override fun onDialData(diyDialId: String, data: ByteArray, bean: DiyWatchFaceConfigBean) {

                    addLogBean("getDeviceDiyWatchFace onDialData diyDialId=$diyDialId data.size=${data.size}", bean)
                    ControlBleTools.getInstance().getDeviceDiyWatchFace(diyDialId, data.size, true, bean, object : DeviceWatchFaceFileStatusListener {
                        override fun onSuccess(statusValue: Int, statusName: String) {
                            addLogI("getDeviceDiyWatchFace statusValue$statusValue statusName=$statusName")

                            if (statusValue == DeviceWatchFaceFileStatusListener.PrepareStatus.READY.state) {

                                val type = BleCommonAttributes.UPLOAD_BIG_DATA_WATCH
                                val isReplace = true
                                addLogI("startUploadBigData type=$type fileByte=${data.size} isReplace=$isReplace")
                                ControlBleTools.getInstance().startUploadBigData(type, data, isReplace, object : UploadBigDataListener {
                                    override fun onSuccess() {
                                        diyWatchFaceConfig = bean
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
                            addLogE("getDeviceDiyWatchFace timeOut")
                        }
                    })
                }

                override fun onChangeConfig(bean: DiyWatchFaceConfigBean) {
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
                    addLogI("getOldDiyDialData onError errMsg=$errMsg")
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
            //刷新背景
            if (diyWatchFaceConfig?.backgroundFileConfig != null && !diyWatchFaceConfig!!.backgroundFileConfig.watchFaceFiles.isNullOrEmpty()) {
                var selectedMd5 = ""
                for (faceFile in diyWatchFaceConfig!!.backgroundFileConfig!!.watchFaceFiles) {
                    if (faceFile.fileNumber == diyWatchFaceConfig!!.backgroundFileConfig!!.usedFileNumber) {
                        selectedMd5 = faceFile.fileMd5
                        addLogI("background selectedMd5=$selectedMd5")
                    }
                }
                for (photo in photoData) {
                    addLogI("background Md5=${DiyDialUtils.getDiyBitmapMd5String(photo.imgBitmap)}")
                    photo.isSelected = TextUtils.equals(selectedMd5, DiyDialUtils.getDiyBitmapMd5String(photo.imgBitmap))
                    if (photo.isSelected) photoSelectBitmap = photo.imgBitmap
                }
            }
            refPhotoAdapter()
            //刷新指针或者数字
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
                    addLogI("pointer md5=${DiyDialUtils.getDiyBinBytesMd5(style.binData)}")
                }
            }
            if (diyWatchFaceConfig?.numberFileConfig != null && !diyWatchFaceConfig!!.numberFileConfig.watchFaceFiles.isNullOrEmpty()) {
                var selectedMd5 = ""
                for (faceFile in diyWatchFaceConfig!!.numberFileConfig!!.watchFaceFiles) {
                    if (faceFile.fileNumber == diyWatchFaceConfig!!.numberFileConfig!!.usedFileNumber) {
                        selectedMd5 = faceFile.fileMd5
                        addLogI("diy_resource selectedMd5=$selectedMd5")
                    }
                }
                for (style in styles) {
                    style.isSelected = TextUtils.equals(selectedMd5, DiyDialUtils.getDiyBinBytesMd5(style.binData))
                    if (style.isSelected) styleSelect = style
                    addLogI("diy_resource md5=${DiyDialUtils.getDiyBinBytesMd5(style.binData)}")
                }

            }
            refStyleAdapter()
            refPreView()
        }


        /**
         * 表盘文件安装结果回调
         */
        CallBackUtils.watchFaceInstallCallBack = WatchFaceInstallCallBack { bean ->
            addLogBean("watchFaceInstallCallBack", bean)
            if (bean.code == WatchFaceInstallCallBack.WatchFaceInstallCode.INSTALL_SUCCESS.state) {
                if (bean.diyWatchFaceConfigBean != null) {
                    diyWatchFaceConfig = bean.diyWatchFaceConfigBean
                }
            }
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


    //region 预览
    private fun refPreView() {
        val bean = getDiyParamsBean()
        addLogI("getOldPreviewBitmap")
        ControlBleTools.getInstance().getOldPreviewBitmap(bean, object : DiyDialPreviewCallBack {
            override fun onDialPreview(preview: Bitmap) {
                addLogE("getOldPreviewBitmap preview size=${preview.width}x${preview.height}")
                Glide.with(this@DiyDialV1Activity).load(preview).into(binding.ivPreview)
            }

            override fun onError(errMsg: String?) {
                addLogE("getOldPreviewBitmap errMsg=$errMsg")
            }
        })
    }

    //region 图片选择适配器
    data class PhotoBean(var imgBitmap: Bitmap?, var isSelected: Boolean)

    class PhotoAdapter(private val context: Context, private val data: List<PhotoBean>, var selected: (position: Int) -> Unit) :
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

    //指针、数字适配器
    data class StyleBean(var type: Int, var img: Bitmap, var imgData: Bitmap, var binData: ByteArray, var isSelected: Boolean) {
        // 重写 equals 方法，确保比较数组内容
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StyleBean) return false

            if (type != other.type) return false
            if (img != other.img) return false
            if (imgData != other.imgData) return false
            if (!binData.contentEquals(other.binData)) return false
            if (isSelected != other.isSelected) return false

            return true
        }

        // 重写 hashCode 方法，基于数组内容生成哈希值
        override fun hashCode(): Int {
            var result = type
            result = 31 * result + img.hashCode()
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


    /**
     * 获取diy表盘请求参数
     */
    private fun getDiyParamsBean(): OldDiyParamsBean {
        val diyParamsBean = OldDiyParamsBean()

        //json
        diyParamsBean.jsonStr = dataJson

        //背景资源
        val backgroundResBean = OldDiyParamsBean.BackgroundResBean()
        val background: Bitmap =
            if (photoSelectBitmap == null) {
                //默认背景
                AssetUtils.getAssetBitmap(this, "$mResource/background/background.png")!!
            } else {
                //本地资源
                photoSelectBitmap!!
                //TODO 相册/相机
            }
        val backgroundOverlay = AssetUtils.getAssetBitmap(this, "$mResource/background/overlay.png")!!
        backgroundResBean.background = background
        backgroundResBean.backgroundOverlay = backgroundOverlay
        diyParamsBean.backgroundResBean = backgroundResBean

        //指针
        if (styleSelect != null) {
            val styleResBean = OldDiyParamsBean.StyleResBean()
            styleResBean.type = styleSelect!!.type
            styleResBean.styleBm = styleSelect!!.imgData
            styleResBean.styleBin = styleSelect!!.binData
            diyParamsBean.styleResBean = styleResBean
        }

        //复杂功能
        val functionsResBean = OldDiyParamsBean.FunctionsResBean()
        functionsResBean.functionsBin = AssetUtils.getAssetBytes(this, "$mResource/complex/complex.bin")
        val functionsBitmapBeans = mutableListOf<OldDiyParamsBean.FunctionsResBean.FunctionsBitmapBean>()

        var functionsBitmapBean = OldDiyParamsBean.FunctionsResBean.FunctionsBitmapBean()
        functionsBitmapBean.bitmap = AssetUtils.getAssetBitmap(this, "$mResource/complex/calorie.png")
        functionsBitmapBean.function = DiyWatchFaceCallBack.DiyWatchFaceFunction.CALORIE.function
        functionsBitmapBeans.add(functionsBitmapBean)

        functionsBitmapBean = OldDiyParamsBean.FunctionsResBean.FunctionsBitmapBean()
        functionsBitmapBean.bitmap = AssetUtils.getAssetBitmap(this, "$mResource/complex/generaldate.png")
        functionsBitmapBean.function = DiyWatchFaceCallBack.DiyWatchFaceFunction.GENERAL_DATE.function
        functionsBitmapBeans.add(functionsBitmapBean)

        functionsBitmapBean = OldDiyParamsBean.FunctionsResBean.FunctionsBitmapBean()
        functionsBitmapBean.bitmap = AssetUtils.getAssetBitmap(this, "$mResource/complex/kwh.png")
        functionsBitmapBean.function = DiyWatchFaceCallBack.DiyWatchFaceFunction.BATTERY.function

        functionsBitmapBeans.add(functionsBitmapBean)
        functionsBitmapBean = OldDiyParamsBean.FunctionsResBean.FunctionsBitmapBean()
        functionsBitmapBean.bitmap = AssetUtils.getAssetBitmap(this, "$mResource/complex/step.png")
        functionsBitmapBean.function = DiyWatchFaceCallBack.DiyWatchFaceFunction.STEP.function

        functionsBitmapBeans.add(functionsBitmapBean)
        functionsResBean.functionsBitmaps = functionsBitmapBeans
        //复杂功能资源
        diyParamsBean.functionsResBean = functionsResBean
        //复杂功能设置
        diyParamsBean.diyWatchFaceConfigBean = diyWatchFaceConfig
        return diyParamsBean
    }

}