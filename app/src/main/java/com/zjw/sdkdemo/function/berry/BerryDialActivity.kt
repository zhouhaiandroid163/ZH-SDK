package com.zjw.sdkdemo.function.berry

import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.PathUtils
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.WatchFaceListBean
import com.zhapp.ble.bean.berry.BerryAlbumWatchFaceEditRequestBean
import com.zhapp.ble.callback.BerryDialUploadListener
import com.zhapp.ble.callback.BerryWatchFaceStatusCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceLargeFileStatusListener
import com.zhapp.ble.callback.WatchFaceCallBack
import com.zhapp.ble.callback.WatchFaceInstallCallBack
import com.zhapp.ble.callback.WatchFaceListCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zhapp.ble.utils.BleUtils
import com.zhapp.ble.utils.MD5Utils
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.AcitivityBerryDialBinding
import com.zjw.sdkdemo.function.MainActivity.GlobalData
import com.zjw.sdkdemo.function.MainActivity.GlobalData.deviceInfo
import com.zjw.sdkdemo.utils.DialogUtils
import java.io.File
import kotlin.math.abs


class BerryDialActivity : BaseActivity() {
    private val binding by lazy { AcitivityBerryDialBinding.inflate(layoutInflater) }
    private val tag: String = BerryDialActivity::class.java.simpleName

    private val ordinaryFilePath = PathUtils.getExternalAppCachePath() + "/berry_ordinary_dial"
    private lateinit var ordinaryFile: File

    private val photoFilePath = PathUtils.getExternalAppCachePath() + "/berry_photo_dial"
    private lateinit var photoFile: File

    private val bgFilePath = PathUtils.getExternalAppCachePath() + "/berry_bg_img"
    private lateinit var bgFile: File
    private lateinit var bgFiles: MutableList<File>

    private var watchFaceList: MutableList<WatchFaceListBean> = mutableListOf()

    //是否发送相册表盘
    private var isSendPhoto = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_dial_berry)
        initLogSet(
            tag,
            binding.layoutLog.llLog,
            binding.layoutLog.cxLog,
            binding.layoutLog.llLogContent,
            binding.layoutLog.btnClear,
            binding.layoutLog.btnSet,
            binding.layoutLog.btnSendLog
        )
        initView()
        initListener()
        initCallBack()
        initData()
    }

    private fun initData() {
        FileUtils.createOrExistsDir(ordinaryFilePath)
        binding.layoutBerryDialOrdinary.layoutTip.tvTip.text = getString(R.string.select_file_attention, ordinaryFilePath)

        FileUtils.createOrExistsDir(photoFilePath)
        binding.layoutBerryDialPhoto.layoutSelectDialFile.tvTip.text = getString(R.string.select_file_attention, photoFilePath)

        FileUtils.createOrExistsDir(bgFilePath)
        binding.layoutBerryDialPhoto.layoutSelectBgFile.tvTip.text = getString(R.string.select_file_attention, bgFilePath)

    }

    private fun initView() {
        setMyCheckBox(binding.layoutBerryDialManage.cbTop, binding.layoutBerryDialManage.llBottom, binding.layoutBerryDialManage.ivHelp)
        setMyCheckBox(binding.layoutBerryDialOrdinary.cbTop, binding.layoutBerryDialOrdinary.llBottom, binding.layoutBerryDialOrdinary.ivHelp)
        setMyCheckBox(binding.layoutBerryDialPhoto.cbTop, binding.layoutBerryDialPhoto.llBottom, binding.layoutBerryDialPhoto.ivHelp)
        binding.layoutBerryDialManage.rvList.layoutManager = LinearLayoutManager(this)
        binding.layoutBerryDialManage.rvList.adapter = MyAdapter(watchFaceList)
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutBerryDialManage.btnGet) {
            addLogI("layoutBerryDialManage.btnGet")
            addLogI("getWatchFaceList")
            ControlBleTools.getInstance().getWatchFaceList(object : WatchFaceListCallBack {
                override fun onResponse(list: MutableList<WatchFaceListBean>) {
                    addLogBean("getWatchFaceList onResponse", list)

                    val oldSize = watchFaceList.size
                    watchFaceList.clear()
                    binding.layoutBerryDialManage.rvList.adapter?.notifyItemRangeRemoved(0, oldSize)
                    watchFaceList.addAll(list)
                    binding.layoutBerryDialManage.rvList.adapter?.notifyItemRangeInserted(0, list.size)
                }

                override fun timeOut(errorStart: SendCmdState) {
                    addLogI("getWatchFaceList timeOut errorStart=$errorStart")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryDialOrdinary.layoutSelectFile.btnSelectFile) {
            addLogI("btnSelectFile")
            DialogUtils.showSelectFileDialog(this, ordinaryFilePath, ".bin") { selectedFile ->
                ordinaryFile = selectedFile
                binding.layoutBerryDialOrdinary.layoutSelectFile.tvFileName.text = selectedFile.name
            }
        }

        clickCheckConnect(binding.layoutBerryDialOrdinary.btnSend) {
            addLogI("layoutBerryDialOrdinary.btnSend")
            if (!::ordinaryFile.isInitialized) {
                addLogI(getString(R.string.select_file_tip))
                return@clickCheckConnect
            }
            val dialId = getTestDialIDByFile(ordinaryFile)
            val dialStyle = binding.layoutBerryDialOrdinary.etStyle.text.toString()
            val dialData = FileIOUtils.readFile2BytesByStream(ordinaryFile)
            isSendPhoto = false
            addLogI("getWatchFaceStatusByBerry dialId=$dialId dialStyle=$dialStyle dialData=${dialData.size}")
            ControlBleTools.getInstance().getWatchFaceStatusByBerry(dialId, dialStyle, dialData.size, null, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getWatchFaceStatusByBerry state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryDialPhoto.layoutSelectDialFile.btnSelectFile) {
            addLogI("layoutSelectDialFile.btnSelectFile")
            DialogUtils.showSelectFileDialog(this, photoFilePath, ".bin") { selectedFile ->
                photoFile = selectedFile
                binding.layoutBerryDialPhoto.layoutSelectDialFile.tvFileName.text = selectedFile.name
            }
        }

        clickCheckConnect(binding.layoutBerryDialPhoto.layoutSelectBgFile.btnSelectFile) {
            addLogI("layoutSelectBgFile.btnSelectFile")
            if (ControlBleTools.getInstance().berryAlbumVersion == BleCommonAttributes.BERRY_ALBUM_VERSION_0) {
                DialogUtils.showSelectImgDialog(this, bgFilePath) { selectedFile ->
                    bgFile = selectedFile
                    binding.layoutBerryDialPhoto.layoutSelectBgFile.tvFileName.text = selectedFile.name
                }
            } else if (ControlBleTools.getInstance().berryAlbumVersion == BleCommonAttributes.BERRY_ALBUM_VERSION_1) {
                DialogUtils.showSelectImgDialog(this, bgFilePath) { selectedFile ->
                    if (!::bgFiles.isInitialized) {
                        bgFiles = mutableListOf<File>()
                    }
                    bgFiles.add(selectedFile)
                    binding.layoutBerryDialPhoto.layoutSelectBgFile.tvFileName.text =
                        binding.layoutBerryDialPhoto.layoutSelectBgFile.tvFileName.text.toString() + "," + selectedFile.name
                }
            }
        }

        clickCheckConnect(binding.layoutBerryDialPhoto.btnSend) {
            addLogI("layoutBerryDialPhoto.btnSend")
            if (!::photoFile.isInitialized) {
                addLogI(getString(R.string.select_file_tip))
                return@clickCheckConnect
            }
            if (watchFaceList.isEmpty()) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }
            val isPhoto = true
            val dialId = binding.layoutBerryDialPhoto.etId.text.toString().trim()
            val dialStyle = binding.layoutBerryDialPhoto.etStyle.text.toString()
            val dialData = FileIOUtils.readFile2BytesByStream(photoFile)
            val dialWidth = binding.layoutBerryDialPhoto.etWidth.text.toString().toInt()
            val dailHeight = binding.layoutBerryDialPhoto.etHeight.text.toString().toInt()

            if (dialId.isEmpty()) {
                addLogI(getString(R.string.get_data_err1))
                return@clickCheckConnect
            }

            if (dialWidth <= 0 || dailHeight <= 0) {
                addLogI(getString(R.string.get_data_err1))
                return@clickCheckConnect
            }
            var isNeedGetState = true
            for (item in watchFaceList) {
                if (TextUtils.equals(item.id, dialId)) {
                    isNeedGetState = false
                }
            }

            //未安装 - 需查询状态后再发送
            if (isNeedGetState) {
                isSendPhoto = true
                if (ControlBleTools.getInstance().berryAlbumVersion == BleCommonAttributes.BERRY_ALBUM_VERSION_0) {
                    var bgBitmap: Bitmap? = null
                    if (::bgFile.isInitialized) {
                        val bgData = FileIOUtils.readFile2BytesByStream(bgFile)
                        bgBitmap = ImageUtils.scale(ConvertUtils.bytes2Bitmap(bgData), dialWidth, dailHeight)
                        bgBitmap = ImageUtils.toRoundCorner(bgBitmap, ControlBleTools.getInstance().berryAlbumRadius * 1.0f)
                    }
                    addLogI("getWatchFaceStatusByBerry dialId=$dialId dialStyle=$dialStyle dialData=${dialData.size}")
                    ControlBleTools.getInstance().getWatchFaceStatusByBerry(dialId, dialStyle, dialData.size, bgBitmap, object : SendCmdStateListener() {
                        override fun onState(state: SendCmdState) {
                            addLogI("getWatchFaceStatusByBerry state=$state")
                        }
                    })
                } else if (ControlBleTools.getInstance().berryAlbumVersion == BleCommonAttributes.BERRY_ALBUM_VERSION_1) {
                    val bgBitmaps = mutableListOf<Bitmap>()
                    if (::bgFiles.isInitialized) {
                        for (file in bgFiles) {
                            val bitmap = ImageUtils.scale(ConvertUtils.bytes2Bitmap(FileIOUtils.readFile2BytesByStream(file)), dialWidth, dailHeight)
                            //处理圆角
                            bgBitmaps.add(ImageUtils.toRoundCorner(bitmap, ControlBleTools.getInstance().berryAlbumRadius * 1.0f))
                        }
                    }
                    addLogI("getWatchFaceStatusMultiImgByBerry dialId=$dialId dialStyle=$dialStyle dialData=${dialData.size}")
                    ControlBleTools.getInstance().getWatchFaceStatusMultiImgByBerry(dialId, dialStyle, dialData.size, bgBitmaps, object : SendCmdStateListener() {
                        override fun onState(state: SendCmdState) {
                            addLogI("getWatchFaceStatusMultiImgByBerry state=$state")
                        }
                    })
                }
            }
            //已安装，直接发送
            else {
                sendPhotoFile()
            }
        }
    }

    private fun initCallBack() {
        CallBackUtils.watchFaceCallBack = object : WatchFaceCallBack {
            override fun setWatchFace(isSet: Boolean) {
                addLogI("watchFaceCallBack setWatchFace isSet=$isSet")
                if (isSet) {
                    binding.layoutBerryDialManage.btnGet.callOnClick()
                }
            }

            override fun removeWatchFace(isRemove: Boolean) {
                addLogI("watchFaceCallBack removeWatchFace isRemove=$isRemove")
                if (isRemove) {
                    binding.layoutBerryDialManage.btnGet.callOnClick()
                }
            }
        }

        CallBackUtils.berryWatchFaceStatusCallBack = BerryWatchFaceStatusCallBack { bean ->
            addLogBean("berryWatchFaceStatusCallBack", bean)
            if (GlobalData.deviceInfo == null) {
                addLogI("deviceInfo is null")
                return@BerryWatchFaceStatusCallBack
            }
            if (bean.statusValue == BerryWatchFaceStatusCallBack.PrepareStatus.READY.state) {
                if (!isSendPhoto) {
                    sendOrdinaryFile()
                } else {
                    sendPhotoFile()
                }
            }
        }

        CallBackUtils.watchFaceInstallCallBack = WatchFaceInstallCallBack { bean ->
            addLogBean("watchFaceInstallCallBack", bean)

            if (bean != null) {
                when (bean?.code) {
                    WatchFaceInstallCallBack.WatchFaceInstallCode.INSTALL_SUCCESS.state -> {
                        //安装成功 Installation Successful
                        binding.layoutBerryDialManage.btnGet.callOnClick()
                    }

                    WatchFaceInstallCallBack.WatchFaceInstallCode.INSTALL_FAILED.state -> {
                        //安装失败 Installation failed
                    }
                    WatchFaceInstallCallBack.WatchFaceInstallCode.VERIFY_FAILED.state,
                    WatchFaceInstallCallBack.WatchFaceInstallCode.INSTALL_USED.state -> {
                        //验证失败 Authentication failed
                    }
                }
            }
        }
    }

    private fun sendOrdinaryFile() {
        val isPhoto = false
        val dialData = FileIOUtils.readFile2BytesByStream(photoFile)
        val fileType = BleCommonAttributes.UPLOAD_BIG_DATA_WATCH
        val deviceNum = GlobalData.deviceInfo!!.equipmentNumber
        val deviceVer = GlobalData.deviceInfo!!.firmwareVersion

        addLogI("getDeviceLargeFileStateByBerry dialData=${dialData.size} fileType=$fileType deviceNum=$deviceNum deviceVer=$deviceVer")
        ControlBleTools.getInstance().getDeviceLargeFileStateByBerry(dialData, fileType, deviceNum, deviceVer, object : DeviceLargeFileStatusListener {
            override fun onSuccess(statusValue: Int, statusName: String?) {
                addLogI("getDeviceLargeFileStateByBerry onSuccess statusValue=$statusValue statusName=$statusName")

                if (statusValue == DeviceLargeFileStatusListener.PrepareStatus.READY.state) {

                    val background = null
                    val requestBean = null
                    addLogI("startUploadDialBigDataByBerry isPhoto=$isPhoto dialData=${dialData.size} background=$background requestBean=$requestBean deviceVer=$deviceVer")
                    ControlBleTools.getInstance().startUploadDialBigDataByBerry(isPhoto, dialData, background, requestBean, object : BerryDialUploadListener {
                        override fun onSuccess(code: Int) {
                            addLogI("startUploadDialBigDataByBerry onSuccess code=$code")
                        }

                        override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                            val percentage = (curPiece * 100 / dataPackTotalPieceLength)
                            addLogI("DeviceLargeFileStatusListener onProgress curPiece=$curPiece dataPackTotalPieceLength=$dataPackTotalPieceLength  percentage=$percentage")
                        }

                        override fun onTimeout(msg: String?) {
                            addLogE("startUploadDialBigDataByBerry onTimeout msg=$msg")
                        }
                    })
                }
            }

            override fun timeOut() {
                addLogE("getDeviceLargeFileStateByBerry timeOut")
            }
        })
    }

    private fun sendPhotoFile() {
        val isPhoto = true
        val dialData = FileIOUtils.readFile2BytesByStream(photoFile)
        val fileType = BleCommonAttributes.UPLOAD_BIG_DATA_WATCH
        val deviceNum = GlobalData.deviceInfo!!.equipmentNumber
        val deviceVer = GlobalData.deviceInfo!!.firmwareVersion

        val dialId = binding.layoutBerryDialPhoto.etId.text.toString().trim()
        val dialStyle = binding.layoutBerryDialPhoto.etStyle.text.toString()
        val dialWidth = binding.layoutBerryDialPhoto.etWidth.text.toString().toInt()
        val dailHeight = binding.layoutBerryDialPhoto.etHeight.text.toString().toInt()
        val imageSwitchStyle = binding.layoutBerryDialPhoto.etBgStyle.text.toString().toInt()

        if (dialWidth <= 0 || dailHeight <= 0) {
            addLogI(getString(R.string.get_data_err1))
            return
        }

        addLogI("getDeviceLargeFileStateByBerry dialData=${dialData.size} fileType=$fileType deviceNum=$deviceNum deviceVer=$deviceVer")
        ControlBleTools.getInstance().getDeviceLargeFileStateByBerry(dialData, fileType, deviceNum, deviceVer, object : DeviceLargeFileStatusListener {
            override fun onSuccess(statusValue: Int, statusName: String?) {
                addLogI("getDeviceLargeFileStateByBerry onSuccess statusValue=$statusValue statusName=$statusName")

                if (statusValue == DeviceLargeFileStatusListener.PrepareStatus.READY.state) {

                    if (ControlBleTools.getInstance().berryAlbumVersion == BleCommonAttributes.BERRY_ALBUM_VERSION_0) {
                        val bgData = FileIOUtils.readFile2BytesByStream(bgFile)
                        var bgBitmap: Bitmap? = null
                        if (::bgFile.isInitialized) {
                            bgBitmap = ImageUtils.scale(ConvertUtils.bytes2Bitmap(bgData), dialWidth, dailHeight)
                            bgBitmap = ImageUtils.toRoundCorner(bgBitmap, ControlBleTools.getInstance().berryAlbumRadius * 1.0f)
                        }
                        val albumRequest = BerryAlbumWatchFaceEditRequestBean()
                        albumRequest.id = dialId
                        albumRequest.isSetCurrent = true
                        albumRequest.style = dialStyle

                        val background = bgBitmap
                        val requestBean = albumRequest
                        addLogI("startUploadDialBigDataByBerry isPhoto=$isPhoto dialData=${dialData.size} background=$background requestBean=$requestBean deviceVer=$deviceVer")
                        ControlBleTools.getInstance().startUploadDialBigDataByBerry(isPhoto, dialData, background, requestBean, object : BerryDialUploadListener {
                            override fun onSuccess(code: Int) {
                                addLogI("startUploadDialBigDataByBerry onSuccess code=$code")
                            }

                            override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                                val percentage = (curPiece * 100 / dataPackTotalPieceLength)
                                addLogI("startUploadDialBigDataByBerry onProgress curPiece=$curPiece dataPackTotalPieceLength=$dataPackTotalPieceLength  percentage=$percentage")
                            }

                            override fun onTimeout(msg: String?) {
                                addLogE("startUploadDialBigDataByBerry onTimeout msg=$msg")
                            }
                        })

                    } else if (ControlBleTools.getInstance().berryAlbumVersion == BleCommonAttributes.BERRY_ALBUM_VERSION_1) {
                        val bgBitmaps: MutableList<Bitmap> = mutableListOf()
                        if (::bgFiles.isInitialized) {
                            for (file in bgFiles) {
                                val bitmap = ImageUtils.scale(ConvertUtils.bytes2Bitmap(FileIOUtils.readFile2BytesByStream(file)), dialWidth, dailHeight)
                                //处理圆角
                                bgBitmaps.add(ImageUtils.toRoundCorner(bitmap, ControlBleTools.getInstance().berryAlbumRadius * 1.0f))
                            }
                        }
                        val albumRequest = BerryAlbumWatchFaceEditRequestBean()
                        albumRequest.id = dialId
                        albumRequest.isSetCurrent = true
                        albumRequest.style = dialStyle
                        //----
                        albumRequest.imageSwitchStyle = imageSwitchStyle
                        addLogI("startUploadDialBigDataByBerry isPhoto=$isPhoto dialData=${dialData.size} background=$bgBitmaps requestBean=$albumRequest deviceVer=$deviceVer")
                        ControlBleTools.getInstance().startUploadDialMultiImgBigDataByBerry(isPhoto, dialData, bgBitmaps, albumRequest, object : BerryDialUploadListener {
                            override fun onSuccess(code: Int) {
                                addLogI("startUploadDialBigDataByBerry onSuccess code=$code")
                            }

                            override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                                val percentage = (curPiece * 100 / dataPackTotalPieceLength)
                                addLogI("startUploadDialBigDataByBerry onProgress curPiece=$curPiece dataPackTotalPieceLength=$dataPackTotalPieceLength  percentage=$percentage")
                            }

                            override fun onTimeout(msg: String?) {
                                addLogE("startUploadDialBigDataByBerry onTimeout msg=$msg")
                            }
                        })
                    }
                }

            }

            override fun timeOut() {
                addLogE("getDeviceLargeFileStateByBerry timeOut")
            }
        })
    }

    inner class MyAdapter(private val dataList: List<WatchFaceListBean>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tvDialId: AppCompatTextView = itemView.findViewById(R.id.tvDialId)
            var tvDialName: AppCompatTextView = itemView.findViewById(R.id.tvDialName)
            var tvDialType: AppCompatTextView = itemView.findViewById(R.id.tvDialType)
            var btnCur: AppCompatButton = itemView.findViewById(R.id.btnCur)
            var btnDel: AppCompatButton = itemView.findViewById(R.id.btnDel)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_berry_dial, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvDialId.text = dataList[position].id
            holder.tvDialName.text = dataList[position].name
            holder.tvDialType.text = dataList[position].style
            holder.btnCur.isEnabled = !dataList[position].isCurrent
            holder.btnDel.visibility = if (dataList[position].isRemove) View.VISIBLE else View.GONE
            ClickUtils.applySingleDebouncing(holder.btnCur) {
                onSetNow(dataList[position])
            }
            ClickUtils.applySingleDebouncing(holder.btnDel) {
                onDelete(dataList[position])
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }
    }

    private fun onSetNow(watchFace: WatchFaceListBean) {
        addLogI("onSetNow")
        val id = watchFace.id
        addLogI("setDeviceWatchFromId id=$id")
        ControlBleTools.getInstance().setDeviceWatchFromId(id, object : SendCmdStateListener() {
            override fun onState(state: SendCmdState?) {
                addLogI("setDeviceWatchFromId state=$state")
            }
        })
    }

    private fun onDelete(watchFace: WatchFaceListBean) {
        addLogI("onDelete")
        val id = watchFace.id
        addLogI("deleteDeviceWatchFromId id=$id")
        ControlBleTools.getInstance().deleteDeviceWatchFromId(id, object : SendCmdStateListener() {
            override fun onState(state: SendCmdState?) {
                addLogI("deleteDeviceWatchFromId state=$state")
            }
        })
    }

    private fun getTestDialIDByFile(file: File): String {
        return abs(BleUtils.byte2Int(MD5Utils.getMD5bytes(FileIOUtils.readFile2BytesByStream(file)), false)).toString()
    }

}